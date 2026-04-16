package com.example.saber_share.fragmentos.contenido;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.saber_share.R;
import com.example.saber_share.model.PaypalRequestDto;
import com.example.saber_share.model.PaypalResponseDto;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Comprar extends Fragment {

    public static final String ARG_ITEM_ID     = "itemId";
    public static final String ARG_TIPO        = "tipo";
    public static final String ARG_TITULO      = "titulo";
    public static final String ARG_PRECIO      = "precio";
    public static final String ARG_DESCRIPCION = "descripcion";

    public static final String PAYPAL_RETURN_SCHEME = "sabersha";
    public static final String PAYPAL_RETURN_HOST   = "paypal-return";

    private static final String PREFS_NAME    = "paypal_pending";
    private static final String KEY_ITEM_ID   = "itemId";
    private static final String KEY_TIPO      = "tipo";
    private static final String KEY_USUARIO   = "usuarioId";

    private TextView    tvTitulo, tvPrecio, tvDescripcion, tvTipo;
    private Button      btnPagar;
    private ProgressBar progressBar;

    private int    itemId;
    private String tipo, titulo, descripcion;
    private double precio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemId      = getArguments().getInt(ARG_ITEM_ID, -1);
            tipo        = getArguments().getString(ARG_TIPO, "CURSO");
            titulo      = getArguments().getString(ARG_TITULO, "");
            precio      = getArguments().getFloat(ARG_PRECIO, 0f);
            descripcion = getArguments().getString(ARG_DESCRIPCION, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_comprar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitulo      = view.findViewById(R.id.tv_comprar_titulo);
        tvPrecio      = view.findViewById(R.id.tv_comprar_precio);
        tvDescripcion = view.findViewById(R.id.tv_comprar_descripcion);
        tvTipo        = view.findViewById(R.id.tv_comprar_tipo);
        btnPagar      = view.findViewById(R.id.btn_pagar_paypal);
        progressBar   = view.findViewById(R.id.progress_comprar);

        tvTitulo.setText(titulo);
        tvPrecio.setText(String.format("$%.2f MXN", precio));
        tvDescripcion.setText(descripcion);
        tvTipo.setText("CURSO".equals(tipo) ? "📚 Curso" : "🎓 Clase 1a1");

        btnPagar.setOnClickListener(v -> iniciarPagoPaypal());
        view.findViewById(R.id.btn_cancelar_compra)

                .setOnClickListener(v -> requireActivity().onBackPressed());
        // Botón atrás del toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar =
                view.findViewById(R.id.toolbar_comprar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v ->
                    requireActivity().onBackPressed()
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() == null || getActivity().getIntent() == null) return;

        Uri uri = getActivity().getIntent().getData();
        if (uri != null
                && PAYPAL_RETURN_SCHEME.equals(uri.getScheme())
                && PAYPAL_RETURN_HOST.equals(uri.getHost())) {

            String paymentId = uri.getQueryParameter("paymentId");
            String payerId   = uri.getQueryParameter("PayerID");

            if (paymentId != null && payerId != null) {
                // Recuperar datos desde SharedPreferences
                SharedPreferences prefs = requireContext()
                        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int    savedItemId   = prefs.getInt(KEY_ITEM_ID, -1);
                String savedTipo     = prefs.getString(KEY_TIPO, "CURSO");
                int    savedUsuario  = prefs.getInt(KEY_USUARIO, -1);

                // Si hay datos guardados usarlos, sino usar los del fragment
                int    resolvedItemId  = savedItemId  > 0 ? savedItemId  : itemId;
                String resolvedTipo    = savedTipo    != null ? savedTipo : tipo;
                int    resolvedUsuario = savedUsuario > 0 ? savedUsuario
                        : SessionManager.getInstance(requireContext()).getUsuarioId();

                confirmarPago(paymentId, payerId, resolvedUsuario, resolvedItemId, resolvedTipo);
                getActivity().setIntent(new Intent());
                // Limpiar prefs después de usar
                prefs.edit().clear().apply();
            }
        }
    }

    // ── Paso 1: Crear orden ──────────────────────────────────────────────────

    private void iniciarPagoPaypal() {
        int usuarioId = SessionManager.getInstance(requireContext()).getUsuarioId();
        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Guardar en SharedPreferences ANTES de abrir el navegador
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_ITEM_ID,  itemId)
                .putString(KEY_TIPO,  tipo)
                .putInt(KEY_USUARIO,  usuarioId)
                .apply();

        PaypalRequestDto request = new PaypalRequestDto(usuarioId, itemId, tipo);

        RetrofitClient.getApiService().iniciarPago(request)
                .enqueue(new Callback<PaypalResponseDto>() {
                    @Override
                    public void onResponse(@NonNull Call<PaypalResponseDto> call,
                                           @NonNull Response<PaypalResponseDto> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            abrirPaypalEnNavegador(response.body().getApprovalUrl());
                        } else {
                            Toast.makeText(requireContext(),
                                    "Error al crear el pago (" + response.code() + ")",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<PaypalResponseDto> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(requireContext(),
                                "Sin conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Paso 2: Abrir PayPal ─────────────────────────────────────────────────

    private void abrirPaypalEnNavegador(String approvalUrl) {
        if (approvalUrl == null || approvalUrl.isEmpty()) {
            Toast.makeText(requireContext(), "URL de pago inválida", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(approvalUrl)));
    }

    // ── Paso 3: Confirmar pago ───────────────────────────────────────────────

    private void confirmarPago(String paymentId, String payerId,
                               int usuarioId, int resolvedItemId, String resolvedTipo) {
        setLoading(true);

        RetrofitClient.getApiService()
                .confirmarPago(paymentId, payerId, usuarioId, resolvedItemId, resolvedTipo)
                .enqueue(new Callback<PaypalResponseDto>() {
                    @Override
                    public void onResponse(@NonNull Call<PaypalResponseDto> call,
                                           @NonNull Response<PaypalResponseDto> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarExitoPago();
                        } else {
                            Toast.makeText(requireContext(),
                                    "No se pudo confirmar el pago.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<PaypalResponseDto> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(requireContext(),
                                "Error de red al confirmar: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Éxito ────────────────────────────────────────────────────────────────

    private void mostrarExitoPago() {
        btnPagar.setEnabled(false);
        btnPagar.setText("✅ Pago completado");
        Toast.makeText(requireContext(),
                "¡Pago exitoso! Tu compra ha sido registrada.", Toast.LENGTH_LONG).show();
        btnPagar.postDelayed(() -> {
            if (getActivity() != null) requireActivity().onBackPressed();
        }, 2000);
    }

    private void setLoading(boolean loading) {
        if (progressBar != null)
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (btnPagar != null)
            btnPagar.setEnabled(!loading);
    }
}
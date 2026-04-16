package com.example.saber_share.fragmentos.contenido;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.saber_share.R;
import com.example.saber_share.model.CursoDto;
import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.util.api.CursoApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.ServicioApi;
import com.example.saber_share.util.api.UsuarioApi;
import com.example.saber_share.util.local.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Perfil extends Fragment {

    private TextView tvNombre, tvEmail, tvRol, tvInicial;
    private TextView tvNPublicaciones, tvNVentas, tvCalificacion;
    private TextView tvSaldo;
    private Button   btnCerrarSesion;
    private View     cardHistorial, cardEstadisticas, cardAgenda, cardSaldo;

    private SessionManager sessionManager;

    public Perfil() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());

        tvInicial        = view.findViewById(R.id.tv_perfil_inicial);
        tvNombre         = view.findViewById(R.id.tv_perfil_nombre);
        tvEmail          = view.findViewById(R.id.tv_perfil_email);
        tvRol            = view.findViewById(R.id.tv_perfil_rol);
        tvNPublicaciones = view.findViewById(R.id.tv_perfil_n_publicaciones);
        tvNVentas        = view.findViewById(R.id.tv_perfil_n_ventas);
        tvCalificacion   = view.findViewById(R.id.tv_perfil_calificacion);
        cardHistorial    = view.findViewById(R.id.card_historial);
        cardEstadisticas = view.findViewById(R.id.card_estadisticas);
        cardAgenda       = view.findViewById(R.id.card_agenda);
        cardSaldo        = view.findViewById(R.id.card_saldo);
        tvSaldo          = view.findViewById(R.id.tv_saldo_pendiente);
        btnCerrarSesion  = view.findViewById(R.id.btn_cerrar_sesion);

        cargarDatosUsuario();
        cargarContadorPublicaciones();

        // Cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> {
            sessionManager.cerrarSesion();
            Navigation.findNavController(v).navigate(R.id.cuenta_nav);
        });

        // Historial (todos los usuarios)
        cardHistorial.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_historialFragment));

        // Solo vendedor/admin
        boolean esVendedor = sessionManager.esChalan() || sessionManager.esAdmin();
        cardEstadisticas.setVisibility(esVendedor ? View.VISIBLE : View.GONE);
        cardAgenda.setVisibility(esVendedor ? View.VISIBLE : View.GONE);
        if (cardSaldo != null) cardSaldo.setVisibility(esVendedor ? View.VISIBLE : View.GONE);

        if (esVendedor) {
            cardEstadisticas.setOnClickListener(v ->
                    Navigation.findNavController(v)
                            .navigate(R.id.action_perfil_to_estadisticasFragment));

            cardAgenda.setOnClickListener(v ->
                    Navigation.findNavController(v)
                            .navigate(R.id.action_perfil_to_misPublicaciones));

            cargarSaldo();

            if (cardSaldo != null)
                cardSaldo.setOnClickListener(v -> mostrarDialogCorreoPaypal());
        }
    }

    // ── Datos de sesión ──────────────────────────────────────────────────────

    private void cargarDatosUsuario() {
        String nombre = sessionManager.getNombre();
        String email  = sessionManager.getEmail();
        String rol    = sessionManager.getRol();

        if (nombre == null || nombre.trim().isEmpty()) nombre = "Usuario";
        if (email  == null) email = "";
        if (rol    == null) rol   = "USUARIO";

        tvNombre.setText(nombre);
        tvEmail.setText(email);
        tvRol.setText(rol);
        tvInicial.setText(String.valueOf(nombre.charAt(0)).toUpperCase());
    }

    // ── Contador publicaciones propias ───────────────────────────────────────

    private void cargarContadorPublicaciones() {
        int userId = sessionManager.getUsuarioId();
        if (userId <= 0) return;

        final int[] cursosCount    = {0};
        final int[] serviciosCount = {0};
        final int[] pendientes     = {2};

        Runnable actualizar = () -> {
            pendientes[0]--;
            if (pendientes[0] == 0) {
                int total = cursosCount[0] + serviciosCount[0];
                if (tvNPublicaciones != null) tvNPublicaciones.setText(String.valueOf(total));
                if (tvNVentas        != null) tvNVentas.setText(String.valueOf(cursosCount[0]));
                if (tvCalificacion   != null) tvCalificacion.setText("★ 0.0");
            }
        };

        RetrofitClient.getInstance().create(CursoApi.class)
                .lista().enqueue(new Callback<List<CursoDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CursoDto>> call,
                                           @NonNull Response<List<CursoDto>> response) {
                        if (response.isSuccessful() && response.body() != null)
                            for (CursoDto c : response.body())
                                if (c.getUsuarioId() != null && c.getUsuarioId() == userId)
                                    cursosCount[0]++;
                        actualizar.run();
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<CursoDto>> call, @NonNull Throwable t) {
                        actualizar.run();
                    }
                });

        RetrofitClient.getInstance().create(ServicioApi.class)
                .lista().enqueue(new Callback<List<ServicioDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ServicioDto>> call,
                                           @NonNull Response<List<ServicioDto>> response) {
                        if (response.isSuccessful() && response.body() != null)
                            for (ServicioDto s : response.body())
                                if (s.getUsuarioId() != null && s.getUsuarioId() == userId)
                                    serviciosCount[0]++;
                        actualizar.run();
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<ServicioDto>> call, @NonNull Throwable t) {
                        actualizar.run();
                    }
                });
    }

    // ── Saldo pendiente ──────────────────────────────────────────────────────

    private void cargarSaldo() {
        int userId = sessionManager.getUsuarioId();
        if (userId <= 0) return;

        RetrofitClient.getInstance().create(UsuarioApi.class)
                .getSaldo(userId).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call,
                                           @NonNull Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null && tvSaldo != null) {
                            Object saldo = response.body().get("saldoPendiente");
                            double valor = saldo != null
                                    ? Double.parseDouble(saldo.toString()) : 0.0;
                            tvSaldo.setText(String.format("$%.2f USD por cobrar", valor));
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call,
                                          @NonNull Throwable t) {}
                });
    }

    // ── Dialog correo PayPal (con EditText simple, sin XML externo) ──────────

    private void mostrarDialogCorreoPaypal() {
        EditText etCorreo = new EditText(requireContext());
        etCorreo.setHint("tu@paypal.com");
        etCorreo.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                | android.text.InputType.TYPE_CLASS_TEXT);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        etCorreo.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(requireContext())
                .setTitle("💰 Correo PayPal")
                .setMessage("Registra el correo donde recibirás tus pagos")
                .setView(etCorreo)
                .setPositiveButton("Guardar", (d, w) -> {
                    String correo = etCorreo.getText().toString().trim();
                    if (correo.isEmpty()) {
                        Toast.makeText(getContext(), "Ingresa un correo",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    guardarCorreoPaypal(correo);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarCorreoPaypal(String correo) {
        int userId = sessionManager.getUsuarioId();
        Map<String, String> body = new HashMap<>();
        body.put("correoPaypal", correo);

        RetrofitClient.getInstance().create(UsuarioApi.class)
                .actualizarCorreoPaypal(userId, body)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call,
                                           @NonNull Response<Map<String, Object>> response) {
                        Toast.makeText(getContext(),
                                response.isSuccessful()
                                        ? "✅ Correo PayPal guardado"
                                        : "Error al guardar",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Error de red",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
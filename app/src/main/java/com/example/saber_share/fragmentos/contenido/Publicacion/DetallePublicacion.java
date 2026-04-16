package com.example.saber_share.fragmentos.contenido.Publicacion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.contenido.Comprar;
import com.example.saber_share.fragmentos.dialogs.CalificarDialog;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.model.OpinionServicioDto;
import com.example.saber_share.model.OpinionesCursoDto;
import com.example.saber_share.util.api.HistorialApi;
import com.example.saber_share.util.api.OpinionApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePublicacion extends Fragment {

    private TextView  tvTitulo, tvDescripcion, tvPrecio, tvPromedioEstrellas,
            tvTotalReviews, tvVendedor, tvVendedorInicial, tvTipo;
    private RatingBar ratingBar;
    private Button    btnComprar, btnCalificar, btnContactar, btnAgendar;

    private int    itemId;
    private int    autorId;
    private String tipo, titulo, descripcion, autorNombre, contenidoUrl;
    private double precio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemId       = getArguments().getInt("itemId");
            tipo         = getArguments().getString("tipo", "CURSO");
            titulo       = getArguments().getString("titulo", "");
            descripcion  = getArguments().getString("descripcion", "");
            precio       = getArguments().getDouble("precio", 0.0);
            autorId      = getArguments().getInt("autorId", -1);
            autorNombre  = getArguments().getString("autorNombre", "Vendedor");
            contenidoUrl = getArguments().getString("contenidoUrl", ""); // ← NUEVO
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_detalle_publicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitulo            = view.findViewById(R.id.tv_detalle_titulo);
        tvDescripcion       = view.findViewById(R.id.tv_detalle_descripcion);
        tvPrecio            = view.findViewById(R.id.tv_detalle_precio);
        tvPromedioEstrellas = view.findViewById(R.id.tv_detalle_promedio);
        tvTotalReviews      = view.findViewById(R.id.tv_detalle_total_reviews);
        tvVendedor          = view.findViewById(R.id.tv_detalle_vendedor);
        tvVendedorInicial   = view.findViewById(R.id.tv_detalle_vendedor_inicial);
        tvTipo              = view.findViewById(R.id.tv_detalle_tipo);
        ratingBar           = view.findViewById(R.id.ratingbar_detalle);
        btnComprar          = view.findViewById(R.id.btn_comprar);
        btnCalificar        = view.findViewById(R.id.btn_calificar);
        btnContactar        = view.findViewById(R.id.btn_contactar);
        btnAgendar          = view.findViewById(R.id.btn_agendar);

        // Botón atrás del toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar =
                view.findViewById(R.id.toolbar_detalle);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v ->
                    Navigation.findNavController(v).navigateUp());
        }

        tvTitulo.setText(titulo);
        tvDescripcion.setText(descripcion);
        tvPrecio.setText(String.format("$%.2f MXN", precio));
        if (tvVendedor      != null) tvVendedor.setText(autorNombre);
        if (tvVendedorInicial != null)
            tvVendedorInicial.setText(autorNombre.isEmpty() ? "V"
                    : String.valueOf(autorNombre.charAt(0)).toUpperCase());
        if (tvTipo != null)
            tvTipo.setText("CURSO".equals(tipo) ? "CURSO" : "CLASE 1A1");

        if (btnAgendar   != null) btnAgendar.setVisibility(View.GONE);
        if (btnCalificar != null) btnCalificar.setVisibility(View.GONE);

        cargarEstadisticas();
        configurarBtnComprar();
        verificarAcceso();
    }

    @Override
    public void onStart() {
        super.onStart();
        verificarAcceso();
    }

    // ── Verificar si ya compró ───────────────────────────────────────────────

    private void verificarAcceso() {
        int usuarioId = SessionManager.getInstance(requireContext()).getUsuarioId();
        if (usuarioId <= 0) return;

        RetrofitClient.getInstance().create(HistorialApi.class)
                .historialPorUsuario(usuarioId).enqueue(new Callback<List<HistorialDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<HistorialDto>> call,
                                           @NonNull Response<List<HistorialDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            boolean yaCompro = false;
                            for (HistorialDto h : response.body()) {
                                if ("CURSO".equals(tipo) && h.getCursoId() != null
                                        && h.getCursoId() == itemId) { yaCompro = true; break; }
                                if (!"CURSO".equals(tipo) && h.getServicioId() != null
                                        && h.getServicioId() == itemId) { yaCompro = true; break; }
                            }
                            if (yaCompro) mostrarAccesoDesbloqueado();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<HistorialDto>> call,
                                          @NonNull Throwable t) {}
                });
    }

    // ── Acceso desbloqueado ──────────────────────────────────────────────────

    private void mostrarAccesoDesbloqueado() {
        if (btnComprar == null) return;

        // Si es CURSO y tiene URL de contenido → botón para abrir el contenido
        if ("CURSO".equals(tipo) && contenidoUrl != null && !contenidoUrl.isEmpty()) {
            btnComprar.setText("📂 Ver contenido del curso");
            btnComprar.setOnClickListener(v -> {
                if (contenidoUrl.startsWith("content://")) {
                    Toast.makeText(requireContext(),
                            "El vendedor debe actualizar el curso con un link web (Google Drive, YouTube, etc.)",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    String url = contenidoUrl;
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(),
                            "No se pudo abrir el contenido", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Sin URL o es CLASE → chatear con vendedor
            btnComprar.setText("💬 Chatear con vendedor");
            btnComprar.setOnClickListener(v -> abrirChat());
        }

        // Mostrar calificar
        if (btnCalificar != null) {
            btnCalificar.setVisibility(View.VISIBLE);
            btnCalificar.setOnClickListener(v -> {
                CalificarDialog dialog = CalificarDialog.newInstance(itemId, tipo);
                dialog.setListener(() -> cargarEstadisticas());
                dialog.show(getParentFragmentManager(), "calificar");
            });
        }

        // Si es CLASE → mostrar botón agendar
        if (!"CURSO".equals(tipo) && btnAgendar != null) {
            btnAgendar.setVisibility(View.VISIBLE);
            btnAgendar.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("servicioId", itemId);
                args.putString("titulo",  titulo);
                args.putFloat("precio",   (float) precio);
                args.putInt("profesorId", autorId);
                Navigation.findNavController(v).navigate(R.id.action_detalle_to_agendar, args);
            });
        }

        if (tvPrecio != null) tvPrecio.setText("✅ Contenido desbloqueado");
        if (btnContactar != null) btnContactar.setVisibility(View.GONE);
    }

    // ── Chat con vendedor ────────────────────────────────────────────────────

    private void abrirChat() {
        if (autorId <= 0) {
            Toast.makeText(requireContext(), "No se encontró al vendedor",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle();
        args.putInt("receptorId",        autorId);
        args.putString("receptorNombre", autorNombre);
        Navigation.findNavController(requireView()).navigate(R.id.chatFragment, args);
    }

    // ── Botón comprar (estado inicial) ───────────────────────────────────────

    private void configurarBtnComprar() {
        if (btnComprar == null) return;
        btnComprar.setText("Comprar con PayPal");
        btnComprar.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt(Comprar.ARG_ITEM_ID,        itemId);
            args.putString(Comprar.ARG_TIPO,        tipo);
            args.putString(Comprar.ARG_TITULO,      titulo);
            args.putFloat(Comprar.ARG_PRECIO,       (float) precio);
            args.putString(Comprar.ARG_DESCRIPCION, descripcion);
            Navigation.findNavController(v).navigate(R.id.action_detalle_to_comprar, args);
        });

        if (btnContactar != null) {
            btnContactar.setOnClickListener(v ->
                    Toast.makeText(requireContext(),
                            "Compra el contenido para chatear", Toast.LENGTH_SHORT).show());
        }
    }

    // ── Calificaciones ───────────────────────────────────────────────────────

    private void cargarEstadisticas() {
        OpinionApi api = RetrofitClient.getInstance().create(OpinionApi.class);
        if ("CURSO".equals(tipo)) {
            api.getOpinionesCurso(itemId).enqueue(new Callback<List<OpinionesCursoDto>>() {
                @Override
                public void onResponse(@NonNull Call<List<OpinionesCursoDto>> call,
                                       @NonNull Response<List<OpinionesCursoDto>> response) {
                    if (response.isSuccessful() && response.body() != null)
                        procesarOpinionesCurso(response.body());
                }
                @Override public void onFailure(@NonNull Call<List<OpinionesCursoDto>> call,
                                                @NonNull Throwable t) {}
            });
        } else {
            api.getOpinionesServicio(itemId).enqueue(new Callback<List<OpinionServicioDto>>() {
                @Override
                public void onResponse(@NonNull Call<List<OpinionServicioDto>> call,
                                       @NonNull Response<List<OpinionServicioDto>> response) {
                    if (response.isSuccessful() && response.body() != null)
                        procesarOpinionesServicio(response.body());
                }
                @Override public void onFailure(@NonNull Call<List<OpinionServicioDto>> call,
                                                @NonNull Throwable t) {}
            });
        }
    }

    private void procesarOpinionesCurso(List<OpinionesCursoDto> lista) {
        if (lista.isEmpty()) return;
        double suma = 0;
        for (OpinionesCursoDto o : lista)
            if (o.getCalOps() != null) suma += o.getCalOps();
        mostrarEstrellas(suma / lista.size(), lista.size());
    }

    private void procesarOpinionesServicio(List<OpinionServicioDto> lista) {
        if (lista.isEmpty()) return;
        double suma = 0;
        for (OpinionServicioDto o : lista)
            if (o.getCalOps() != null) suma += o.getCalOps();
        mostrarEstrellas(suma / lista.size(), lista.size());
    }

    private void mostrarEstrellas(double promedio, int total) {
        if (tvPromedioEstrellas != null)
            tvPromedioEstrellas.setText(String.format("%.1f", promedio));
        if (ratingBar != null) ratingBar.setRating((float) promedio);
        if (tvTotalReviews != null) tvTotalReviews.setText(total + " reseñas");
    }
}
package com.example.saber_share.fragmentos.contenido.Publicacion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saber_share.R;
import com.example.saber_share.model.CursoDto;
import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.util.api.CursoApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.ServicioApi;
import com.example.saber_share.util.local.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResumenPublicacion extends Fragment {

    private String tipo, titulo, descripcion, extra;
    private double precio;

    // Campos parseados de "extra" para CLASE
    private String modalidad = "";
    private String duracion  = "";
    private String fecha     = "";
    private String hora      = "";

    private SessionManager sessionManager;
    private Button btnConfirmar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tipo        = getArguments().getString("tipo", "CURSO");
            titulo      = getArguments().getString("titulo", "");
            descripcion = getArguments().getString("descripcion", "");
            precio      = getArguments().getDouble("precio", 0.0);
            extra       = getArguments().getString("extra", "");
        }

        // Parsear extra para CLASE: "EN_LINEA|60 min|2026-03-15|14:30"
        if ("CLASE".equals(tipo) && extra != null && extra.contains("|")) {
            String[] partes = extra.split("\\|", -1);
            modalidad = partes.length > 0 ? partes[0] : "";
            duracion  = partes.length > 1 ? partes[1] : "";
            fecha     = partes.length > 2 ? partes[2] : "";
            hora      = partes.length > 3 ? partes[3] : "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_vender_resumen_publicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = SessionManager.getInstance(requireContext());

        TextView tvTipo        = view.findViewById(R.id.tvTipo);
        TextView tvTituloVal   = view.findViewById(R.id.tvTituloValor);
        TextView tvDescVal     = view.findViewById(R.id.tvDescripcionValor);
        TextView tvPrecioVal   = view.findViewById(R.id.tvPrecioValor);
        TextView tvExtraLabel  = view.findViewById(R.id.tvExtraLabel);
        TextView tvExtraVal    = view.findViewById(R.id.tvExtraValor);

        tvTituloVal.setText(titulo);
        tvDescVal.setText(descripcion);
        tvPrecioVal.setText(String.format("$ %.2f MXN", precio));

        if ("CLASE".equals(tipo)) {
            tvTipo.setText("Tipo: Clase 1 a 1");

            // Mostrar detalles de la clase en el campo extra
            String modalidadLabel = "EN_LINEA".equals(modalidad) ? "💻 En línea" : "📍 Presencial";
            StringBuilder sb = new StringBuilder();
            sb.append(modalidadLabel);
            if (!duracion.isEmpty()) sb.append("\nDuración: ").append(duracion);
            if (!fecha.isEmpty())    sb.append("\nFecha: ").append(fecha);
            if (!hora.isEmpty())     sb.append("\nHora: ").append(hora);

            tvExtraLabel.setText("Detalles:");
            tvExtraVal.setText(sb.toString());
        } else {
            tvTipo.setText("Tipo: Curso (Pregrabado)");
            tvExtraLabel.setText("Archivo:");
            tvExtraVal.setText(!extra.isEmpty() ? extra : "Sin archivo seleccionado");
        }

        btnConfirmar = view.findViewById(R.id.btnConfirmar);
        Button btnRegresar = view.findViewById(R.id.btnRegresar);

        btnRegresar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        btnConfirmar.setOnClickListener(v -> publicarEnServidor());
    }

    private void publicarEnServidor() {
        btnConfirmar.setEnabled(false);
        btnConfirmar.setText("Publicando...");

        int userId = sessionManager.getUsuarioId();
        if (userId == -1) {
            Toast.makeText(getContext(), "Error de sesión. Vuelve a ingresar.", Toast.LENGTH_SHORT).show();
            btnConfirmar.setEnabled(true);
            btnConfirmar.setText("Confirmar publicación");
            return;
        }

        if ("CURSO".equals(tipo)) registrarCurso(userId);
        else                      registrarClase(userId);
    }

    private void registrarCurso(int userId) {
        CursoDto curso = new CursoDto();
        curso.setTitulo(titulo);
        curso.setDescripcion(descripcion);
        curso.setPrecio(precio);
        curso.setFoto(extra);          // nombre del archivo
        curso.setUsuarioId(userId);
        curso.setCalificacion("0");

        RetrofitClient.getInstance().create(CursoApi.class)
                .crearCurso(curso).enqueue(new Callback<CursoDto>() {
                    @Override
                    public void onResponse(@NonNull Call<CursoDto> call,
                                           @NonNull Response<CursoDto> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "¡Curso publicado exitosamente!", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.action_resumenPublicacion_to_inicio);
                        } else {
                            mostrarError("Error al publicar curso: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<CursoDto> call, @NonNull Throwable t) {
                        mostrarError("Fallo de conexión: " + t.getMessage());
                    }
                });
    }

    private void registrarClase(int userId) {
        ServicioDto servicio = new ServicioDto();
        servicio.setTitulo(titulo);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);
        servicio.setRequisitos(modalidad);   // guardamos modalidad en requisitos
        servicio.setUsuarioId(userId);
        servicio.setFecha(fecha);
        servicio.setHora(hora.isEmpty() ? "00:00:00" : hora + ":00");

        RetrofitClient.getInstance().create(ServicioApi.class)
                .crearServicio(servicio).enqueue(new Callback<ServicioDto>() {
                    @Override
                    public void onResponse(@NonNull Call<ServicioDto> call,
                                           @NonNull Response<ServicioDto> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "¡Clase publicada exitosamente!", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.action_resumenPublicacion_to_inicio);
                        } else {
                            mostrarError("Error al publicar clase: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ServicioDto> call, @NonNull Throwable t) {
                        mostrarError("Fallo de conexión: " + t.getMessage());
                    }
                });
    }

    private void mostrarError(String msg) {
        btnConfirmar.setEnabled(true);
        btnConfirmar.setText("Confirmar publicación");
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
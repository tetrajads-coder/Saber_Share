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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class ResumenPublicacion extends Fragment {

    private String tipo, titulo, descripcion, extra;
    private double precio;

    private SessionManager sessionManager;
    private Button btnConfirmar;

    // 1. Recuperar datos al crear el fragmento
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tipo = getArguments().getString("tipo");
            titulo = getArguments().getString("titulo");
            descripcion = getArguments().getString("descripcion");
            precio = getArguments().getDouble("precio");
            extra = getArguments().getString("extra");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_vender_resumen_publicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        // 2. Llenar la tarjeta con la información
        TextView tvTipo = view.findViewById(R.id.tvTipo);
        TextView tvTitulo = view.findViewById(R.id.tvTituloValor);
        TextView tvDesc = view.findViewById(R.id.tvDescripcionValor);
        TextView tvPrecio = view.findViewById(R.id.tvPrecioValor);
        TextView tvExtraLabel = view.findViewById(R.id.tvExtraLabel);
        TextView tvExtraVal = view.findViewById(R.id.tvExtraValor);

        tvTitulo.setText(titulo);
        tvDesc.setText(descripcion);
        tvPrecio.setText(String.format("$ %.2f MXN", precio));
        tvExtraVal.setText(extra);

        // Personalizar etiquetas según lo que estamos vendiendo
        if ("CLASE".equals(tipo)) {
            tvTipo.setText("Tipo: Clase 1 a 1");
            tvExtraLabel.setText("Requisitos:");
        } else {
            tvTipo.setText("Tipo: Curso (Pregrabado)");
            tvExtraLabel.setText("Archivo:");
        }

        // 3. Configurar botones
        btnConfirmar = view.findViewById(R.id.btnConfirmar);
        Button btnRegresar = view.findViewById(R.id.btnRegresar);

        // Volver atrás si el usuario quiere editar algo
        btnRegresar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // Confirmar y enviar al servidor
        btnConfirmar.setOnClickListener(v -> publicarEnServidor());
    }

    private void publicarEnServidor() {
        // Bloqueamos el botón para evitar doble envío
        btnConfirmar.setEnabled(false);
        btnConfirmar.setText("Publicando...");

        // Obtenemos el ID del usuario logueado (Vital para la base de datos)
        int userId = sessionManager.getUserId();

        if (userId == -1) {
            Toast.makeText(getContext(), "Error de sesión. Vuelve a ingresar.", Toast.LENGTH_SHORT).show();
            btnConfirmar.setEnabled(true);
            return;
        }

        // Decidimos a qué endpoint llamar
        if ("CURSO".equals(tipo)) {
            registrarCurso(userId);
        } else {
            registrarClase(userId);
        }
    }

    private void registrarCurso(int userId) {
        CursoDto curso = new CursoDto();
        curso.setTitulo(titulo);
        curso.setDescripcion(descripcion);
        curso.setPrecio(precio);
        curso.setFoto(extra); // Aquí guardamos la ruta del archivo como "foto"
        curso.setUsuarioId(userId);
        curso.setCalificacion("0"); // Calificación inicial

        CursoApi api = RetrofitClient.getClient().create(CursoApi.class);
        api.crearCurso(curso).enqueue(new Callback<CursoDto>() {
            @Override
            public void onResponse(Call<CursoDto> call, Response<CursoDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Curso publicado exitosamente!", Toast.LENGTH_LONG).show();
                    // Navegar al inicio y borrar el historial de pantallas de venta
                    Navigation.findNavController(requireView()).navigate(R.id.action_resumenPublicacion_to_inicio);
                } else {
                    mostrarError("Error al publicar curso");
                }
            }
            @Override
            public void onFailure(Call<CursoDto> call, Throwable t) {
                mostrarError("Fallo de conexión: " + t.getMessage());
            }
        });
    }

    private void registrarClase(int userId) {
        ServicioDto servicio = new ServicioDto();
        servicio.setTitulo(titulo);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);
        servicio.setRequisitos(extra); // Aquí guardamos los requisitos
        servicio.setUsuarioId(userId);

        // Fechas automáticas (porque la BD las exige NOT NULL)
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        servicio.setFecha(fechaHoy);
        servicio.setHora(horaActual);

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        api.crearServicio(servicio).enqueue(new Callback<ServicioDto>() {
            @Override
            public void onResponse(Call<ServicioDto> call, Response<ServicioDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Clase publicada exitosamente!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_resumenPublicacion_to_inicio);
                } else {
                    mostrarError("Error al publicar clase");
                }
            }
            @Override
            public void onFailure(Call<ServicioDto> call, Throwable t) {
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
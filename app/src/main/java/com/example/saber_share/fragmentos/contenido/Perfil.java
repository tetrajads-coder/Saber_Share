package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.example.saber_share.R;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.util.api.HistorialApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Perfil extends Fragment implements View.OnClickListener {

    private TextView tvNombrePerfil, tvCorreoPerfil, tvCountCursos, tvCountClases;
    private EditText etNombrePublico, etCorreoPublico;
    private Button btnCerrarSesion, btnHistorial, btnEstadisticas, btnGestionarTarjetas;

    private SessionManager sessionManager;

    public Perfil() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        // --- UI ---
        tvNombrePerfil       = view.findViewById(R.id.tvNombrePerfil);
        tvCorreoPerfil       = view.findViewById(R.id.tvCorreoPerfil);
        tvCountCursos        = view.findViewById(R.id.tvCountCursos);
        tvCountClases        = view.findViewById(R.id.tvCountClases);
        etNombrePublico      = view.findViewById(R.id.etNombrePublico);
        etCorreoPublico      = view.findViewById(R.id.etCorreoPublico);
        btnCerrarSesion      = view.findViewById(R.id.btnCerrarSesion);
        btnHistorial         = view.findViewById(R.id.btnHistorial);
        btnEstadisticas      = view.findViewById(R.id.btnEstadisticas);
        btnGestionarTarjetas = view.findViewById(R.id.btnGestionarTarjetas);

        // Listeners
        btnCerrarSesion.setOnClickListener(this);
        btnHistorial.setOnClickListener(this);
        btnEstadisticas.setOnClickListener(this);
        btnGestionarTarjetas.setOnClickListener(this);

        // Cargar info
        cargarDatosUsuario();
        cargarContadoresDesdeHistorial();
    }

    private void cargarDatosUsuario() {
        String nombre = sessionManager.getNombre();
        HashMap<String, String> datos = sessionManager.getUserDetails();
        String correoOUser = datos.get("usuario");

        if (nombre == null || nombre.trim().isEmpty()) nombre = "Usuario";
        if (correoOUser == null) correoOUser = "";

        tvNombrePerfil.setText(nombre);
        tvCorreoPerfil.setText(correoOUser);
        etNombrePublico.setText(nombre);
        etCorreoPublico.setText(correoOUser);
    }

    private void cargarContadoresDesdeHistorial() {
        int idUsuario = sessionManager.getUserId();
        if (idUsuario <= 0) {
            tvCountCursos.setText("0");
            tvCountClases.setText("0");
            return;
        }

        HistorialApi api = RetrofitClient.getClient().create(HistorialApi.class);
        api.historialPorUsuario(idUsuario).enqueue(new Callback<List<HistorialDto>>() {
            @Override
            public void onResponse(Call<List<HistorialDto>> call, Response<List<HistorialDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvCountCursos.setText("0");
                    tvCountClases.setText("0");
                    return;
                }

                int cursos = 0;
                int clases = 0;

                for (HistorialDto h : response.body()) {
                    if (h.getCursoId() != null) cursos++;
                    if (h.getServicioId() != null) clases++;
                }

                tvCountCursos.setText(String.valueOf(cursos));
                tvCountClases.setText(String.valueOf(clases));
            }

            @Override
            public void onFailure(Call<List<HistorialDto>> call, Throwable t) {
                tvCountCursos.setText("0");
                tvCountClases.setText("0");
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnCerrarSesion) {
            sessionManager.logoutUser();

        } else if (id == R.id.btnHistorial) {
            // Navega al fragmento de historial
            Navigation.findNavController(v)
                    .navigate(R.id.action_perfil_to_historialFragment);

        } else if (id == R.id.btnEstadisticas) {
            Toast.makeText(getContext(), "Pantalla de estadisticas pendiente", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.btnGestionarTarjetas) {
            Toast.makeText(getContext(), "Pantalla de metodos de pago pendiente", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.example.saber_share.fragmentos.contenido.Publicacion;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.contenido.adapter.AgendaAdapter;
import com.example.saber_share.model.AgendaDto;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.util.api.AgendaApi;
import com.example.saber_share.util.api.HistorialApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgendarClase extends Fragment {

    private RecyclerView rvHorarios;
    private TextView tvSubtitulo, tvVacio;
    private SessionManager sessionManager;
    private double precioClase;

    private int servicioId;
    private int profesorId;
    private String tituloServicio;

    public AgendarClase() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_agendar_clase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        rvHorarios = view.findViewById(R.id.rvHorarios);
        tvSubtitulo = view.findViewById(R.id.tvSubtituloServicio);
        tvVacio = view.findViewById(R.id.tvSinHorarios);

        if (getArguments() != null) {
            servicioId = getArguments().getInt("servicioId", -1);
            tituloServicio = getArguments().getString("titulo", "Clase");
            precioClase = getArguments().getDouble("precio", 0.0);
            profesorId = getArguments().getInt("profesorId", -1);

            tvSubtitulo.setText("Horarios para: " + tituloServicio);
        }
        rvHorarios.setLayoutManager(new LinearLayoutManager(getContext()));
        cargarHorariosDisponibles();
    }

    private void cargarHorariosDisponibles() {
        AgendaApi api = RetrofitClient.getClient().create(AgendaApi.class);
        api.getSlotsPorServicio(servicioId).enqueue(new Callback<List<AgendaDto>>() {
            @Override
            public void onResponse(Call<List<AgendaDto>> call, Response<List<AgendaDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AgendaDto> disponibles = new ArrayList<>();
                    for (AgendaDto slot : response.body()) {
                        if ("DISPONIBLE".equalsIgnoreCase(slot.getEstado())) {
                            disponibles.add(slot);
                        }
                    }
                    actualizarUI(disponibles);
                } else {
                    mostrarMensaje("No se pudieron cargar los horarios");
                }
            }
            @Override public void onFailure(Call<List<AgendaDto>> call, Throwable t) {
                mostrarMensaje("Error de conexión");
            }
        });
    }

    private void actualizarUI(List<AgendaDto> lista) {
        if (lista.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            rvHorarios.setVisibility(View.GONE);
        } else {
            tvVacio.setVisibility(View.GONE);
            rvHorarios.setVisibility(View.VISIBLE);
            AgendaAdapter adapter = new AgendaAdapter(lista, this::confirmarReserva);
            rvHorarios.setAdapter(adapter);
        }
    }

    private void confirmarReserva(AgendaDto slot) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar Reserva")
                .setMessage("¿Deseas reservar la clase para el " + slot.getFecha() + " a las " + slot.getHora() + "?")
                .setPositiveButton("Sí, Agendar", (dialog, which) -> realizarReserva(slot))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void realizarReserva(AgendaDto slot) {
        int miIdAlumno = sessionManager.getUserId();
        if(miIdAlumno == profesorId) {
            mostrarMensaje("No puedes reservar tu propia clase");
            return;
        }

        AgendaApi api = RetrofitClient.getClient().create(AgendaApi.class);
        api.reservarSlot(slot.getIdAgenda(), miIdAlumno).enqueue(new Callback<AgendaDto>() {
            @Override
            public void onResponse(Call<AgendaDto> call, Response<AgendaDto> response) {
                if (response.isSuccessful()) {
                    guardarEnHistorial(slot);
                } else {
                    mostrarMensaje("Error al reservar: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<AgendaDto> call, Throwable t) {
                mostrarMensaje("Fallo de red al reservar");
            }
        });
    }

    private void guardarEnHistorial(AgendaDto slot) {
        HistorialDto historial = new HistorialDto();
        historial.setFechapago(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        historial.setPago(precioClase);
        historial.setUsuario_idUsuario(sessionManager.getUserId());

        historial.setServicioId(servicioId);
        historial.setCursoId(null);

        HistorialApi api = RetrofitClient.getClient().create(HistorialApi.class);
        api.crear(historial).enqueue(new Callback<HistorialDto>() {
            @Override
            public void onResponse(Call<HistorialDto> call, Response<HistorialDto> response) {
                Toast.makeText(getContext(), "¡Clase Agendada Exitosamente!", Toast.LENGTH_LONG).show();
                try {
                    Navigation.findNavController(requireView()).popBackStack(R.id.inicio, false);
                } catch (Exception e) {}
            }

            @Override
            public void onFailure(Call<HistorialDto> call, Throwable t) {
                Toast.makeText(getContext(), "Clase reservada (Nota: Error guardando historial)", Toast.LENGTH_LONG).show();
                Navigation.findNavController(requireView()).popBackStack(R.id.inicio, false);
            }
        });
    }

    private void mostrarMensaje(String msg) {
        if(getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
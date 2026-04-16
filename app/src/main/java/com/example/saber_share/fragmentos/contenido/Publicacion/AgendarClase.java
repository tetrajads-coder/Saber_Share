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

    private RecyclerView   rvHorarios;
    private TextView       tvSubtitulo, tvVacio;
    private SessionManager sessionManager;

    private int    servicioId;
    private int    profesorId;
    private String tituloServicio;
    private double precioClase;

    public AgendarClase() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_agendar_clase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = SessionManager.getInstance(requireContext());

        rvHorarios  = view.findViewById(R.id.rvHorarios);
        tvSubtitulo = view.findViewById(R.id.tvSubtituloServicio);
        tvVacio     = view.findViewById(R.id.tvSinHorarios);

        if (getArguments() != null) {
            servicioId     = getArguments().getInt("servicioId", -1);
            tituloServicio = getArguments().getString("titulo", "Clase");
            precioClase    = getArguments().getDouble("precio", 0.0);
            profesorId     = getArguments().getInt("profesorId", -1);
        }

        tvSubtitulo.setText("Horarios para: " + tituloServicio);
        rvHorarios.setLayoutManager(new LinearLayoutManager(getContext()));
        cargarHorariosDisponibles();
    }

    // ── Cargar slots disponibles ─────────────────────────────────────────────

    private void cargarHorariosDisponibles() {
        RetrofitClient.getInstance().create(AgendaApi.class)
                .getSlotsPorServicio(servicioId).enqueue(new Callback<List<AgendaDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<AgendaDto>> call,
                                           @NonNull Response<List<AgendaDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<AgendaDto> disponibles = new ArrayList<>();
                            for (AgendaDto slot : response.body())
                                if ("DISPONIBLE".equalsIgnoreCase(slot.getEstado()))
                                    disponibles.add(slot);
                            actualizarUI(disponibles);
                        } else {
                            mostrarMensaje("No se pudieron cargar los horarios");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<AgendaDto>> call, @NonNull Throwable t) {
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
            rvHorarios.setAdapter(new AgendaAdapter(lista, this::confirmarReserva));
        }
    }

    // ── Confirmar y reservar ─────────────────────────────────────────────────

    private void confirmarReserva(AgendaDto slot) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar reserva")
                .setMessage("¿Reservar el " + slot.getFecha() + " a las " + slot.getHora() + "?\n\n"
                        + "Se abrirá el chat con el profesor para coordinar los detalles.")
                .setPositiveButton("Sí, agendar", (d, w) -> realizarReserva(slot))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void realizarReserva(AgendaDto slot) {
        int miId = sessionManager.getUsuarioId();
        if (miId == profesorId) {
            mostrarMensaje("No puedes reservar tu propia clase");
            return;
        }

        RetrofitClient.getInstance().create(AgendaApi.class)
                .reservarSlot(slot.getIdAgenda(), miId).enqueue(new Callback<AgendaDto>() {
                    @Override
                    public void onResponse(@NonNull Call<AgendaDto> call,
                                           @NonNull Response<AgendaDto> response) {
                        if (response.isSuccessful()) guardarEnHistorial(slot);
                        else mostrarMensaje("Error al reservar: " + response.code());
                    }
                    @Override
                    public void onFailure(@NonNull Call<AgendaDto> call, @NonNull Throwable t) {
                        mostrarMensaje("Fallo de red al reservar");
                    }
                });
    }

    // ── Guardar historial → abrir chat ───────────────────────────────────────

    private void guardarEnHistorial(AgendaDto slot) {
        HistorialDto historial = new HistorialDto();
        historial.setFechapago(
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        historial.setPago(precioClase);
        historial.setUsuario_idUsuario(sessionManager.getUsuarioId());
        historial.setServicioId(servicioId);
        historial.setCursoId(null);

        RetrofitClient.getInstance().create(HistorialApi.class)
                .crear(historial).enqueue(new Callback<HistorialDto>() {
                    @Override
                    public void onResponse(@NonNull Call<HistorialDto> call,
                                           @NonNull Response<HistorialDto> response) {
                        Toast.makeText(getContext(),
                                "¡Clase agendada! Ahora puedes coordinar con el profesor.",
                                Toast.LENGTH_LONG).show();
                        abrirChatConProfesor();
                    }
                    @Override
                    public void onFailure(@NonNull Call<HistorialDto> call, @NonNull Throwable t) {
                        // Aunque falle el historial, abrimos el chat igualmente
                        Toast.makeText(getContext(),
                                "Clase reservada. Contacta al profesor.",
                                Toast.LENGTH_LONG).show();
                        abrirChatConProfesor();
                    }
                });
    }

    // ── Navegar al chat con el profesor ──────────────────────────────────────

    private void abrirChatConProfesor() {
        if (profesorId <= 0) {
            // Si no tenemos profesorId volvemos a inicio
            try {
                Navigation.findNavController(requireView()).popBackStack(R.id.inicio, false);
            } catch (Exception e) { /* ignorar */ }
            return;
        }

        Bundle args = new Bundle();
        args.putInt("receptorId",       profesorId);
        args.putString("receptorNombre", tituloServicio != null ? tituloServicio : "Profesor");

        try {
            // Limpiamos el back stack hasta inicio y luego abrimos el chat
            Navigation.findNavController(requireView()).popBackStack(R.id.inicio, false);
            Navigation.findNavController(requireView()).navigate(R.id.chatFragment, args);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Clase agendada ✅", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarMensaje(String msg) {
        if (getContext() != null)
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
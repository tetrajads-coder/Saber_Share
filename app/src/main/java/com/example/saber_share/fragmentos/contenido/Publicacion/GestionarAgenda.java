package com.example.saber_share.fragmentos.contenido.Publicacion;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.example.saber_share.fragmentos.contenido.adapter.AgendaProfeAdapter;
import com.example.saber_share.model.AgendaDto;
import com.example.saber_share.util.api.AgendaApi;
import com.example.saber_share.util.api.RetrofitClient;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionarAgenda extends Fragment {

    private RecyclerView rvMisSlots;
    private TextView tvVacio;

    private int servicioId = -1;
    private int profesorId = -1;

    public GestionarAgenda() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_gestionar_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI
        rvMisSlots = view.findViewById(R.id.rvMisSlots);
        tvVacio = view.findViewById(R.id.tvVacioProfe);
        rvMisSlots.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Obtener argumentos de forma segura
        if (getArguments() != null) {
            servicioId = getArguments().getInt("servicioId", -1);
            profesorId = getArguments().getInt("profesorId", -1);
        }

        // ✅ Validar que lleguen correctos
        if (servicioId <= 0 || profesorId <= 0) {
            Toast.makeText(
                    getContext(),
                    "ERROR: servicioId o profesorId invalido\nservicioId=" + servicioId + "\nprofesorId=" + profesorId,
                    Toast.LENGTH_LONG
            ).show();

            // Ocultamos lista por si acaso
            tvVacio.setVisibility(View.VISIBLE);
            rvMisSlots.setVisibility(View.GONE);

            return;
        }

        // Boton agregar
        view.findViewById(R.id.btnAgregarSlot).setOnClickListener(v -> abrirSelectorFecha());

        // Cargar datos
        cargarMisHorarios();
    }

    private void abrirSelectorFecha() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            String fecha = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day);
            abrirSelectorHora(fecha);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void abrirSelectorHora(String fechaSeleccionada) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hour, minute) -> {
            String hora = String.format(Locale.US, "%02d:%02d:00", hour, minute);
            guardarNuevoSlot(fechaSeleccionada, hora);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
    }

    private void guardarNuevoSlot(String fecha, String hora) {

        // ✅ Segunda validacion antes de mandar al backend
        if (servicioId <= 0) {
            Toast.makeText(getContext(), "Servicio invalido: " + servicioId, Toast.LENGTH_LONG).show();
            return;
        }

        AgendaDto nuevo = new AgendaDto();
        nuevo.setFecha(fecha);
        nuevo.setHora(hora);
        nuevo.setServicioId(servicioId);
        nuevo.setProfesorId(profesorId);
        nuevo.setEstado("DISPONIBLE");

        AgendaApi api = RetrofitClient.getClient().create(AgendaApi.class);

        api.crearSlot(nuevo).enqueue(new Callback<AgendaDto>() {
            @Override
            public void onResponse(Call<AgendaDto> call, Response<AgendaDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Horario agregado ✅", Toast.LENGTH_SHORT).show();
                    cargarMisHorarios();
                } else {
                    Toast.makeText(getContext(), "Error al guardar: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AgendaDto> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarMisHorarios() {
        AgendaApi api = RetrofitClient.getClient().create(AgendaApi.class);

        api.getSlotsPorServicio(servicioId).enqueue(new Callback<List<AgendaDto>>() {
            @Override
            public void onResponse(Call<List<AgendaDto>> call, Response<List<AgendaDto>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<AgendaDto> lista = response.body();

                    if (lista.isEmpty()) {
                        tvVacio.setVisibility(View.VISIBLE);
                        rvMisSlots.setVisibility(View.GONE);
                    } else {
                        tvVacio.setVisibility(View.GONE);
                        rvMisSlots.setVisibility(View.VISIBLE);

                        rvMisSlots.setAdapter(new AgendaProfeAdapter(lista, new AgendaProfeAdapter.OnSlotActionListener() {
                            @Override
                            public void onEliminarClick(int idAgenda) {
                                eliminarSlot(idAgenda);
                            }

                            @Override
                            public void onVerDetalleClick(AgendaDto slot) {
                                mostrarDetalleAlumno(slot);
                            }
                        }));
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<AgendaDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo al cargar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDetalleAlumno(AgendaDto slot) {
        String nombre = (slot.getNombreAlumno() != null) ? slot.getNombreAlumno() : "Estudiante";

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Detalle de la Reserva")
                .setMessage("Clase reservada por:\n" + nombre + "\n\nFecha: " + slot.getFecha() + " " + slot.getHora())
                .setPositiveButton("Enviar Mensaje", (d, w) -> {
                    Navigation.findNavController(requireView()).navigate(R.id.mensajes);
                    Toast.makeText(getContext(), "Abriendo chat con " + nombre, Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cerrar", null)
                .create();

        dialog.show();
    }

    private void eliminarSlot(int idAgenda) {
        Toast.makeText(getContext(), "Eliminar ID: " + idAgenda + " (Implementar API)", Toast.LENGTH_SHORT).show();
    }
}

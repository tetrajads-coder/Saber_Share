package com.example.saber_share.fragmentos.contenido;

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
import com.example.saber_share.fragmentos.contenido.adapter.MetodoPagoAdapter;
import com.example.saber_share.model.MetodoDePagoDto;
import com.example.saber_share.util.api.MetodoPagoApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdministrarTarjetas extends Fragment {

    private RecyclerView rvTarjetas;
    private TextView tvVacio;
    private MetodoPagoAdapter adapter;
    private SessionManager sessionManager;

    public AdministrarTarjetas() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_administrar_tarjetas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        rvTarjetas = view.findViewById(R.id.rvTarjetas);
        tvVacio = view.findViewById(R.id.tvSinTarjetas);

        // Configurar RecyclerView
        rvTarjetas.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MetodoPagoAdapter(new ArrayList<>(), this::confirmarEliminacion);
        rvTarjetas.setAdapter(adapter);

        // Botón Agregar
        view.findViewById(R.id.fabAgregarTarjeta).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_administrarTarjetas_to_agregarTarjeta)
        );

        cargarTarjetas();
    }

    private void cargarTarjetas() {
        MetodoPagoApi api = RetrofitClient.getClient().create(MetodoPagoApi.class);
        api.listarTarjetas().enqueue(new Callback<List<MetodoDePagoDto>>() {
            @Override
            public void onResponse(Call<List<MetodoDePagoDto>> call, Response<List<MetodoDePagoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MetodoDePagoDto> misTarjetas = new ArrayList<>();
                    int miId = sessionManager.getUserId();

                    for (MetodoDePagoDto m : response.body()) {
                        if (m.getUsuarioId() != null && m.getUsuarioId() == miId) {
                            misTarjetas.add(m);
                        }
                    }

                    actualizarUI(misTarjetas);
                } else {
                    mostrarError("Error al cargar tarjetas");
                }
            }

            @Override
            public void onFailure(Call<List<MetodoDePagoDto>> call, Throwable t) {
                mostrarError("Error de conexión");
            }
        });
    }

    private void actualizarUI(List<MetodoDePagoDto> lista) {
        if (lista.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            rvTarjetas.setVisibility(View.GONE);
        } else {
            tvVacio.setVisibility(View.GONE);
            rvTarjetas.setVisibility(View.VISIBLE);
            adapter.setDatos(lista);
        }
    }

    private void confirmarEliminacion(int idTarjeta) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Tarjeta")
                .setMessage("¿Seguro que deseas eliminar este método de pago?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarTarjeta(idTarjeta))
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarTarjeta(int id) {
        MetodoPagoApi api = RetrofitClient.getClient().create(MetodoPagoApi.class);
        api.eliminarTarjeta(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Tarjeta eliminada", Toast.LENGTH_SHORT).show();
                    cargarTarjetas(); // Recargar lista
                } else {
                    mostrarError("No se pudo eliminar");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mostrarError("Error de red");
            }
        });
    }

    private void mostrarError(String msg) {
        if (getContext() != null)
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
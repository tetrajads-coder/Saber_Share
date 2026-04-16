package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.contenido.adapter.HistorialAdapter;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.util.api.HistorialApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialFragment extends Fragment {

    private RecyclerView       rvHistorial;
    private SwipeRefreshLayout swipeHistorial;
    private View               layoutEmpty;
    private HistorialAdapter   adapter;
    private final List<HistorialDto> datos = new ArrayList<>();

    public HistorialFragment() { super(R.layout.fragment_historial); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistorial    = view.findViewById(R.id.rv_historial);
        swipeHistorial = view.findViewById(R.id.swipe_historial);
        layoutEmpty    = view.findViewById(R.id.layout_empty_historial);

        // Toolbar back
        view.<com.google.android.material.appbar.MaterialToolbar>findViewById(R.id.toolbar_historial)
                .setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistorialAdapter(requireContext(), datos);
        rvHistorial.setAdapter(adapter);

        swipeHistorial.setColorSchemeResources(R.color.accent_primary);
        swipeHistorial.setOnRefreshListener(this::cargarHistorial);

        cargarHistorial();
    }

    private void cargarHistorial() {
        swipeHistorial.setRefreshing(true);
        int idUsuario = SessionManager.getInstance(requireContext()).getUsuarioId();

        RetrofitClient.getInstance().create(HistorialApi.class)
                .historialPorUsuario(idUsuario).enqueue(new Callback<List<HistorialDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<HistorialDto>> call,
                                           @NonNull Response<List<HistorialDto>> response) {
                        swipeHistorial.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            datos.clear();
                            datos.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            boolean vacio = datos.isEmpty();
                            layoutEmpty.setVisibility(vacio ? View.VISIBLE : View.GONE);
                            rvHistorial.setVisibility(vacio ? View.GONE : View.VISIBLE);
                        } else {
                            Toast.makeText(getContext(), "Error al cargar historial", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<HistorialDto>> call, @NonNull Throwable t) {
                        swipeHistorial.setRefreshing(false);
                        Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
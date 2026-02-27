package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

    private RecyclerView rvHistorial;
    private HistorialAdapter adapter;
    private final List<HistorialDto> datos = new ArrayList<>();

    public HistorialFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistorial = view.findViewById(R.id.rvHistorial);
        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistorialAdapter(requireContext(), datos);
        rvHistorial.setAdapter(adapter);

        cargarHistorial();
    }

    private void cargarHistorial() {
        SessionManager sm = new SessionManager(requireContext());
        int idUsuario = sm.getUserId();

        HistorialApi api = RetrofitClient.getClient().create(HistorialApi.class);
        api.historialPorUsuario(idUsuario).enqueue(new Callback<List<HistorialDto>>() {
            @Override
            public void onResponse(Call<List<HistorialDto>> call, Response<List<HistorialDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    datos.clear();
                    datos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error al cargar historial", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<HistorialDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

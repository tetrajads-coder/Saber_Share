package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.saber_share.R;
import com.example.saber_share.model.CursoDto;
import com.example.saber_share.model.Publicacion;
import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.fragmentos.contenido.adapter.PublicacionAdapter;
import com.example.saber_share.util.api.CursoApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.ServicioApi;
import com.example.saber_share.util.local.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisPublicaciones extends Fragment {

    private RecyclerView       rvMisPublicaciones;
    private SwipeRefreshLayout swipeRefresh;
    private View               layoutEmpty;
    private PublicacionAdapter adapter;
    private final List<Publicacion> lista = new ArrayList<>();

    private SessionManager sessionManager;
    private int userId;

    public MisPublicaciones() { super(R.layout.fragment_mis_publicaciones); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        userId = sessionManager.getUsuarioId();

        rvMisPublicaciones = view.findViewById(R.id.rv_mis_publicaciones);
        swipeRefresh       = view.findViewById(R.id.swipe_mis_publicaciones);
        layoutEmpty        = view.findViewById(R.id.layout_empty_mis_publicaciones);

        view.<com.google.android.material.appbar.MaterialToolbar>
                        findViewById(R.id.toolbar_mis_publicaciones)
                .setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        rvMisPublicaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PublicacionAdapter(requireContext(), lista, userId, p -> onItemClick(p));
        rvMisPublicaciones.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.accent_primary);
        swipeRefresh.setOnRefreshListener(this::cargar);

        cargar();
    }

    private void cargar() {
        swipeRefresh.setRefreshing(true);
        lista.clear();
        cargarCursos();
    }

    private void cargarCursos() {
        RetrofitClient.getInstance().create(CursoApi.class)
                .lista().enqueue(new Callback<List<CursoDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CursoDto>> call,
                                           @NonNull Response<List<CursoDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (CursoDto c : response.body()) {
                                if (c.getUsuarioId() != null && c.getUsuarioId() == userId) {
                                    lista.add(new Publicacion(
                                            Publicacion.TIPO_CURSO,
                                            c.getIdCurso() != null ? c.getIdCurso() : 0,
                                            c.getTitulo(), c.getDescripcion(),
                                            c.getPrecio() != null ? c.getPrecio() : 0.0,
                                            sessionManager.getNombre(), c.getCalificacion(),
                                            null, c.getFoto() != null ? c.getFoto() : "",
                                            userId));
                                }
                            }
                        }
                        cargarServicios();
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<CursoDto>> call, @NonNull Throwable t) {
                        cargarServicios();
                    }
                });
    }

    private void cargarServicios() {
        RetrofitClient.getInstance().create(ServicioApi.class)
                .lista().enqueue(new Callback<List<ServicioDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ServicioDto>> call,
                                           @NonNull Response<List<ServicioDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (ServicioDto s : response.body()) {
                                if (s.getUsuarioId() != null && s.getUsuarioId() == userId) {
                                    lista.add(new Publicacion(
                                            Publicacion.TIPO_CLASE,
                                            s.getServicioId() != null ? s.getServicioId() : 0,
                                            s.getTitulo(), s.getDescripcion(),
                                            s.getPrecio() != null ? s.getPrecio() : 0.0,
                                            sessionManager.getNombre(), "0",
                                            null, s.getRequisitos() != null ? s.getRequisitos() : "",
                                            userId));
                                }
                            }
                        }
                        swipeRefresh.setRefreshing(false);
                        adapter.setDatos(new ArrayList<>(lista));
                        boolean vacio = lista.isEmpty();
                        layoutEmpty.setVisibility(vacio ? View.VISIBLE : View.GONE);
                        rvMisPublicaciones.setVisibility(vacio ? View.GONE : View.VISIBLE);
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<ServicioDto>> call, @NonNull Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onItemClick(Publicacion p) {
        // Si es CLASE → ofrecer gestionar agenda o editar
        if (Publicacion.TIPO_CLASE.equals(p.getTipo())) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(p.getTitulo())
                    .setItems(new String[]{"📅 Gestionar agenda", "✏️ Editar", "Cancelar"},
                            (dialog, which) -> {
                                if (which == 0) irAGestionarAgenda(p);
                                else if (which == 1) irAEditar(p);
                            })
                    .show();
        } else {
            // CURSO → solo editar
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(p.getTitulo())
                    .setItems(new String[]{"✏️ Editar", "Cancelar"},
                            (dialog, which) -> { if (which == 0) irAEditar(p); })
                    .show();
        }
    }

    private void irAGestionarAgenda(Publicacion p) {
        Bundle args = new Bundle();
        args.putInt("servicioId", p.getIdOriginal());
        args.putInt("profesorId", userId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_misPublicaciones_to_gestionarAgenda, args);
    }

    private void irAEditar(Publicacion p) {
        Bundle args = new Bundle();
        args.putInt("idOriginal",    p.getIdOriginal());
        args.putString("tipo",       p.getTipo());
        args.putString("titulo",     p.getTitulo());
        args.putString("descripcion",p.getDescripcion());
        args.putDouble("precio",     p.getPrecio());
        args.putString("extra",      p.getExtraInfo());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_misPublicaciones_to_editar, args);
    }
}
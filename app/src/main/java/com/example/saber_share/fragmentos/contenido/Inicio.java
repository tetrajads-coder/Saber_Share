package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.contenido.adapter.PublicacionAdapter;
import com.example.saber_share.model.CursoDto;
import com.example.saber_share.model.Publicacion;
import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.util.api.CursoApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.ServicioApi;
import com.example.saber_share.util.local.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Inicio extends Fragment implements PublicacionAdapter.OnItemClickListener {

    private RecyclerView          rvPublicaciones;
    private SwipeRefreshLayout    swipeRefresh;
    private View                  layoutEmpty;
    private TextView              tvSaludo, tvAvatarInicial;
    private TextInputEditText     etBuscar;
    private Chip                  chipTodos, chipCursos, chipServicios, chipPopulares;

    private PublicacionAdapter    adapter;
    private final List<Publicacion> listaTodas = new ArrayList<>();

    private SessionManager sessionManager;
    private String filtroActivo = "TODOS";

    public Inicio() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());

        tvSaludo        = view.findViewById(R.id.tv_saludo);
        tvAvatarInicial = view.findViewById(R.id.tv_avatar_inicial);
        rvPublicaciones = view.findViewById(R.id.rv_publicaciones);
        swipeRefresh    = view.findViewById(R.id.swipe_refresh_inicio);
        layoutEmpty     = view.findViewById(R.id.layout_empty_inicio);
        etBuscar        = view.findViewById(R.id.et_buscar);
        chipTodos       = view.findViewById(R.id.chip_todos);
        chipCursos      = view.findViewById(R.id.chip_cursos);
        chipServicios   = view.findViewById(R.id.chip_servicios);
        chipPopulares   = view.findViewById(R.id.chip_populares);

        // Saludo
        String nombre = sessionManager.getNombre();
        if (nombre == null || nombre.trim().isEmpty()) nombre = "Usuario";

        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String saludo = hora >= 5 && hora < 12 ? "Buenos días" :
                hora >= 12 && hora < 19 ? "Buenas tardes" : "Buenas noches";

        tvSaludo.setText(saludo + ", " + nombre);
        tvAvatarInicial.setText(String.valueOf(nombre.charAt(0)).toUpperCase());

        // RecyclerView
        rvPublicaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PublicacionAdapter(requireContext(), new ArrayList<>(),
                sessionManager.getUsuarioId(), this);
        rvPublicaciones.setAdapter(adapter);

        // Swipe refresh
        swipeRefresh.setColorSchemeResources(R.color.accent_primary);
        swipeRefresh.setOnRefreshListener(this::cargarPublicaciones);

        // Buscador
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
                actualizarEstadoVacio();
            }
        });

        // Chips
        chipTodos.setOnClickListener(v -> aplicarFiltro("TODOS"));
        chipCursos.setOnClickListener(v -> aplicarFiltro("CURSO"));
        chipServicios.setOnClickListener(v -> aplicarFiltro("SERVICIO"));
        chipPopulares.setOnClickListener(v -> aplicarFiltro("POPULARES"));

        cargarPublicaciones();
    }

    // ── Carga ────────────────────────────────────────────────────────────────

    private void cargarPublicaciones() {
        swipeRefresh.setRefreshing(true);
        listaTodas.clear();
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
                                listaTodas.add(new Publicacion(
                                        Publicacion.TIPO_CURSO,
                                        c.getIdCurso() != null ? c.getIdCurso() : 0,
                                        c.getTitulo(), c.getDescripcion(),
                                        c.getPrecio() != null ? c.getPrecio() : 0.0,
                                        c.getNombreUsuario() != null ? c.getNombreUsuario() : "Instructor",
                                        c.getCalificacion() != null ? c.getCalificacion() : "0",
                                        c.getFoto() != null ? c.getFoto() : "",  // ← imagenUrl = foto
                                        c.getFoto() != null ? c.getFoto() : "",
                                        c.getUsuarioId() != null ? c.getUsuarioId() : 0));
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
                                listaTodas.add(new Publicacion(
                                        Publicacion.TIPO_CLASE,
                                        s.getServicioId() != null ? s.getServicioId() : 0,
                                        s.getTitulo(), s.getDescripcion(),
                                        s.getPrecio() != null ? s.getPrecio() : 0.0,
                                        s.getNombreUsuario() != null ? s.getNombreUsuario() : "Instructor",
                                        "0", null,
                                        s.getRequisitos() != null ? s.getRequisitos() : "",
                                        s.getUsuarioId() != null ? s.getUsuarioId() : 0));
                            }
                        }
                        swipeRefresh.setRefreshing(false);
                        aplicarFiltro(filtroActivo);
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<ServicioDto>> call, @NonNull Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        aplicarFiltro(filtroActivo);
                    }
                });
    }

    // ── Filtros ───────────────────────────────────────────────────────────────

    private void aplicarFiltro(String filtro) {
        filtroActivo = filtro;
        List<Publicacion> filtradas = new ArrayList<>();
        for (Publicacion p : listaTodas) {
            switch (filtro) {
                case "CURSO":    if (Publicacion.TIPO_CURSO.equals(p.getTipo()))  filtradas.add(p); break;
                case "SERVICIO": if (Publicacion.TIPO_CLASE.equals(p.getTipo()))  filtradas.add(p); break;
                default:         filtradas.add(p); break;
            }
        }
        adapter.setDatos(filtradas);
        actualizarEstadoVacio();
    }

    private void actualizarEstadoVacio() {
        boolean vacio = adapter.getItemCount() == 0;
        layoutEmpty.setVisibility(vacio ? View.VISIBLE : View.GONE);
        rvPublicaciones.setVisibility(vacio ? View.GONE : View.VISIBLE);
    }

    // ── Click en publicación ──────────────────────────────────────────────────

    @Override
    public void onItemClick(Publicacion p) {
        int miId = sessionManager.getUsuarioId();

        // Si es mía → mostrar opciones de gestión
        if (p.getIdAutor() == miId) {
            String[] opciones = Publicacion.TIPO_CLASE.equals(p.getTipo())
                    ? new String[]{"✏️ Editar", "📅 Gestionar agenda", "Cancelar"}
                    : new String[]{"✏️ Editar", "Cancelar"};

            new AlertDialog.Builder(requireContext())
                    .setTitle(p.getTitulo())
                    .setItems(opciones, (dialog, which) -> {
                        if (opciones[which].contains("Editar")) {
                            irAEditar(p);
                        } else if (opciones[which].contains("Gestionar")) {
                            irAGestionarAgenda(p);
                        }
                    })
                    .show();
            return;
        }

        // Si no es mía → ir al detalle con contenidoUrl incluido
        Bundle args = new Bundle();
        args.putInt("itemId",         p.getIdOriginal());
        args.putString("tipo",        p.getTipo());
        args.putString("titulo",      p.getTitulo());
        args.putString("descripcion", p.getDescripcion());
        args.putDouble("precio",      p.getPrecio());
        args.putInt("autorId",        p.getIdAutor());
        args.putString("autorNombre", p.getAutor() != null ? p.getAutor() : "Vendedor");
        args.putString("contenidoUrl", p.getImagenUrl() != null ? p.getImagenUrl() : ""); // ← NUEVO

        Navigation.findNavController(requireView())
                .navigate(R.id.action_inicio_to_detallePublicacion, args);
    }

    private void irAEditar(Publicacion p) {
        Bundle args = new Bundle();
        args.putInt("idOriginal",     p.getIdOriginal());
        args.putString("tipo",        p.getTipo());
        args.putString("titulo",      p.getTitulo());
        args.putString("descripcion", p.getDescripcion());
        args.putDouble("precio",      p.getPrecio());
        args.putString("extra",       p.getExtraInfo() != null ? p.getExtraInfo() : "");
        Navigation.findNavController(requireView())
                .navigate(R.id.action_inicio_to_editarPublicacion, args);
    }

    private void irAGestionarAgenda(Publicacion p) {
        Bundle args = new Bundle();
        args.putInt("servicioId", p.getIdOriginal());
        args.putInt("profesorId", sessionManager.getUsuarioId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_inicio_to_gestionarAgenda, args);
    }
}
package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.contenido.adapter.PublicacionAdapter;
import com.example.saber_share.model.CursoDto;
import com.example.saber_share.model.Publicacion;
import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.util.api.CursoApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.ServicioApi;
import com.example.saber_share.util.local.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Comprar extends Fragment {

    private RecyclerView rvResultados;
    private EditText etBuscar;
    private PublicacionAdapter adapter;
    private List<Publicacion> listaGlobal = new ArrayList<>();
    private SessionManager sessionManager;

    public Comprar() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_comprar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        rvResultados = view.findViewById(R.id.rvResultadosBusqueda);
        etBuscar = view.findViewById(R.id.etBuscarComprar);

        // Configurar RecyclerView
        rvResultados.setLayoutManager(new LinearLayoutManager(getContext()));

        // OBTENER EL ID DEL USUARIO ACTUAL
        int miId = sessionManager.getUserId();

        // CREAR ADAPTER CON LOS 4 PARÁMETROS NUEVOS
        adapter = new PublicacionAdapter(
                getContext(),
                new ArrayList<>(),
                miId,
                this::irADetalle // Referencia al método que maneja el clic
        );

        rvResultados.setAdapter(adapter);

        // Cargar datos desde API
        cargarDatos();

        // Configurar Buscador
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void irADetalle(Publicacion p) {
        Bundle bundle = new Bundle();
        bundle.putInt("idOriginal", p.getIdOriginal());
        bundle.putInt("idAutor", p.getIdAutor());
        bundle.putString("tipo", p.getTipo());
        bundle.putString("titulo", p.getTitulo());
        bundle.putString("descripcion", p.getDescripcion());
        bundle.putDouble("precio", p.getPrecio());
        bundle.putString("autor", p.getAutor());
        bundle.putString("calificacion", p.getCalificacion());

        // IMPORTANTE: Pasar el campo extra
        // (Asegúrate de haber agregado getExtra() a tu modelo Publicacion)
        bundle.putString("extra", p.getImagenUrl()); // Usamos imagenUrl para guardar el archivo/requisito temporalmente

        Navigation.findNavController(requireView()).navigate(R.id.detallePublicacion, bundle);
    }

    private void cargarDatos() {
        listaGlobal.clear();

        CursoApi cursoApi = RetrofitClient.getClient().create(CursoApi.class);
        cursoApi.lista().enqueue(new Callback<List<CursoDto>>() {
            @Override
            public void onResponse(Call<List<CursoDto>> call, Response<List<CursoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (CursoDto c : response.body()) {
                        // Asegúrate de pasar el ID del autor aquí (c.getUsuarioId())
                        listaGlobal.add(new Publicacion(
                                Publicacion.TIPO_CURSO,
                                c.getIdCurso(),
                                c.getTitulo(),
                                c.getDescripcion(),
                                c.getPrecio(),
                                c.getNombreUsuario(),
                                c.getCalificacion(),
                                c.getFoto(),
                                null,
                                c.getUsuarioId() // <--- PASAR ID DEL AUTOR (Agregado al constructor de Publicacion)
                        ));
                    }
                    adapter.setDatos(listaGlobal);
                }
                cargarServicios();
            }

            @Override
            public void onFailure(Call<List<CursoDto>> call, Throwable t) {
                cargarServicios();
            }
        });
    }

    private void cargarServicios() {
        ServicioApi servicioApi = RetrofitClient.getClient().create(ServicioApi.class);
        servicioApi.lista().enqueue(new Callback<List<ServicioDto>>() {
            @Override
            public void onResponse(Call<List<ServicioDto>> call, Response<List<ServicioDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ServicioDto s : response.body()) {
                        listaGlobal.add(new Publicacion(
                                Publicacion.TIPO_CLASE,
                                s.getServicioId(),
                                s.getTitulo(),
                                s.getDescripcion(),
                                s.getPrecio(),
                                s.getNombreUsuario(),
                                s.getRequisitos(),
                                "N/A",
                                null,
                                s.getUsuarioId() // <--- PASAR ID DEL AUTOR
                        ));
                    }
                    adapter.setDatos(listaGlobal);
                }
            }

            @Override
            public void onFailure(Call<List<ServicioDto>> call, Throwable t) {
                if(listaGlobal.isEmpty()) {
                    Toast.makeText(getContext(), "No se pudieron cargar las publicaciones", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
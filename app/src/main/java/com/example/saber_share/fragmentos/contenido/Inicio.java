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
import android.widget.TextView;
import android.widget.Toast;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.contenido.adapter.PublicacionAdapter;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.model.Publicacion;
import com.example.saber_share.util.api.HistorialApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Inicio extends Fragment implements PublicacionAdapter.OnItemClickListener {

    private RecyclerView rvProximasClases, rvContinuarCursos, rvCursosImpartidos;
    private PublicacionAdapter adapterProximas;
    private PublicacionAdapter adapterContinuar;
    private PublicacionAdapter adapterImpartidos;

    private final List<Publicacion> listaProximas   = new ArrayList<>();
    private final List<Publicacion> listaContinuar  = new ArrayList<>();
    private final List<Publicacion> listaImpartidos = new ArrayList<>();

    private SessionManager sessionManager;

    public Inicio() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_inicio, container, false);

        // ---------- SALUDO ----------
        TextView tvSaludo = view.findViewById(R.id.tvSaludo);

        sessionManager = new SessionManager(requireContext());
        String nombre = sessionManager.getNombre();
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = "Usuario";
        }

        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);

        String saludo;
        if (hora >= 5 && hora < 12) {
            saludo = "Buenos dias";
        } else if (hora >= 12 && hora < 19) {
            saludo = "Buenas tardes";
        } else {
            saludo = "Buenas noches";
        }

        tvSaludo.setText(saludo + ", " + nombre);

        // ---------- RECYCLERVIEWS ----------
        rvProximasClases    = view.findViewById(R.id.rvProximasClases);
        rvContinuarCursos   = view.findViewById(R.id.rvContinuarCursos);
        rvCursosImpartidos  = view.findViewById(R.id.rvCursosImpartidos);

        rvProximasClases.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvContinuarCursos.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvCursosImpartidos.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
        );

        int idUsuarioActual = sessionManager.getUserId();

        adapterProximas = new PublicacionAdapter(
                requireContext(),
                listaProximas,
                idUsuarioActual,
                this
        );

        adapterContinuar = new PublicacionAdapter(
                requireContext(),
                listaContinuar,
                idUsuarioActual,
                this
        );

        adapterImpartidos = new PublicacionAdapter(
                requireContext(),
                listaImpartidos,
                idUsuarioActual,
                this
        );

        rvProximasClases.setAdapter(adapterProximas);
        rvContinuarCursos.setAdapter(adapterContinuar);
        rvCursosImpartidos.setAdapter(adapterImpartidos);

        // Cargar datos reales desde historial
        cargarDesdeHistorial();

        return view;
    }

    private void cargarDesdeHistorial() {
        int idUsuario = sessionManager.getUserId();
        if (idUsuario <= 0) return;

        HistorialApi api = RetrofitClient.getClient().create(HistorialApi.class);
        api.historialPorUsuario(idUsuario).enqueue(new Callback<List<HistorialDto>>() {
            @Override
            public void onResponse(Call<List<HistorialDto>> call, Response<List<HistorialDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "No se pudo cargar historial", Toast.LENGTH_SHORT).show();
                    return;
                }

                listaProximas.clear();
                listaContinuar.clear();
                listaImpartidos.clear();

                for (HistorialDto h : response.body()) {
                    // Ojo: aqui no tenemos aun titulo real del curso/servicio,
                    // asi que armamos algo simple. Luego se puede mejorar
                    // consultando CursoApi y ServicioApi por id.
                    if (h.getCursoId() != null) {
                        Publicacion pubCurso = new Publicacion(
                                Publicacion.TIPO_CURSO,
                                h.getCursoId(),
                                "Curso #" + h.getCursoId(),
                                "Comprado el " + h.getFechapago(),
                                h.getPago() != null ? h.getPago() : 0.0,
                                "Profesor",
                                "N/A",
                                null,
                                "",
                                0
                        );
                        // Lo mostramos como "Continuar cursos"
                        listaContinuar.add(pubCurso);
                    }

                    if (h.getServicioId() != null) {
                        Publicacion pubClase = new Publicacion(
                                Publicacion.TIPO_CLASE,
                                h.getServicioId(),
                                "Clase 1 a 1 #" + h.getServicioId(),
                                "Agendada el " + h.getFechapago(),
                                h.getPago() != null ? h.getPago() : 0.0,
                                "Profesor",
                                "N/A",
                                null,
                                "",
                                0
                        );
                        // La ponemos en "Proximas clases 1 a 1"
                        listaProximas.add(pubClase);
                    }
                }

                adapterProximas.notifyDataSetChanged();
                adapterContinuar.notifyDataSetChanged();
                adapterImpartidos.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<HistorialDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onItemClick(Publicacion publicacion) {
        Bundle bundle = new Bundle();
        bundle.putInt("idAutor", publicacion.getIdAutor());
        bundle.putInt("idOriginal", publicacion.getIdOriginal());
        bundle.putString("tipo", publicacion.getTipo());
        bundle.putString("titulo", publicacion.getTitulo());
        bundle.putString("descripcion", publicacion.getDescripcion());
        bundle.putDouble("precio", publicacion.getPrecio());
        bundle.putString("autor", publicacion.getAutor());
        bundle.putString("extra", publicacion.getExtraInfo());
        bundle.putString("calificacion", publicacion.getCalificacion());

        androidx.navigation.Navigation.findNavController(requireView())
                .navigate(R.id.action_inicio_to_detallePublicacion, bundle);
    }
}

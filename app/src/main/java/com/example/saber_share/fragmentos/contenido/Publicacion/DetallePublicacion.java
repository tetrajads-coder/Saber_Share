package com.example.saber_share.fragmentos.contenido.Publicacion;

import android.content.ActivityNotFoundException;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.saber_share.R;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.model.Publicacion;
import com.example.saber_share.util.api.CursoApi;
import com.example.saber_share.util.api.HistorialApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.ServicioApi;
import com.example.saber_share.util.api.UsuarioApi;
import com.example.saber_share.util.local.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePublicacion extends Fragment {

    private int idAutor;
    private int idOriginal;
    private String tipo, titulo, descripcion, autor, extra, calificacion;
    private double precio;

    private SessionManager sessionManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idAutor      = getArguments().getInt("idAutor");
            idOriginal   = getArguments().getInt("idOriginal");
            tipo         = getArguments().getString("tipo");
            titulo       = getArguments().getString("titulo");
            descripcion  = getArguments().getString("descripcion");
            precio       = getArguments().getDouble("precio");
            autor        = getArguments().getString("autor");
            extra        = getArguments().getString("extra");
            calificacion = getArguments().getString("calificacion");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_detalle_publicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        int miId = sessionManager.getUserId();

        // 1) Llenar datos visuales
        ((TextView) view.findViewById(R.id.tvDetalleTitulo)).setText(titulo);
        ((TextView) view.findViewById(R.id.tvDetalleDescripcion)).setText(descripcion);
        ((TextView) view.findViewById(R.id.tvDetallePrecio)).setText(String.format("$ %.2f", precio));
        ((TextView) view.findViewById(R.id.tvDetalleAutor)).setText(autor);
        ((TextView) view.findViewById(R.id.tvDetalleCalif)).setText(calificacion + " ★");

        TextView tvLabelExtra = view.findViewById(R.id.tvLabelExtra);
        TextView tvExtra = view.findViewById(R.id.tvDetalleExtra);

        if (Publicacion.TIPO_CURSO.equals(tipo)) {
            tvLabelExtra.setText("Archivo del curso:");
            tvExtra.setText(extra != null ? extra : "No disponible");
        } else {
            tvLabelExtra.setText("Requisitos para la clase:");
            tvExtra.setText(extra != null ? extra : "Sin requisitos");
        }

        // 2) Paneles
        LinearLayout panelDueno = view.findViewById(R.id.panelDueno);
        LinearLayout panelCliente = view.findViewById(R.id.panelClienteCompra);
        LinearLayout panelAlumno = view.findViewById(R.id.panelAlumnoAcceso);
        Button btnAccion = view.findViewById(R.id.btnAccionPrincipal);
        Button btnContactar = view.findViewById(R.id.btnContactarProfe);

        // Boton flotante atras
        view.findViewById(R.id.fabAtras)
                .setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        if (miId == idAutor) {
            // Soy dueno
            panelDueno.setVisibility(View.VISIBLE);
            panelCliente.setVisibility(View.GONE);
            panelAlumno.setVisibility(View.GONE);
            configurarPanelDueno(view);
        } else {
            // Soy cliente
            panelDueno.setVisibility(View.GONE);
            panelCliente.setVisibility(View.VISIBLE);
            panelAlumno.setVisibility(View.GONE);

            if (Publicacion.TIPO_CURSO.equals(tipo)) {
                btnAccion.setText("Comprar Curso - " + String.format("$ %.2f", precio));
                btnAccion.setOnClickListener(v -> iniciarCompra());
            } else {
                btnAccion.setText("Agendar Clase");
                btnAccion.setOnClickListener(v -> confirmarYAgendarClase());
            }

            // Boton contactar (chat)
            btnContactar.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("receptorId", idAutor);
                bundle.putString("nombreReceptor", autor);
                Navigation.findNavController(v).navigate(R.id.mensajes, bundle);
            });
        }
    }

    private void configurarPanelDueno(View view) {
        // Editar
        view.findViewById(R.id.btnEditarCurso).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("idOriginal", idOriginal);
            bundle.putString("tipo", tipo);
            bundle.putString("titulo", titulo);
            bundle.putString("descripcion", descripcion);
            bundle.putDouble("precio", precio);
            bundle.putString("extra", extra);

            Navigation.findNavController(v)
                    .navigate(R.id.action_detallePublicacion_to_editarPublicacion, bundle);
        });

        View btnAgenda = view.findViewById(R.id.btnGestionarAgenda);
        if (btnAgenda != null) {
            btnAgenda.setOnClickListener(v -> {
                Bundle b = new Bundle();
                b.putInt("idOriginal", idOriginal);
                b.putString("tipo", tipo);
                b.putString("titulo", titulo);
                b.putInt("idAutor", idAutor);


                    Navigation.findNavController(v).navigate(R.id.gestionarAgendaFragment, b);
            });
        }

        // Eliminar
        view.findViewById(R.id.btnEliminar).setOnClickListener(v -> mostrarDialogoConfirmacion());

        // Ver alumnos inscritos
        view.findViewById(R.id.btnVerAlumnos).setOnClickListener(v -> cargarYMostrarAlumnosTexto());
    }

    private void cargarYMostrarAlumnosTexto() {
        if (getContext() == null) return;

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Alumnos inscritos")
                .setMessage("Cargando...")
                .setPositiveButton("Cerrar", null)
                .create();

        dialog.show();

        HistorialApi historialApi = RetrofitClient.getClient().create(HistorialApi.class);
        UsuarioApi usuarioApi = RetrofitClient.getClient().create(UsuarioApi.class);

        historialApi.listarPorCurso(idOriginal).enqueue(new Callback<List<HistorialDto>>() {
            @Override
            public void onResponse(Call<List<HistorialDto>> call, Response<List<HistorialDto>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    dialog.setMessage("Error al cargar alumnos (" + response.code() + ")");
                    return;
                }

                Set<Integer> ids = new HashSet<>();
                for (HistorialDto h : response.body()) {
                    ids.add(h.getUsuario_idUsuario());
                }

                if (ids.isEmpty()) {
                    dialog.setMessage("No hay alumnos inscritos.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                final int[] pendientes = { ids.size() };

                for (Integer idUsuario : ids) {
                    usuarioApi.obtenerPorId(idUsuario).enqueue(new Callback<com.example.saber_share.model.UsuarioDto>() {
                        @Override
                        public void onResponse(Call<com.example.saber_share.model.UsuarioDto> call,
                                               Response<com.example.saber_share.model.UsuarioDto> response) {

                            if (response.isSuccessful() && response.body() != null) {
                                com.example.saber_share.model.UsuarioDto u = response.body();

                                String nombre = u.getNombre() != null ? u.getNombre() : "";
                                String apellido = u.getApellido() != null ? u.getApellido() : "";
                                String nombreCompleto = (nombre + " " + apellido).trim();

                                if (!nombreCompleto.isEmpty()) {
                                    sb.append("• ").append(nombreCompleto)
                                            .append(" (ID: ").append(idUsuario).append(")\n");
                                } else {
                                    sb.append("• Alumno ID: ").append(idUsuario).append("\n");
                                }
                            } else {
                                sb.append("• Alumno ID: ").append(idUsuario).append("\n");
                            }

                            pendientes[0]--;
                            if (pendientes[0] == 0) dialog.setMessage(sb.toString());
                        }

                        @Override
                        public void onFailure(Call<com.example.saber_share.model.UsuarioDto> call, Throwable t) {
                            sb.append("• Alumno ID: ").append(idUsuario).append("\n");
                            pendientes[0]--;
                            if (pendientes[0] == 0) dialog.setMessage(sb.toString());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<HistorialDto>> call, Throwable t) {
                dialog.setMessage("Error de conexion");
            }
        });
    }

    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Publicacion")
                .setMessage("Estas seguro de que deseas eliminar esto? Esta accion no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> ejecutarEliminacion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminacion() {
        if (Publicacion.TIPO_CURSO.equals(tipo)) {
            CursoApi api = RetrofitClient.getClient().create(CursoApi.class);
            api.deleteCurso(idOriginal).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Curso eliminado correctamente", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Error al eliminar: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
            api.deleteServicio(idOriginal).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Clase eliminada correctamente", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Error al eliminar: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void iniciarCompra() {
        if (getContext() == null) return;
        int idUsuario = sessionManager.getUserId();

        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar compra")
                .setMessage("Quieres comprar el curso \"" + titulo + "\" por $" + String.format(Locale.getDefault(), "%.2f", precio) + "?")
                .setPositiveButton("Comprar", (dialog, which) -> {
                    HistorialDto dto = new HistorialDto();
                    dto.setFechapago(fechaHoyIso());
                    dto.setPago(precio);
                    dto.setUsuario_idUsuario(idUsuario);
                    dto.setCursoId(idOriginal);
                    dto.setServicioId(null);

                    HistorialApi api = RetrofitClient.getClient().create(HistorialApi.class);
                    api.crear(dto).enqueue(new Callback<HistorialDto>() {
                        @Override
                        public void onResponse(Call<HistorialDto> call, Response<HistorialDto> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Compra registrada correctamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Error al registrar compra (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<HistorialDto> call, Throwable t) {
                            Toast.makeText(getContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarYAgendarClase() {
        if (getContext() == null) return;
        int idUsuario = sessionManager.getUserId();

        new AlertDialog.Builder(getContext())
                .setTitle("Agendar clase")
                .setMessage("Quieres agendar esta clase 1 a 1 por $" + String.format(Locale.getDefault(), "%.2f", precio) + "?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    HistorialDto dto = new HistorialDto();
                    dto.setFechapago(fechaHoyIso());
                    dto.setPago(precio);
                    dto.setUsuario_idUsuario(idUsuario);
                    dto.setServicioId(idOriginal);
                    dto.setCursoId(null);

                    HistorialApi api = RetrofitClient.getClient().create(HistorialApi.class);
                    api.crear(dto).enqueue(new Callback<HistorialDto>() {
                        @Override
                        public void onResponse(Call<HistorialDto> call, Response<HistorialDto> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Clase registrada, abriendo calendario...", Toast.LENGTH_SHORT).show();
                                abrirCalendario();
                            } else {
                                Toast.makeText(getContext(), "Error al registrar clase (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<HistorialDto> call, Throwable t) {
                            Toast.makeText(getContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirCalendario() {
        if (getContext() == null) return;

        Calendar inicio = Calendar.getInstance();
        inicio.add(Calendar.DAY_OF_YEAR, 1);
        long startMillis = inicio.getTimeInMillis();

        Calendar fin = (Calendar) inicio.clone();
        fin.add(Calendar.HOUR_OF_DAY, 1);
        long endMillis = fin.getTimeInMillis();

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                .putExtra(CalendarContract.Events.TITLE, "Clase 1 a 1: " + titulo)
                .putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "Saber Share - Online")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No se encontro una app de calendario", Toast.LENGTH_SHORT).show();
        }
    }

    private String fechaHoyIso() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}

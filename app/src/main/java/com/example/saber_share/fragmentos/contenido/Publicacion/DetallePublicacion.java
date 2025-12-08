package com.example.saber_share.fragmentos.contenido.Publicacion;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Button;
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
import com.example.saber_share.util.local.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePublicacion extends Fragment {

    private int idAutor;
    private int idOriginal; // El ID real de la base de datos
    private String tipo, titulo, descripcion, autor, extra, calificacion;
    private double precio;

    private SessionManager sessionManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idAutor     = getArguments().getInt("idAutor");
            idOriginal  = getArguments().getInt("idOriginal");
            tipo        = getArguments().getString("tipo");
            titulo      = getArguments().getString("titulo");
            descripcion = getArguments().getString("descripcion");
            precio      = getArguments().getDouble("precio");
            autor       = getArguments().getString("autor");
            extra       = getArguments().getString("extra");
            calificacion= getArguments().getString("calificacion");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_detalle_publicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        int miId = sessionManager.getUserId();

        // 1. Llenar datos visuales
        ((TextView) view.findViewById(R.id.tvDetalleTitulo)).setText(titulo);
        ((TextView) view.findViewById(R.id.tvDetalleDescripcion)).setText(descripcion);
        ((TextView) view.findViewById(R.id.tvDetallePrecio)).setText(String.format("$ %.2f", precio));
        ((TextView) view.findViewById(R.id.tvDetalleAutor)).setText(autor);
        ((TextView) view.findViewById(R.id.tvDetalleCalif)).setText(calificacion + " ★");

        TextView tvLabelExtra = view.findViewById(R.id.tvLabelExtra);
        TextView tvExtra      = view.findViewById(R.id.tvDetalleExtra);

        if (Publicacion.TIPO_CURSO.equals(tipo)) {
            tvLabelExtra.setText("Archivo del curso:");
            tvExtra.setText(extra != null ? extra : "No disponible");
        } else {
            tvLabelExtra.setText("Requisitos para la clase:");
            tvExtra.setText(extra != null ? extra : "Sin requisitos");
        }

        // 2. Control de Paneles (Dueño vs Cliente)
        LinearLayout panelDueno   = view.findViewById(R.id.panelDueno);
        LinearLayout panelCliente = view.findViewById(R.id.panelClienteCompra);
        LinearLayout panelAlumno  = view.findViewById(R.id.panelAlumnoAcceso);
        Button btnAccion          = view.findViewById(R.id.btnAccionPrincipal);

        // Botón flotante atrás
        view.findViewById(R.id.fabAtras)
                .setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        if (miId == idAutor) {
            // SOY EL DUEÑO
            panelDueno.setVisibility(View.VISIBLE);
            panelCliente.setVisibility(View.GONE);
            panelAlumno.setVisibility(View.GONE);
            configurarPanelDueno(view);
        } else {
            // SOY CLIENTE
            panelDueno.setVisibility(View.GONE);
            // (luego puedes validar si ya compró para mostrar panelAlumno)
            panelCliente.setVisibility(View.VISIBLE);
            panelAlumno.setVisibility(View.GONE);

            if (Publicacion.TIPO_CURSO.equals(tipo)) {
                btnAccion.setText("Comprar Curso - " + String.format("$ %.2f", precio));
                btnAccion.setOnClickListener(v -> iniciarCompra());
            } else {
                btnAccion.setText("Agendar Clase");
                btnAccion.setOnClickListener(v -> confirmarYAgendarClase());
            }
        }
    }

    // =========================
    //   PANEL DEL DUEÑO
    // =========================

    private void configurarPanelDueno(View view) {
        // Botón Editar
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

        // Botón Eliminar
        view.findViewById(R.id.btnEliminar).setOnClickListener(v -> mostrarDialogoConfirmacion());

        // Botón Ver alumnos (a futuro puedes llamar /historial/curso/{idOriginal})
        view.findViewById(R.id.btnVerAlumnos).setOnClickListener(v ->
                Toast.makeText(getContext(), "Ver lista de alumnos (Próximamente)", Toast.LENGTH_SHORT).show());
    }

    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Publicación")
                .setMessage("¿Estás seguro de que deseas eliminar esto? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> ejecutarEliminacion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminacion() {
        if (Publicacion.TIPO_CURSO.equals(tipo)) {
            // Borrar Curso
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
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Borrar Servicio
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
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // =========================
    //   MÉTODOS DE CLIENTE
    // =========================

    // Compra de CURSO -> guarda en /historial
    private void iniciarCompra() {
        if (getContext() == null) return;

        int idUsuario = sessionManager.getUserId();

        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar compra")
                .setMessage("¿Quieres comprar el curso \"" + titulo + "\" por $" + String.format("%.2f", precio) + "?")
                .setPositiveButton("Comprar", (dialog, which) -> {

                    HistorialDto dto = new HistorialDto();
                    dto.setFechapago(fechaHoyIso());
                    dto.setPago(precio);
                    dto.setUsuario_idUsuario(idUsuario);
                    dto.setCursoId(idOriginal);   // curso
                    dto.setServicioId(null);      // no es servicio

                    HistorialApi api = RetrofitClient.getClient().create(HistorialApi.class);
                    api.crear(dto).enqueue(new Callback<HistorialDto>() {
                        @Override
                        public void onResponse(Call<HistorialDto> call, Response<HistorialDto> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Compra registrada correctamente", Toast.LENGTH_SHORT).show();
                                // Si quieres, regresas a inicio:
                                // Navigation.findNavController(requireView()).navigate(R.id.action_detallePublicacion_to_inicio);
                            } else {
                                Toast.makeText(
                                        getContext(),
                                        "Error al registrar compra (" + response.code() + ")",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<HistorialDto> call, Throwable t) {
                            Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Pregunta, guarda historial de SERVICIO y abre calendario
    private void confirmarYAgendarClase() {
        if (getContext() == null) return;

        int idUsuario = sessionManager.getUserId();

        new AlertDialog.Builder(getContext())
                .setTitle("Agendar clase")
                .setMessage("¿Quieres agendar esta clase 1 a 1 por $" + String.format("%.2f", precio) + "?")
                .setPositiveButton("Confirmar", (dialog, which) -> {

                    HistorialDto dto = new HistorialDto();
                    dto.setFechapago(fechaHoyIso());
                    dto.setPago(precio);
                    dto.setUsuario_idUsuario(idUsuario);
                    dto.setServicioId(idOriginal); // servicio
                    dto.setCursoId(null);          // no es curso

                    HistorialApi api = RetrofitClient.getClient().create(HistorialApi.class);
                    api.crear(dto).enqueue(new Callback<HistorialDto>() {
                        @Override
                        public void onResponse(Call<HistorialDto> call, Response<HistorialDto> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(
                                        getContext(),
                                        "Clase registrada, abriendo calendario...",
                                        Toast.LENGTH_SHORT
                                ).show();
                                abrirCalendario();
                            } else {
                                Toast.makeText(
                                        getContext(),
                                        "Error al registrar clase (" + response.code() + ")",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<HistorialDto> call, Throwable t) {
                            Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    // Abre la app de calendario con un evento prellenado para mañana
    private void abrirCalendario() {
        if (getContext() == null) return;

        Calendar inicio = Calendar.getInstance();
        inicio.add(Calendar.DAY_OF_YEAR, 1);  // mañana
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
            Toast.makeText(getContext(), "No se encontró una app de calendario", Toast.LENGTH_SHORT).show();
        }
    }

    // Fecha en formato yyyy-MM-dd (para LocalDate.parse en el backend)
    private String fechaHoyIso() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}

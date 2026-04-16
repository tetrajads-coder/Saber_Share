package com.example.saber_share.fragmentos.contenido.Publicacion;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.saber_share.model.CursoDto;
import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.util.api.CursoApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.ServicioApi;
import com.example.saber_share.util.local.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPublicacion extends Fragment {

    private int    idPublicacion;
    private String tipo, titulo, descripcion, extra;
    private double precio;

    private SessionManager sessionManager;

    // Campos comunes
    private TextInputEditText etTitulo, etDescripcion, etPrecio;
    private Button            btnGuardar;

    // Sección curso
    private LinearLayout      layoutSeccionCurso;
    private TextInputEditText etArchivo;

    // Sección clase
    private LinearLayout      layoutSeccionClase;
    private View              btnModalidadOnline, btnModalidadPresencial;
    private TextView          tvModalidadOnlineLabel, tvModalidadPresencialLabel;
    private TextInputEditText etDuracion, etFecha, etHora;
    private boolean           esOnline = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idPublicacion = getArguments().getInt("idOriginal", -1);
            tipo          = getArguments().getString("tipo", "CURSO");
            titulo        = getArguments().getString("titulo", "");
            descripcion   = getArguments().getString("descripcion", "");
            precio        = getArguments().getDouble("precio", 0.0);
            extra         = getArguments().getString("extra", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_editar_publicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = SessionManager.getInstance(requireContext());

        // Comunes
        etTitulo      = view.findViewById(R.id.et_edit_titulo);
        etDescripcion = view.findViewById(R.id.et_edit_descripcion);
        etPrecio      = view.findViewById(R.id.et_edit_precio);
        btnGuardar    = view.findViewById(R.id.btn_guardar_cambios);
        Button btnCancelar = view.findViewById(R.id.btn_cancelar_edicion);

        // Sección curso
        layoutSeccionCurso = view.findViewById(R.id.layout_edit_seccion_curso);
        etArchivo          = view.findViewById(R.id.et_edit_archivo);

        // Sección clase
        layoutSeccionClase         = view.findViewById(R.id.layout_edit_seccion_clase);
        btnModalidadOnline         = view.findViewById(R.id.btn_edit_modalidad_online);
        btnModalidadPresencial     = view.findViewById(R.id.btn_edit_modalidad_presencial);
        tvModalidadOnlineLabel     = view.findViewById(R.id.tv_edit_modalidad_online_label);
        tvModalidadPresencialLabel = view.findViewById(R.id.tv_edit_modalidad_presencial_label);
        etDuracion = view.findViewById(R.id.et_edit_duracion);
        etFecha    = view.findViewById(R.id.et_edit_fecha);
        etHora     = view.findViewById(R.id.et_edit_hora);

        // Poblar campos comunes
        etTitulo.setText(titulo);
        etDescripcion.setText(descripcion);
        etPrecio.setText(String.valueOf(precio));

        if ("CURSO".equals(tipo)) {
            layoutSeccionCurso.setVisibility(View.VISIBLE);
            layoutSeccionClase.setVisibility(View.GONE);
            etArchivo.setText(extra);
        } else {
            layoutSeccionCurso.setVisibility(View.GONE);
            layoutSeccionClase.setVisibility(View.VISIBLE);
            // Parsear extra: "EN_LINEA|60 min|2026-03-15|14:30"
            if (extra != null && extra.contains("|")) {
                String[] p = extra.split("\\|", -1);
                esOnline = !"PRESENCIAL".equals(p[0]);
                if (p.length > 1) etDuracion.setText(p[1]);
                if (p.length > 2) etFecha.setText(p[2]);
                if (p.length > 3) etHora.setText(p[3]);
            }
            seleccionarModalidad(esOnline);
        }

        // Listeners modalidad
        if (btnModalidadOnline    != null)
            btnModalidadOnline.setOnClickListener(v -> seleccionarModalidad(true));
        if (btnModalidadPresencial != null)
            btnModalidadPresencial.setOnClickListener(v -> seleccionarModalidad(false));

        // Date/Time pickers
        if (etFecha != null) etFecha.setOnClickListener(v -> mostrarDatePicker());
        if (etHora  != null) etHora.setOnClickListener(v -> mostrarTimePicker());

        btnCancelar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void seleccionarModalidad(boolean online) {
        esOnline = online;
        if (btnModalidadOnline    != null)
            btnModalidadOnline.setBackgroundResource(online
                    ? R.drawable.bg_tipo_selected : R.drawable.bg_tipo_unselected);
        if (btnModalidadPresencial != null)
            btnModalidadPresencial.setBackgroundResource(online
                    ? R.drawable.bg_tipo_unselected : R.drawable.bg_tipo_selected);
        if (tvModalidadOnlineLabel != null)
            tvModalidadOnlineLabel.setTextColor(requireContext().getColor(online
                    ? R.color.accent_primary : R.color.text_tertiary));
        if (tvModalidadPresencialLabel != null)
            tvModalidadPresencialLabel.setTextColor(requireContext().getColor(online
                    ? R.color.text_tertiary : R.color.accent_primary));
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (dp, y, m, d) ->
                etFecha.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void mostrarTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (tp, h, min) ->
                etHora.setText(String.format("%02d:%02d", h, min)),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
                .show();
    }

    private void guardarCambios() {
        String nuevoTitulo = getText(etTitulo);
        String nuevaDesc   = getText(etDescripcion);
        String precioStr   = getText(etPrecio);

        if (TextUtils.isEmpty(nuevoTitulo)) { etTitulo.setError("Requerido"); return; }
        if (TextUtils.isEmpty(precioStr))   { etPrecio.setError("Requerido"); return; }

        double nuevoPrecio;
        try { nuevoPrecio = Double.parseDouble(precioStr); }
        catch (NumberFormatException e) { etPrecio.setError("Precio inválido"); return; }

        int userId = sessionManager.getUsuarioId();
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        if ("CURSO".equals(tipo)) {
            actualizarCurso(userId, nuevoTitulo, nuevaDesc, nuevoPrecio, getText(etArchivo));
        } else {
            String fecha    = getText(etFecha);
            String hora     = getText(etHora);
            String duracion = getText(etDuracion);
            if (TextUtils.isEmpty(fecha)) { etFecha.setError("Requerida"); resetBoton(); return; }
            if (TextUtils.isEmpty(hora))  { etHora.setError("Requerida");  resetBoton(); return; }
            String nuevoExtra = (esOnline ? "EN_LINEA" : "PRESENCIAL")
                    + "|" + duracion + "|" + fecha + "|" + hora;
            actualizarServicio(userId, nuevoTitulo, nuevaDesc, nuevoPrecio, nuevoExtra, fecha, hora);
        }
    }

    private void actualizarCurso(int userId, String tit, String desc, double pre, String foto) {
        CursoDto curso = new CursoDto();
        curso.setTitulo(tit);
        curso.setDescripcion(desc);
        curso.setPrecio(pre);
        curso.setFoto(foto);
        curso.setUsuarioId(userId);
        curso.setCalificacion("0");

        RetrofitClient.getInstance().create(CursoApi.class)
                .updateCurso(idPublicacion, curso).enqueue(new Callback<CursoDto>() {
                    @Override
                    public void onResponse(@NonNull Call<CursoDto> call,
                                           @NonNull Response<CursoDto> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "¡Curso actualizado!", Toast.LENGTH_SHORT).show();
                            navegarAlDetalle("CURSO", tit, desc, pre, foto, userId);
                        } else mostrarError("Error al actualizar: " + response.code());
                    }
                    @Override
                    public void onFailure(@NonNull Call<CursoDto> call, @NonNull Throwable t) {
                        mostrarError("Fallo de conexión");
                    }
                });
    }

    private void actualizarServicio(int userId, String tit, String desc, double pre,
                                    String nuevoExtra, String fecha, String hora) {
        ServicioDto servicio = new ServicioDto();
        servicio.setTitulo(tit);
        servicio.setDescripcion(desc);
        servicio.setUsuarioId(userId);
        servicio.setPrecio(pre);
        servicio.setRequisitos(esOnline ? "EN_LINEA" : "PRESENCIAL");
        servicio.setFecha(fecha);
        servicio.setHora(hora + ":00");

        RetrofitClient.getInstance().create(ServicioApi.class)
                .updateServicio(idPublicacion, servicio).enqueue(new Callback<ServicioDto>() {
                    @Override
                    public void onResponse(@NonNull Call<ServicioDto> call,
                                           @NonNull Response<ServicioDto> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "¡Clase actualizada!", Toast.LENGTH_SHORT).show();
                            navegarAlDetalle("CLASE", tit, desc, pre, nuevoExtra, userId);
                        } else mostrarError("Error al actualizar: " + response.code());
                    }
                    @Override
                    public void onFailure(@NonNull Call<ServicioDto> call, @NonNull Throwable t) {
                        mostrarError("Fallo de conexión");
                    }
                });
    }

    private void navegarAlDetalle(String tipo, String tit, String desc,
                                  double pre, String ext, int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt("itemId",       idPublicacion);
        bundle.putInt("autorId",      userId);
        bundle.putString("tipo",      tipo);
        bundle.putString("titulo",    tit);
        bundle.putString("descripcion", desc);
        bundle.putDouble("precio",    pre);
        bundle.putString("autorNombre", sessionManager.getNombre());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_editarPublicacion_to_detallePublicacion, bundle);
    }

    private void mostrarError(String msg) {
        resetBoton();
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void resetBoton() {
        btnGuardar.setEnabled(true);
        btnGuardar.setText("Guardar cambios");
    }

    private String getText(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }
}
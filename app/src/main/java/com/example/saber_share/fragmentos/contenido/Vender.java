package com.example.saber_share.fragmentos.contenido;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.saber_share.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class Vender extends Fragment {

    // Tipo publicación
    private View     btnTipoCurso, btnTipoServicio;
    private TextView tvTipoCursoLabel, tvTipoServicioLabel;
    private boolean  esCurso = true;

    // Campos comunes
    private TextInputEditText etTitulo, etDescripcion, etPrecio;

    // Secciones dinámicas
    private LinearLayout layoutSeccionCurso, layoutSeccionClase;

    // Sección Curso
    private TextInputEditText      etNombreArchivo;
    private Button                 btnSeleccionarArchivo;
    private Uri                    archivoUri;

    // Sección Clase
    private View     btnModalidadOnline, btnModalidadPresencial;
    private TextView tvModalidadOnlineLabel, tvModalidadPresencialLabel;
    private TextInputEditText etDuracion, etFecha, etHora;
    private boolean esOnline = true;

    // Comunes
    private ProgressBar progressVender;
    private TextView    tvError;
    private Button      btnPublicar;

    // Selector de archivo
    private final ActivityResultLauncher<String[]> pickFileLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                archivoUri = uri;
                requireContext().getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String nombre = obtenerNombreArchivo(uri);
                etNombreArchivo.setText(nombre);
            });

    public Vender() { super(R.layout.fragment_main_vender); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tipo publicación
        btnTipoCurso        = view.findViewById(R.id.btn_tipo_curso);
        btnTipoServicio     = view.findViewById(R.id.btn_tipo_servicio);
        tvTipoCursoLabel    = view.findViewById(R.id.tv_tipo_curso_label);
        tvTipoServicioLabel = view.findViewById(R.id.tv_tipo_servicio_label);

        // Comunes
        etTitulo      = view.findViewById(R.id.et_titulo);
        etDescripcion = view.findViewById(R.id.et_descripcion);
        etPrecio      = view.findViewById(R.id.et_precio);
        progressVender = view.findViewById(R.id.progress_vender);
        tvError        = view.findViewById(R.id.tv_error_vender);
        btnPublicar    = view.findViewById(R.id.btn_publicar);

        // Secciones
        layoutSeccionCurso  = view.findViewById(R.id.layout_seccion_curso);
        layoutSeccionClase  = view.findViewById(R.id.layout_seccion_clase);

        // Curso
        etNombreArchivo      = view.findViewById(R.id.et_nombre_archivo);
        btnSeleccionarArchivo = view.findViewById(R.id.btn_seleccionar_archivo);

        // Clase
        btnModalidadOnline         = view.findViewById(R.id.btn_modalidad_online);
        btnModalidadPresencial     = view.findViewById(R.id.btn_modalidad_presencial);
        tvModalidadOnlineLabel     = view.findViewById(R.id.tv_modalidad_online_label);
        tvModalidadPresencialLabel = view.findViewById(R.id.tv_modalidad_presencial_label);
        etDuracion = view.findViewById(R.id.et_duracion);
        etFecha    = view.findViewById(R.id.et_fecha);
        etHora     = view.findViewById(R.id.et_hora);

        // Listeners tipo
        btnTipoCurso.setOnClickListener(v -> seleccionarTipo(true));
        btnTipoServicio.setOnClickListener(v -> seleccionarTipo(false));
        seleccionarTipo(true);

        // Archivo
        btnSeleccionarArchivo.setOnClickListener(v ->
                pickFileLauncher.launch(new String[]{"video/*", "application/pdf", "application/zip"}));

        // Modalidad
        btnModalidadOnline.setOnClickListener(v -> seleccionarModalidad(true));
        btnModalidadPresencial.setOnClickListener(v -> seleccionarModalidad(false));
        seleccionarModalidad(true);

        // DatePicker
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        view.findViewById(R.id.til_fecha).setOnClickListener(v -> mostrarDatePicker());

        // TimePicker
        etHora.setOnClickListener(v -> mostrarTimePicker());
        view.findViewById(R.id.til_hora).setOnClickListener(v -> mostrarTimePicker());

        btnPublicar.setOnClickListener(v -> irAResumen());
    }

    // ── Tipo de publicación ──────────────────────────────────────────────────

    private void seleccionarTipo(boolean curso) {
        esCurso = curso;

        btnTipoCurso.setBackgroundResource(curso
                ? R.drawable.bg_tipo_selected : R.drawable.bg_tipo_unselected);
        btnTipoServicio.setBackgroundResource(curso
                ? R.drawable.bg_tipo_unselected : R.drawable.bg_tipo_selected);

        tvTipoCursoLabel.setTextColor(requireContext().getColor(curso
                ? R.color.accent_primary : R.color.text_tertiary));
        tvTipoServicioLabel.setTextColor(requireContext().getColor(curso
                ? R.color.text_tertiary : R.color.accent_primary));

        layoutSeccionCurso.setVisibility(curso ? View.VISIBLE : View.GONE);
        layoutSeccionClase.setVisibility(curso ? View.GONE : View.VISIBLE);
    }

    // ── Modalidad clase ──────────────────────────────────────────────────────

    private void seleccionarModalidad(boolean online) {
        esOnline = online;
        btnModalidadOnline.setBackgroundResource(online
                ? R.drawable.bg_tipo_selected : R.drawable.bg_tipo_unselected);
        btnModalidadPresencial.setBackgroundResource(online
                ? R.drawable.bg_tipo_unselected : R.drawable.bg_tipo_selected);
        tvModalidadOnlineLabel.setTextColor(requireContext().getColor(online
                ? R.color.accent_primary : R.color.text_tertiary));
        tvModalidadPresencialLabel.setTextColor(requireContext().getColor(online
                ? R.color.text_tertiary : R.color.accent_primary));
    }

    // ── Date / Time Pickers ──────────────────────────────────────────────────

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
            etFecha.setText(String.format("%04d-%02d-%02d", year, month + 1, day));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void mostrarTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (timePicker, hour, minute) -> {
            etHora.setText(String.format("%02d:%02d", hour, minute));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
                .show();
    }

    // ── Validar y navegar a Resumen ──────────────────────────────────────────

    private void irAResumen() {
        tvError.setVisibility(View.GONE);

        String titulo      = getText(etTitulo);
        String descripcion = getText(etDescripcion);
        String precioStr   = getText(etPrecio);

        if (TextUtils.isEmpty(titulo))    { etTitulo.setError("Requerido"); return; }
        if (TextUtils.isEmpty(precioStr)) { etPrecio.setError("Requerido"); return; }

        double precio;
        try { precio = Double.parseDouble(precioStr); }
        catch (NumberFormatException e) { etPrecio.setError("Precio inválido"); return; }

        String extra = "";

        if (esCurso) {
            // Para curso: el "extra" es el nombre del archivo (la URL real se guardará en foto)
            extra = getText(etNombreArchivo);
        } else {
            // Para clase: extra = "modalidad|duración|fecha|hora"
            String duracion = getText(etDuracion);
            String fecha    = getText(etFecha);
            String hora     = getText(etHora);

            if (TextUtils.isEmpty(fecha)) { etFecha.setError("Selecciona una fecha"); return; }
            if (TextUtils.isEmpty(hora))  { etHora.setError("Selecciona una hora"); return; }

            extra = (esOnline ? "EN_LINEA" : "PRESENCIAL")
                    + "|" + duracion
                    + "|" + fecha
                    + "|" + hora;
        }

        Bundle bundle = new Bundle();
        bundle.putString("tipo",        esCurso ? "CURSO" : "CLASE");
        bundle.putString("titulo",      titulo);
        bundle.putString("descripcion", descripcion);
        bundle.putDouble("precio",      precio);
        bundle.putString("extra",       extra);

        Navigation.findNavController(requireView())
                .navigate(R.id.action_vender_to_resumenPublicacion, bundle);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String getText(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String obtenerNombreArchivo(Uri uri) {
        String nombre = "archivo";
        try (android.database.Cursor cursor = requireContext().getContentResolver()
                .query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) nombre = cursor.getString(idx);
            }
        } catch (Exception e) { nombre = uri.getLastPathSegment(); }
        return nombre;
    }
}
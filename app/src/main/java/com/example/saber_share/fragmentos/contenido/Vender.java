package com.example.saber_share.fragmentos.contenido;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.saber_share.R;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Vender extends Fragment {

    private ChipGroup cgTipoVenta;
    private EditText etTitulo, etDescripcion, etPrecio, etNombreArchivo, etRequisitos;
    private TextInputLayout tilRequisitos;
    private LinearLayout layoutArchivo;
    private Button btnSeleccionarArchivo, btnSiguiente;

    private boolean esCurso = true; // Por defecto iniciamos en "Curso"
    private String archivoSeleccionadoUri = "";

    // Lanzador para abrir el selector de archivos del celular
    private final ActivityResultLauncher<Intent> selectorArchivoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        archivoSeleccionadoUri = uri.toString();
                        // Mostramos solo el nombre del archivo, no toda la ruta fea
                        etNombreArchivo.setText(uri.getLastPathSegment());
                    }
                }
            }
    );

    public Vender() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_vender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Vincular todas las vistas
        cgTipoVenta = view.findViewById(R.id.cgTipoVenta);
        etTitulo = view.findViewById(R.id.etTitulo);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        etPrecio = view.findViewById(R.id.etPrecio);

        // Vistas dinámicas (cambian según el tipo)
        etNombreArchivo = view.findViewById(R.id.etNombreArchivo);
        layoutArchivo = view.findViewById(R.id.layoutArchivo);
        btnSeleccionarArchivo = view.findViewById(R.id.btnSeleccionarArchivo);

        etRequisitos = view.findViewById(R.id.etRequisitos);
        tilRequisitos = view.findViewById(R.id.tilRequisitos);

        btnSiguiente = view.findViewById(R.id.btnSiguiente);

        // 2. Lógica de los Chips (Botones de selección)
        cgTipoVenta.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipCurso)) {
                esCurso = true;
                // Si es curso: Muestra archivo, oculta requisitos
                layoutArchivo.setVisibility(View.VISIBLE);
                tilRequisitos.setVisibility(View.GONE);
            } else {
                esCurso = false;
                // Si es clase: Oculta archivo, muestra requisitos
                layoutArchivo.setVisibility(View.GONE);
                tilRequisitos.setVisibility(View.VISIBLE);
            }
        });

        // 3. Configurar botones
        btnSeleccionarArchivo.setOnClickListener(v -> abrirSelectorArchivos());
        btnSiguiente.setOnClickListener(v -> irAResumen());
    }

    private void abrirSelectorArchivos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Acepta cualquier tipo de archivo
        String[] mimeTypes = {"application/pdf", "video/mp4", "application/zip"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        selectorArchivoLauncher.launch(intent);
    }

    private void irAResumen() {
        // Recolectar datos
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String requisitos = etRequisitos.getText().toString().trim();

        // Validaciones básicas
        if (TextUtils.isEmpty(titulo)) { etTitulo.setError("Requerido"); return; }
        if (TextUtils.isEmpty(precioStr)) { etPrecio.setError("Requerido"); return; }

        // Validación especial: Si es curso, DEBE tener archivo
        if (esCurso && TextUtils.isEmpty(archivoSeleccionadoUri)) {
            Toast.makeText(getContext(), "Selecciona un archivo para el curso", Toast.LENGTH_SHORT).show();
            return;
        }

        // Empaquetar datos para mandarlos al siguiente fragmento
        Bundle bundle = new Bundle();
        bundle.putString("tipo", esCurso ? "CURSO" : "CLASE");
        bundle.putString("titulo", titulo);
        bundle.putString("descripcion", descripcion);
        try {
            bundle.putDouble("precio", Double.parseDouble(precioStr));
        } catch (NumberFormatException e) {
            etPrecio.setError("Precio inválido");
            return;
        }

        // El campo "extra" lleva el archivo O los requisitos, según el caso
        if (esCurso) {
            bundle.putString("extra", archivoSeleccionadoUri);
        } else {
            bundle.putString("extra", TextUtils.isEmpty(requisitos) ? "Sin requisitos específicos" : requisitos);
        }

        // ¡Vámonos al resumen!
        Navigation.findNavController(requireView()).navigate(R.id.action_vender_to_resumenPublicacion, bundle);
    }
}
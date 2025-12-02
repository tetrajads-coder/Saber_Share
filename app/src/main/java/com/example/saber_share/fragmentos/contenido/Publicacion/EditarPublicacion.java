package com.example.saber_share.fragmentos.contenido.Publicacion;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPublicacion extends Fragment {

    private int idPublicacion;
    private String tipo, titulo, descripcion, extra;
    private double precio;
    private SessionManager sessionManager;

    private EditText etTitulo, etDescripcion, etPrecio, etExtra;
    private Button btnGuardar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idPublicacion = getArguments().getInt("idOriginal");
            tipo = getArguments().getString("tipo");
            titulo = getArguments().getString("titulo");
            descripcion = getArguments().getString("descripcion");
            precio = getArguments().getDouble("precio");
            extra = getArguments().getString("extra");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_editar_publicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        etTitulo = view.findViewById(R.id.etEditTitulo);
        etDescripcion = view.findViewById(R.id.etEditDescripcion);
        etPrecio = view.findViewById(R.id.etEditPrecio);
        etExtra = view.findViewById(R.id.etEditExtra);
        btnGuardar = view.findViewById(R.id.btnGuardarCambios);
        Button btnCancelar = view.findViewById(R.id.btnCancelarEdicion);

        // Llenar campos
        etTitulo.setText(titulo);
        etDescripcion.setText(descripcion);
        etPrecio.setText(String.valueOf(precio));
        etExtra.setText(extra);

        if ("CLASE".equals(tipo)) {
            etExtra.setHint("Requisitos");
        } else {
            etExtra.setHint("Archivo (URL/URI)");
        }

        btnCancelar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void guardarCambios() {
        String nuevoTitulo = etTitulo.getText().toString().trim();
        String nuevaDesc = etDescripcion.getText().toString().trim();
        String nuevoPrecioStr = etPrecio.getText().toString().trim();
        String nuevoExtra = etExtra.getText().toString().trim();

        if (TextUtils.isEmpty(nuevoTitulo) || TextUtils.isEmpty(nuevoPrecioStr)) {
            Toast.makeText(getContext(), "Campos requeridos vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        double nuevoPrecio = Double.parseDouble(nuevoPrecioStr);
        int userId = sessionManager.getUserId();
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        if ("CURSO".equals(tipo)) {
            actualizarCurso(userId, nuevoTitulo, nuevaDesc, nuevoPrecio, nuevoExtra);
        } else {
            actualizarServicio(userId, nuevoTitulo, nuevaDesc, nuevoPrecio, nuevoExtra);
        }
    }

    private void actualizarCurso(int userId, String tit, String desc, double pre, String foto) {
        CursoDto curso = new CursoDto();
        curso.setTitulo(tit);
        curso.setDescripcion(desc);
        curso.setPrecio(pre);
        curso.setFoto(foto);
        curso.setUsuarioId(userId);
        curso.setCalificacion("0"); // Mantener calificación actual si es posible

        CursoApi api = RetrofitClient.getClient().create(CursoApi.class);
        api.updateCurso(idPublicacion, curso).enqueue(new Callback<CursoDto>() {
            @Override
            public void onResponse(Call<CursoDto> call, Response<CursoDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Actualizado!", Toast.LENGTH_SHORT).show();

                    // PREPARAMOS EL REGRESO AL DETALLE
                    Bundle bundle = new Bundle();
                    bundle.putInt("idOriginal", idPublicacion);
                    bundle.putInt("idAutor", userId); // El usuario actual es el autor
                    bundle.putString("tipo", "CURSO");

                    // Pasamos los datos NUEVOS que acabamos de enviar
                    bundle.putString("titulo", etTitulo.getText().toString());
                    bundle.putString("descripcion", etDescripcion.getText().toString());
                    bundle.putDouble("precio", Double.parseDouble(etPrecio.getText().toString()));
                    bundle.putString("extra", etExtra.getText().toString());
                    bundle.putString("autor", "Tu (Editado)"); // O mantenemos el nombre original si lo tienes guardado
                    bundle.putString("calificacion", "0"); // O la original

                    Navigation.findNavController(requireView()).navigate(R.id.action_editarPublicacion_to_detallePublicacion, bundle);
                } else {
                    mostrarError("Error al actualizar1");
                }
            }
            @Override
            public void onFailure(Call<CursoDto> call, Throwable t) {
                mostrarError("Fallo de conexión");
            }
        });
    }

    private void actualizarServicio(int userId, String tit, String desc, double pre, String req) {

        ServicioDto servicio = new ServicioDto();
        servicio.setTitulo(tit);
        servicio.setDescripcion(desc);
        servicio.setUsuarioId(userId);
        servicio.setPrecio(pre);
        servicio.setRequisitos(req);
        servicio.setUsuarioId(userId);
        servicio.setFecha("2025-01-01");
        servicio.setHora("12:00:00");

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        api.updateServicio(idPublicacion, servicio).enqueue(new Callback<ServicioDto>() {
            @Override
            public void onResponse(Call<ServicioDto> call, Response<ServicioDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Actualizado!", Toast.LENGTH_SHORT).show();

                    Bundle bundle = new Bundle();
                    bundle.putInt("idOriginal", idPublicacion);
                    bundle.putInt("idAutor", userId);
                    bundle.putString("tipo", "CLASE");

                    bundle.putString("titulo", etTitulo.getText().toString());
                    bundle.putString("descripcion", etDescripcion.getText().toString());
                    bundle.putDouble("precio", Double.parseDouble(etPrecio.getText().toString()));
                    bundle.putString("extra", etExtra.getText().toString());
                    bundle.putString("autor", "Tu (Editado)");
                    bundle.putString("calificacion", "N/A");

                    Navigation.findNavController(requireView()).navigate(R.id.action_editarPublicacion_to_detallePublicacion, bundle);
                } else {
                    mostrarError("Error al actualizar2");
                }
            }
            @Override
            public void onFailure(Call<ServicioDto> call, Throwable t) {
                mostrarError("Fallo de conexión");
            }
        });
    }

    private void mostrarError(String msg) {
        btnGuardar.setEnabled(true);
        btnGuardar.setText("Guardar Cambios");
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
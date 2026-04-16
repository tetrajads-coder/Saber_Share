package com.example.saber_share.fragmentos.cuenta;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.saber_share.R;
import com.example.saber_share.model.UsuarioDto;
import com.example.saber_share.util.repository.UsuarioRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroSesion extends Fragment {

    private TextInputLayout   tilUsuario, tilNombre, tilApellido, tilCorreo, tilTelefono, tilPass;
    private TextInputEditText etUsuario, etNombre, etApellido, etCorreo, etTelefono, etPassword;
    private Button            btnRegistrarse, btnIniciarSesion;
    private ProgressBar       progressRegistro;
    private TextView          tvError;

    private UsuarioRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuenta_registro_sesion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new UsuarioRepository(requireContext());

        tilUsuario   = view.findViewById(R.id.tilUsuario);
        tilNombre    = view.findViewById(R.id.tilNombre);
        tilApellido  = view.findViewById(R.id.tilApellido);
        tilCorreo    = view.findViewById(R.id.tilCorreo);
        tilTelefono  = view.findViewById(R.id.tilTelefono);
        tilPass      = view.findViewById(R.id.tilPass);

        etUsuario    = view.findViewById(R.id.etUsuario);
        etNombre     = view.findViewById(R.id.etNombre);
        etApellido   = view.findViewById(R.id.etApellido);
        etCorreo     = view.findViewById(R.id.etCorreo);
        etTelefono   = view.findViewById(R.id.etTelefono);
        etPassword   = view.findViewById(R.id.etPassword);

        btnRegistrarse   = view.findViewById(R.id.btnRegistrarse);
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion);
        progressRegistro = view.findViewById(R.id.progress_registro);
        tvError          = view.findViewById(R.id.tv_error_registro);

        btnRegistrarse.setOnClickListener(v -> intentarRegistro());
        btnIniciarSesion.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_registroSesion_to_inicioSesion));
    }

    private void intentarRegistro() {
        limpiarErrores();
        ocultarError();

        String usuario  = getText(etUsuario);
        String nombre   = getText(etNombre);
        String apellido = getText(etApellido);
        String correo   = getText(etCorreo);
        String telefono = getText(etTelefono);
        String password = getText(etPassword);

        if (TextUtils.isEmpty(usuario)) {
            tilUsuario.setError("Ingresa un nombre de usuario"); return;
        }
        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("Ingresa tu nombre"); return;
        }
        if (TextUtils.isEmpty(apellido)) {
            tilApellido.setError("Ingresa tu apellido"); return;
        }
        if (TextUtils.isEmpty(correo) ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Ingresa un correo válido"); return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            tilPass.setError("Mínimo 6 caracteres"); return;
        }

        setLoading(true);

        UsuarioDto nuevoUsuario = new UsuarioDto();
        nuevoUsuario.setUser(usuario);
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setApellido(apellido);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setTelefono(telefono);
        nuevoUsuario.setPassword(password);

        repository.registrarUsuario(nuevoUsuario, new Callback<UsuarioDto>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioDto> call,
                                   @NonNull Response<UsuarioDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    repository.guardarSesion(response.body());
                    Toast.makeText(requireContext(),
                            "¡Bienvenido, " + response.body().getNombre() + "!",
                            Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_registroSesion_to_main);
                } else {
                    mostrarError("Error al crear cuenta (código " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioDto> call, @NonNull Throwable t) {
                setLoading(false);
                mostrarError("Sin conexión: " + t.getMessage());
            }
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        progressRegistro.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegistrarse.setEnabled(!loading);
        btnIniciarSesion.setEnabled(!loading);
    }

    private void mostrarError(String msg) {
        if (tvError != null) { tvError.setText(msg); tvError.setVisibility(View.VISIBLE); }
    }

    private void ocultarError() {
        if (tvError != null) tvError.setVisibility(View.GONE);
    }

    private void limpiarErrores() {
        tilUsuario.setError(null);
        tilNombre.setError(null);
        tilApellido.setError(null);
        tilCorreo.setError(null);
        tilPass.setError(null);
    }
}
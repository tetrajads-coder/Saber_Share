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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioSesion extends Fragment {

    private TextInputLayout   tilUsuario, tilPassword;
    private TextInputEditText etUsuario, etPassword;
    private Button            btnLogin, btnIrRegistro;
    private ProgressBar       progressLogin;
    private TextView          tvError;

    private UsuarioRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuenta_inicio_sesion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new UsuarioRepository(requireContext());

        tilUsuario    = view.findViewById(R.id.til_email);      // reutilizamos el mismo TIL
        tilPassword   = view.findViewById(R.id.til_password);
        etUsuario     = view.findViewById(R.id.et_email);       // reutilizamos el mismo ET
        etPassword    = view.findViewById(R.id.et_password);
        btnLogin      = view.findViewById(R.id.btn_login);
        btnIrRegistro = view.findViewById(R.id.btn_ir_registro);
        progressLogin = view.findViewById(R.id.progress_login);
        tvError       = view.findViewById(R.id.tv_error_login);

        // Cambiar hint a "Usuario"
        tilUsuario.setHint("Nombre de usuario");

        btnLogin.setOnClickListener(v -> intentarLogin());
        btnIrRegistro.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_inicioSesion_to_registroSesion));
    }

    private void intentarLogin() {
        ocultarError();

        String usuario  = getText(etUsuario);
        String password = getText(etPassword);

        if (TextUtils.isEmpty(usuario)) {
            tilUsuario.setError("Ingresa tu nombre de usuario");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Ingresa tu contraseña");
            return;
        }

        setLoading(true);

        repository.verificarUsuario(usuario, new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<UsuarioDto>> call,
                                   @NonNull Response<List<UsuarioDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    validarPasswordYEntrar(response.body().get(0), password);
                } else {
                    setLoading(false);
                    mostrarError("Usuario no encontrado");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UsuarioDto>> call, @NonNull Throwable t) {
                setLoading(false);
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void validarPasswordYEntrar(UsuarioDto usuario, String passIngresada) {
        if (passIngresada.equals(usuario.getPassword())) {
            repository.guardarSesion(usuario);
            Toast.makeText(requireContext(),
                    "¡Bienvenido, " + usuario.getNombre() + "!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_inicioSesion_to_main);
        } else {
            setLoading(false);
            mostrarError("Contraseña incorrecta");
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        if (progressLogin != null) progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (btnLogin      != null) btnLogin.setEnabled(!loading);
        if (btnIrRegistro != null) btnIrRegistro.setEnabled(!loading);
    }

    private void mostrarError(String mensaje) {
        if (tvError != null) { tvError.setText(mensaje); tvError.setVisibility(View.VISIBLE); }
    }

    private void ocultarError() {
        if (tvError    != null) tvError.setVisibility(View.GONE);
        if (tilUsuario != null) tilUsuario.setError(null);
        if (tilPassword!= null) tilPassword.setError(null);
    }
}
package com.example.saber_share.fragmentos.cuenta;

import android.content.Intent;
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

import com.example.saber_share.MainActivity;
import com.example.saber_share.R;
import com.example.saber_share.model.UsuarioDto;
import com.example.saber_share.util.repository.UsuarioRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioSesion extends Fragment implements View.OnClickListener {

    EditText etCorreo;
    EditText etPassword;
    Button btnIniciarSesion;
    Button btnRegistrarse;

    private UsuarioRepository repository;

    public InicioSesion() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new UsuarioRepository(requireContext());

        etCorreo = view.findViewById(R.id.etCorreo);
        etPassword = view.findViewById(R.id.etPassword);
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion);
        btnRegistrarse = view.findViewById(R.id.btnRegistrarse);

        btnIniciarSesion.setOnClickListener(this);
        btnRegistrarse.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuenta_inicio_sesion, container, false);
    }

    @Override
    public void onClick(View view) {
        String opcion = ((Button) view).getText().toString();

        if (opcion.equals("Iniciar sesión")) {
            intentarLogin();
        } else if (opcion.equals("Registrarse")) {
            Navigation.findNavController(view).navigate(R.id.action_inicioSesion_to_registroSesion);
        }
    }

    private void intentarLogin() {
        String usuarioInput = etCorreo.getText().toString().trim();
        String passwordInput = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(usuarioInput)) {
            etCorreo.setError("Ingresa tu usuario");
            return;
        }
        if (TextUtils.isEmpty(passwordInput)) {
            etPassword.setError("Ingresa tu contraseña");
            return;
        }

        btnIniciarSesion.setEnabled(false);
        btnIniciarSesion.setText("Cargando...");

        repository.verificarUsuario(usuarioInput, new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                btnIniciarSesion.setEnabled(true);
                btnIniciarSesion.setText("Iniciar sesión");

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    UsuarioDto u = response.body().get(0);
                    if (passwordInput.equals(u.getPassword())) {
                        repository.guardarSesion(u.getCorreo(), u.getPassword(), u.getId());
                        irAlMain();
                    } else {
                        Toast.makeText(getContext(), "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                btnIniciarSesion.setEnabled(true);
                btnIniciarSesion.setText("Iniciar sesión");
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irAlMain() {
        Toast.makeText(getContext(), "Bienvenido", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
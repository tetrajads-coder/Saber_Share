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

    EditText etCorreo; // Este campo acepta Usuario o Correo
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
        int id = view.getId();

        if (id == R.id.btnIniciarSesion) {
            intentarLogin();
        } else if (id == R.id.btnRegistrarse) {
            Navigation.findNavController(view).navigate(R.id.action_inicioSesion_to_registroSesion);
        }
    }

    private void intentarLogin() {
        String input = etCorreo.getText().toString().trim();
        String passwordInput = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            etCorreo.setError("Ingresa tu usuario o correo");
            return;
        }
        if (TextUtils.isEmpty(passwordInput)) {
            etPassword.setError("Ingresa tu contraseña");
            return;
        }

        btnIniciarSesion.setEnabled(false);
        btnIniciarSesion.setText("Cargando...");

        // Intentamos buscar por NOMBRE DE USUARIO (usu_usu)
        repository.verificarUsuario(input, new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    validarPasswordYEntrar(response.body().get(0), passwordInput);
                } else {
                    // Si no lo encuentra por usuario, intentamos por CORREO
                    intentarLoginPorCorreo(input, passwordInput);
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                desbloquearBoton();
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void intentarLoginPorCorreo(String correo, String pass) {
        repository.verificarCorreo(correo, new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                desbloquearBoton();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    validarPasswordYEntrar(response.body().get(0), pass);
                } else {
                    Toast.makeText(getContext(), "Usuario o correo no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                desbloquearBoton();
                Toast.makeText(getContext(), "Error al validar correo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validarPasswordYEntrar(UsuarioDto u, String passIngresada) {
        if (passIngresada.equals(u.getPassword())) {
            repository.guardarSesion(u);
            irAlMain();
        } else {
            desbloquearBoton();
            Toast.makeText(getContext(), "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
        }
    }

    private void desbloquearBoton() {
        btnIniciarSesion.setEnabled(true);
        btnIniciarSesion.setText("Iniciar sesión");
    }

    private void irAlMain() {
        Toast.makeText(getContext(), "Bienvenido", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
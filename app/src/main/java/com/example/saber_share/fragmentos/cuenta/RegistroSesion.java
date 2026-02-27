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

public class RegistroSesion extends Fragment implements View.OnClickListener {

    EditText etUsuario, etNombre, etApellido, etCorreo, etTelefono, etPassword;
    Button btnRegistrarse, btnIniciarSesion;

    private UsuarioRepository repository;

    public RegistroSesion() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuenta_registro_sesion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new UsuarioRepository(requireContext());

        etUsuario = view.findViewById(R.id.etUsuario);
        etNombre = view.findViewById(R.id.etNombre);
        etApellido = view.findViewById(R.id.etApellido);
        etCorreo = view.findViewById(R.id.etCorreo);
        etTelefono = view.findViewById(R.id.etTelefono);
        etPassword = view.findViewById(R.id.etPassword);
        btnRegistrarse = view.findViewById(R.id.btnRegistrarse);
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion);

        btnRegistrarse.setOnClickListener(this);
        btnIniciarSesion.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnRegistrarse) {
            validarDatosYProceder();
        } else if (id == R.id.btnIniciarSesion) {
            Navigation.findNavController(view).navigate(R.id.action_registroSesion_to_inicioSesion);
        }
    }

    private void validarDatosYProceder() {
        String usuario = etUsuario.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(usuario)) { etUsuario.setError("Requerido"); return; }
        if (TextUtils.isEmpty(nombre)) { etNombre.setError("Requerido"); return; }

        btnRegistrarse.setEnabled(false);
        btnRegistrarse.setText("Validando...");

        repository.verificarUsuario(usuario, new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                boolean usuarioExiste = false;
                if (response.isSuccessful() && response.body() != null) {
                    for (UsuarioDto u : response.body()) {
                        if (u.getUser().equalsIgnoreCase(usuario)) {
                            usuarioExiste = true; break;
                        }
                    }
                }

                if (usuarioExiste) {
                    desbloquearBoton();
                    etUsuario.setError("Usuario ya en uso");
                } else {
                    validarCorreo(usuario, nombre, apellido, correo, telefono, password);
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                desbloquearBoton();
                mostrarError("Error al validar usuario");
            }
        });
    }

    private void validarCorreo(String user, String nom, String ape, String mail, String tel, String pass) {
        repository.verificarCorreo(mail, new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                boolean correoExiste = false;
                if (response.isSuccessful() && response.body() != null) {
                    for (UsuarioDto u : response.body()) {
                        if (u.getCorreo() != null && u.getCorreo().equalsIgnoreCase(mail)) {
                            correoExiste = true; break;
                        }
                    }
                }

                if (correoExiste) {
                    desbloquearBoton();
                    etCorreo.setError("Correo ya registrado");
                } else {
                    realizarRegistro(user, nom, ape, mail, tel, pass);
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                desbloquearBoton();
                mostrarError("Error al validar correo");
            }
        });
    }

    private void realizarRegistro(String user, String nom, String ape, String mail, String tel, String pass) {
        UsuarioDto nuevo = new UsuarioDto();
        nuevo.setUser(user);
        nuevo.setNombre(nom);
        nuevo.setApellido(ape);
        nuevo.setCorreo(mail);
        nuevo.setTelefono(tel);
        nuevo.setPassword(pass);

        repository.registrarUsuario(nuevo, new Callback<UsuarioDto>() {
            @Override
            public void onResponse(Call<UsuarioDto> call, Response<UsuarioDto> response) {
                desbloquearBoton();
                if (response.isSuccessful() && response.body() != null) {
                    int nuevoId = response.body().getId();
                    // SE GUARDA EL NOMBRE TAMBIÉN
                    repository.guardarSesion(user, pass, nuevoId, nom);
                    irAlMain();
                } else {
                    mostrarError("Error al registrar");
                }
            }

            @Override
            public void onFailure(Call<UsuarioDto> call, Throwable t) {
                desbloquearBoton();
                mostrarError("Error de conexión");
            }
        });
    }

    private void irAlMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void desbloquearBoton() {
        if(btnRegistrarse != null) {
            btnRegistrarse.setEnabled(true);
            btnRegistrarse.setText("Registrarse");
        }
    }

    private void mostrarError(String msg) {
        if(getContext() != null)
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
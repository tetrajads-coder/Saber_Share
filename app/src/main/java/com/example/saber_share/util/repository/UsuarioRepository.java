package com.example.saber_share.util.repository;

import android.content.Context;

import com.example.saber_share.model.UsuarioDto;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.UsuarioApi;
import com.example.saber_share.util.local.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class UsuarioRepository {
    private final UsuarioApi api;
    private final SessionManager sessionManager;

    public UsuarioRepository(Context context) {
        this.api = RetrofitClient.getClient().create(UsuarioApi.class);
        this.sessionManager = new SessionManager(context);
    }

    public void verificarUsuario(String usuario, Callback<List<UsuarioDto>> callback) {
        Call<List<UsuarioDto>> call = api.login(usuario);
        call.enqueue(callback);
    }

    public void verificarCorreo(String correo, Callback<List<UsuarioDto>> callback) {
        Call<List<UsuarioDto>> call = api.BuscaCorreo(correo);
        call.enqueue(callback);
    }

    public void registrarUsuario(UsuarioDto nuevoUsuario, Callback<UsuarioDto> callback) {
        Call<UsuarioDto> call = api.registrar(nuevoUsuario);
        call.enqueue(callback);
    }

    public void guardarSesion(String usuario, String password, int id) {
        sessionManager.createLoginSession(usuario, password, id);
    }
}
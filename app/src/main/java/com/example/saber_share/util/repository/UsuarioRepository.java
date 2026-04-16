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
        this.api = RetrofitClient.getInstance().create(UsuarioApi.class);
        this.sessionManager = SessionManager.getInstance(context);
    }

    // Busca por nombre de usuario → GET /api/usuario?user=xxx
    public void verificarUsuario(String user, Callback<List<UsuarioDto>> callback) {
        api.buscarPorUsuario(user).enqueue(callback);
    }

    // Busca por correo → GET /api/usuario?correo=xxx
    public void verificarCorreo(String correo, Callback<List<UsuarioDto>> callback) {
        api.buscarPorCorreo(correo).enqueue(callback);
    }

    // Registrar → POST /api/usuario
    public void registrarUsuario(UsuarioDto nuevoUsuario, Callback<UsuarioDto> callback) {
        api.registrar(nuevoUsuario).enqueue(callback);
    }

    public void guardarSesion(UsuarioDto dto) {
        sessionManager.guardarSesion(
                dto.getId(),
                dto.getNombre(),
                dto.getCorreo(),
                "USUARIO"
        );
    }
}
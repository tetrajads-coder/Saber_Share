package com.example.saber_share.util.api;

import com.example.saber_share.model.UsuarioDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UsuarioApi {

    @GET("api/usuario")
    Call<List<UsuarioDto>> login(@Query("user") String user);

    @GET("api/usuario")
    Call<List<UsuarioDto>> BuscaCorreo(@Query("correo") String correo);

    @POST("api/usuario")
    Call<UsuarioDto> registrar(@Body UsuarioDto usuario);

    // NUEVO: GET /api/usuario/{id}
    @GET("api/usuario/{id}")
    Call<UsuarioDto> obtenerPorId(@Path("id") int id);
}

package com.example.saber_share.util.api;

import com.example.saber_share.model.UsuarioDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UsuarioApi {

    @GET("usuario")
    Call<List<UsuarioDto>> login(
            @Query("user") String user
    );

    @GET("usuario")
    Call<List<UsuarioDto>> BuscaCorreo(@Query("correo") String correo);

    @POST("usuario")
    Call<UsuarioDto> registrar(
            @Body UsuarioDto usuario
    );
}
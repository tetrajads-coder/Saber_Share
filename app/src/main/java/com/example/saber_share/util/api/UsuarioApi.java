package com.example.saber_share.util.api;

import com.example.saber_share.model.UsuarioDto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UsuarioApi {

    @GET("usuario")
    Call<List<UsuarioDto>> buscarPorUsuario(@Query("user") String user);

    @GET("usuario")
    Call<List<UsuarioDto>> buscarPorCorreo(@Query("correo") String correo);

    @GET("usuario/{id}")
    Call<UsuarioDto> getById(@Path("id") int id);

    @POST("usuario")
    Call<UsuarioDto> registrar(@Body UsuarioDto dto);

    @PUT("usuario/{id}")
    Call<UsuarioDto> actualizar(@Path("id") int id, @Body UsuarioDto dto);

    @DELETE("usuario/{id}")
    Call<Void> eliminar(@Path("id") int id);

    // Saldo y correo PayPal del vendedor
    @GET("usuario/{id}/saldo")
    Call<Map<String, Object>> getSaldo(@Path("id") int id);

    @PUT("usuario/{id}/correo-paypal")
    Call<Map<String, Object>> actualizarCorreoPaypal(
            @Path("id") int id,
            @Body Map<String, String> body);
}
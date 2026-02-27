package com.example.saber_share.util.api;

import com.example.saber_share.model.MetodoDePagoDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MetodoPagoApi {

    @GET("api/metodoPago")
    Call<List<MetodoDePagoDto>> listarTarjetas(@Query("idUsuario") int idUsuario);

    @GET("api/metodoPago/{id}")
    Call<MetodoDePagoDto> obtenerTarjeta(@Path("id") int id);

    @POST("api/metodoPago")
    Call<MetodoDePagoDto> crearTarjeta(@Body MetodoDePagoDto tarjeta);

    @PUT("api/metodoPago/{id}")
    Call<MetodoDePagoDto> actualizarTarjeta(@Path("id") int id, @Body MetodoDePagoDto tarjeta);

    @DELETE("api/metodoPago/{id}")
    Call<Void> eliminarTarjeta(@Path("id") int id);
}

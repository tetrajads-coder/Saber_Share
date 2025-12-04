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

public interface MetodoPagoApi {

    @GET("metodoPago")
    Call<List<MetodoDePagoDto>> listarTarjetas();

    @GET("metodoPago/{id}")
    Call<MetodoDePagoDto> obtenerTarjeta(@Path("id") int id);

    @POST("metodoPago")
    Call<MetodoDePagoDto> crearTarjeta(@Body MetodoDePagoDto tarjeta);

    @PUT("metodoPago/{id}")
    Call<MetodoDePagoDto> actualizarTarjeta(@Path("id") int id, @Body MetodoDePagoDto tarjeta);

    @DELETE("metodoPago/{id}")
    Call<Void> eliminarTarjeta(@Path("id") int id);
}
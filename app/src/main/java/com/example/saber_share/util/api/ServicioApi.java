package com.example.saber_share.util.api;

import com.example.saber_share.model.ServicioDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ServicioApi {
    @GET("servicio")          Call<List<ServicioDto>> lista();
    @GET("servicio/{id}")     Call<ServicioDto> getById(@Path("id") int id);
    @POST("servicio")         Call<ServicioDto> crearServicio(@Body ServicioDto servicio);
    @PUT("servicio/{id}")     Call<ServicioDto> updateServicio(@Path("id") int id, @Body ServicioDto servicio);
    @DELETE("servicio/{id}")  Call<Void> deleteServicio(@Path("id") int id);
}
package com.example.saber_share.util.api;

import com.example.saber_share.model.HistorialDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface HistorialApi {

    // GET /Saber_Share/api/historial
    @GET("historial")
    Call<List<HistorialDto>> lista();

    // GET /Saber_Share/api/historial/{id}
    @GET("historial/{id}")
    Call<HistorialDto> getById(@Path("id") int id);

    // GET /Saber_Share/api/historial/usuario/{usuarioId}
    @GET("historial/usuario/{usuarioId}")
    Call<List<HistorialDto>> historialPorUsuario(@Path("usuarioId") int usuarioId);

    // POST /Saber_Share/api/historial
    @POST("historial")
    Call<HistorialDto> crear(@Body HistorialDto historial);
}

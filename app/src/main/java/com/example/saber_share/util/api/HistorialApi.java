package com.example.saber_share.util.api;

import com.example.saber_share.model.HistorialDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface HistorialApi {
    @GET("historial")                          Call<List<HistorialDto>> lista();
    @GET("historial/{id}")                     Call<HistorialDto> getById(@Path("id") int id);
    @GET("historial/curso/{idCurso}")
    Call<List<HistorialDto>> listarPorCurso(@Path("idCurso") int idCurso);
    @GET("historial/usuario/{usuarioId}")
    Call<List<HistorialDto>> historialPorUsuario(@Path("usuarioId") int usuarioId);
    @GET("historial/servicio/{idServicio}")
    Call<List<HistorialDto>> listarPorServicio(@Path("idServicio") int idServicio);
    @POST("historial")                         Call<HistorialDto> crear(@Body HistorialDto historial);
}
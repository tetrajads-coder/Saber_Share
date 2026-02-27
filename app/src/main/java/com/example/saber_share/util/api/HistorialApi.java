package com.example.saber_share.util.api;

import com.example.saber_share.model.HistorialDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface HistorialApi {

    @GET("api/historial")
    Call<List<HistorialDto>> lista();

    @GET("api/historial/{id}")
    Call<HistorialDto> getById(@Path("id") int id);

    @GET("api/historial/curso/{idCurso}")
    Call<List<HistorialDto>> listarPorCurso(@Path("idCurso") int idCurso);

    @GET("api/historial/usuario/{usuarioId}")
    Call<List<HistorialDto>> historialPorUsuario(@Path("usuarioId") int usuarioId);

    @GET("api/historial/servicio/{idServicio}")
    Call<List<HistorialDto>> listarPorServicio(@Path("idServicio") int idServicio);

    @POST("api/historial")
    Call<HistorialDto> crear(@Body HistorialDto historial);

}

package com.example.saber_share.util.api;

import com.example.saber_share.model.OpinionServicioDto;
import com.example.saber_share.model.OpinionesCursoDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface OpinionApi {
    @GET("opinion_servicio/servicio/{id}")
    Call<List<OpinionServicioDto>> getOpinionesServicio(@Path("id") int idServicio);

    @POST("opinion_servicio")
    Call<OpinionServicioDto> calificarServicio(@Body OpinionServicioDto dto);

    @GET("opiniones/cursos/{id}")          // ← CORREGIDO
    Call<List<OpinionesCursoDto>> getOpinionesCurso(@Path("id") int idCurso);

    @POST("opiniones/cursos")              // ← CORREGIDO
    Call<OpinionesCursoDto> calificarCurso(@Body OpinionesCursoDto dto);
}
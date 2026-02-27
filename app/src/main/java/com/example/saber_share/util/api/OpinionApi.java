package com.example.saber_share.util.api;

import com.example.saber_share.model.OpinionServicioDto;
import com.example.saber_share.model.OpinionesCursoDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OpinionApi {

    // SERVICIOS (Clase 1 a 1)
    @GET("api/opinion_servicio/servicio/{id}")
    Call<List<OpinionServicioDto>> getOpinionesServicio(@Path("id") int idServicio);

    @POST("api/opinion_servicio")
    Call<OpinionServicioDto> calificarServicio(@Body OpinionServicioDto dto);

    // CURSOS
    @GET("api/opiniones_curso/curso/{id}")
    Call<List<OpinionesCursoDto>> getOpinionesCurso(@Path("id") int idCurso);

    @POST("api/opiniones_curso")
    Call<OpinionesCursoDto> calificarCurso(@Body OpinionesCursoDto dto);
}

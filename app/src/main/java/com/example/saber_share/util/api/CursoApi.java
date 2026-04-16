package com.example.saber_share.util.api;

import com.example.saber_share.model.CursoDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface CursoApi {
    @GET("curso")          Call<List<CursoDto>> lista();
    @GET("curso/{id}")     Call<CursoDto> getById(@Path("id") int id);
    @POST("curso")         Call<CursoDto> crearCurso(@Body CursoDto curso);
    @PUT("curso/{id}")     Call<CursoDto> updateCurso(@Path("id") int id, @Body CursoDto curso);
    @DELETE("curso/{id}")  Call<Void> deleteCurso(@Path("id") int id);
    @GET("curso")          Call<List<CursoDto>> buscaCorreo(@Query("correo") String correo);
}
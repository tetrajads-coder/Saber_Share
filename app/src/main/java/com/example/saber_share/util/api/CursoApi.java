package com.example.saber_share.util.api;

import com.example.saber_share.model.CursoDto;
import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.model.UsuarioDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CursoApi {

    @GET("curso")
    Call<List<CursoDto>> login(
            @Query("user") String user
    );
    @GET("curso")
    Call<List<CursoDto>> BuscaCorreo(@Query("correo") String correo);
    @POST("curso")
    Call<CursoDto> crearCurso(@Body CursoDto curso);

    @GET("curso")
    Call<List<CursoDto>> lista();

    @PUT("curso/{id}")
    Call<CursoDto> updateCurso(@Path("id") int id, @Body CursoDto curso);

    @DELETE("curso/{id}")
    Call<Void> deleteCurso(@Path("id") int id);

}

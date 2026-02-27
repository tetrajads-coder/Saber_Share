package com.example.saber_share.util.api;

import com.example.saber_share.model.CursoDto;
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

    // GET /api/curso
    @GET("api/curso")
    Call<List<CursoDto>> lista();

    // GET /api/curso/{id}
    @GET("api/curso/{id}")
    Call<CursoDto> getById(@Path("id") int id);

    // POST /api/curso
    @POST("api/curso")
    Call<CursoDto> crearCurso(@Body CursoDto curso);

    // PUT /api/curso/{id}
    @PUT("api/curso/{id}")
    Call<CursoDto> updateCurso(@Path("id") int id, @Body CursoDto curso);

    // DELETE /api/curso/{id}
    @DELETE("api/curso/{id}")
    Call<Void> deleteCurso(@Path("id") int id);

    // (Opcional) si quieres buscar por correo con tu backend:
    // GET /api/curso?correo=....
    @GET("api/curso")
    Call<List<CursoDto>> buscaCorreo(@Query("correo") String correo);
}

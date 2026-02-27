package com.example.saber_share.util.api;

import com.example.saber_share.model.AgendaDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AgendaApi {

    @GET("Saber_Share/api/agenda/servicio/{idServicio}")
    Call<List<AgendaDto>> getSlotsPorServicio(@Path("idServicio") int idServicio);

    @GET("Saber_Share/api/agenda/usuario/{idUsuario}")
    Call<List<AgendaDto>> getMisAgendas(@Path("idUsuario") int idUsuario);

    @POST("Saber_Share/api/agenda")
    Call<AgendaDto> crearSlot(@Body AgendaDto slot);

    @PUT("Saber_Share/api/agenda/reservar/{idAgenda}")
    Call<AgendaDto> reservarSlot(@Path("idAgenda") int idAgenda, @Query("idAlumno") int idAlumno);
}

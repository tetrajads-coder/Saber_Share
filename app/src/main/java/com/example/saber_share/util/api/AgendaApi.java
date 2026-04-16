package com.example.saber_share.util.api;

import com.example.saber_share.model.AgendaDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AgendaApi {

    @GET("agenda/servicio/{idServicio}")
    Call<List<AgendaDto>> getSlotsPorServicio(@Path("idServicio") int idServicio);

    @GET("agenda/usuario/{idUsuario}")
    Call<List<AgendaDto>> getMisAgendas(@Path("idUsuario") int idUsuario);

    @POST("agenda")
    Call<AgendaDto> crearSlot(@Body AgendaDto slot);

    @PUT("agenda/reservar/{idAgenda}")
    Call<AgendaDto> reservarSlot(@Path("idAgenda") int idAgenda, @Query("idAlumno") int idAlumno);

    @DELETE("agenda/{idAgenda}")
    Call<Void> eliminarSlot(@Path("idAgenda") int idAgenda);
}
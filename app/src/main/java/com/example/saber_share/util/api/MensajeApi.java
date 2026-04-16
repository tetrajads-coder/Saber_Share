package com.example.saber_share.util.api;

import com.example.saber_share.model.ConversacionDto;
import com.example.saber_share.model.MensajeCreateDto;
import com.example.saber_share.model.MensajeDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface MensajeApi {
    @GET("mensajes/conversacion")  Call<List<MensajeDto>> conversacion(@Query("user1") int user1, @Query("user2") int user2);
    @POST("mensajes")              Call<MensajeDto> enviar(@Body MensajeCreateDto dto);
    @GET("mensajes/inbox")         Call<List<ConversacionDto>> inbox(@Query("userId") int userId);
}
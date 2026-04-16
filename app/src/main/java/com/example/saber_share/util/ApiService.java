package com.example.saber_share.util;

import com.example.saber_share.model.ConversacionDto;
import com.example.saber_share.model.CursoDto;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.model.MensajeCreateDto;
import com.example.saber_share.model.MensajeDto;
import com.example.saber_share.model.OpinionServicioDto;
import com.example.saber_share.model.OpinionesCursoDto;
import com.example.saber_share.model.PaypalRequestDto;
import com.example.saber_share.model.PaypalResponseDto;
import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.model.UsuarioDto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ─── USUARIO ────────────────────────────────────────────────────────────
    @POST("usuarios/login")
    Call<UsuarioDto> login(@Body Map<String, String> credenciales);

    @POST("usuarios/registro")
    Call<UsuarioDto> registro(@Body UsuarioDto usuarioDto);

    @GET("usuarios/{id}")
    Call<UsuarioDto> getUsuario(@Path("id") int id);

    @PUT("usuarios/{id}")
    Call<UsuarioDto> actualizarUsuario(@Path("id") int id, @Body UsuarioDto usuarioDto);

    // ─── CURSOS ──────────────────────────────────────────────────────────────
    @GET("cursos")
    Call<List<CursoDto>> getCursos();

    @GET("cursos/{id}")
    Call<CursoDto> getCurso(@Path("id") int id);

    @POST("cursos")
    Call<CursoDto> crearCurso(@Body CursoDto cursoDto);

    @PUT("cursos/{id}")
    Call<CursoDto> actualizarCurso(@Path("id") int id, @Body CursoDto cursoDto);

    @DELETE("cursos/{id}")
    Call<Void> eliminarCurso(@Path("id") int id);

    // ─── SERVICIOS ───────────────────────────────────────────────────────────
    @GET("servicios")
    Call<List<ServicioDto>> getServicios();

    @GET("servicios/{id}")
    Call<ServicioDto> getServicio(@Path("id") int id);

    @POST("servicios")
    Call<ServicioDto> crearServicio(@Body ServicioDto servicioDto);

    @PUT("servicios/{id}")
    Call<ServicioDto> actualizarServicio(@Path("id") int id, @Body ServicioDto servicioDto);

    @DELETE("servicios/{id}")
    Call<Void> eliminarServicio(@Path("id") int id);

    // ─── OPINIONES CURSOS ────────────────────────────────────────────────────
    @POST("opiniones/cursos")
    Call<OpinionesCursoDto> crearOpinionCurso(@Body OpinionesCursoDto dto);

    @GET("opiniones/cursos/{cursoId}")
    Call<List<OpinionesCursoDto>> getOpinionesCurso(@Path("cursoId") int cursoId);

    @GET("opiniones/cursos/stats/{cursoId}")
    Call<Map<String, Object>> getStatsCurso(@Path("cursoId") int cursoId);

    @GET("opiniones/cursos/usuario/{usuarioId}")
    Call<List<OpinionesCursoDto>> getOpinionesCursoByUsuario(@Path("usuarioId") int usuarioId);

    @PUT("opiniones/cursos/{id}")
    Call<OpinionesCursoDto> actualizarOpinionCurso(@Path("id") int id, @Body OpinionesCursoDto dto);

    @DELETE("opiniones/cursos/{id}")
    Call<Void> eliminarOpinionCurso(@Path("id") int id);

    // ─── OPINIONES SERVICIOS ─────────────────────────────────────────────────
    @POST("opinion_servicio")
    Call<OpinionServicioDto> crearOpinionServicio(@Body OpinionServicioDto dto);

    @GET("opinion_servicio/servicio/{servicioId}")
    Call<List<OpinionServicioDto>> getOpinionesServicio(@Path("servicioId") int servicioId);

    @GET("opinion_servicio/stats/{servicioId}")
    Call<Map<String, Object>> getStatsServicio(@Path("servicioId") int servicioId);

    @GET("opinion_servicio/usuario/{usuarioId}")
    Call<List<OpinionServicioDto>> getOpinionesServicioByUsuario(@Path("usuarioId") int usuarioId);

    @PUT("opinion_servicio/{id}")
    Call<OpinionServicioDto> actualizarOpinionServicio(@Path("id") int id, @Body OpinionServicioDto dto);

    @DELETE("opinion_servicio/{id}")
    Call<Void> eliminarOpinionServicio(@Path("id") int id);

    // ─── MENSAJES ────────────────────────────────────────────────────────────
    @POST("mensajes")
    Call<MensajeDto> enviarMensaje(@Body MensajeCreateDto dto);

    @GET("mensajes/conversacion")
    Call<List<MensajeDto>> getConversacion(
            @Query("user1") int user1,
            @Query("user2") int user2
    );

    @GET("mensajes/inbox")
    Call<List<ConversacionDto>> getInbox(@Query("userId") int userId);

    @PUT("mensajes/leidos")
    Call<Void> marcarLeidos(
            @Query("receptorId") int receptorId,
            @Query("emisorId") int emisorId
    );

    // ─── HISTORIAL ───────────────────────────────────────────────────────────
    @GET("historial/usuario/{usuarioId}")
    Call<List<HistorialDto>> getHistorial(@Path("usuarioId") int usuarioId);

    // ─── PAYPAL ──────────────────────────────────────────────────────────────
    @POST("paypal/pagar")
    Call<PaypalResponseDto> iniciarPago(@Body PaypalRequestDto dto);

    @GET("paypal/confirmar")
    Call<PaypalResponseDto> confirmarPago(
            @Query("paymentId") String paymentId,
            @Query("PayerID") String payerId,
            @Query("usuarioId") int usuarioId,
            @Query("itemId") int itemId,
            @Query("tipo") String tipo
    );
}

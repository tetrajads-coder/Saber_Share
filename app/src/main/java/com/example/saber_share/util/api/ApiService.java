package com.example.saber_share.util.api;

import com.example.saber_share.model.PaypalRequestDto;
import com.example.saber_share.model.PaypalResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // ── PayPal: Paso 1 — crear orden ────────────────────────────────────────
    @POST("paypal/pagar")
    Call<PaypalResponseDto> iniciarPago(@Body PaypalRequestDto request);

    @GET("paypal/confirmar")
    Call<PaypalResponseDto> confirmarPago(
            @Query("paymentId") String paymentId,
            @Query("PayerID")   String payerId,
            @Query("usuarioId") int    usuarioId,
            @Query("itemId")    int    itemId,
            @Query("tipo")      String tipo
    );
}
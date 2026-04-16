package com.example.saber_share.util.api;

import android.content.Context;
import android.os.Build;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {


    // Para encontrarla: en Windows ejecuta "ipconfig" en cmd
    //                   en Mac/Linux ejecuta "ifconfig" en terminal
    // Busca la IP que empieza con 192.168.x.x
    private static final String IP_PC_LOCAL = "10.107.99.75"; // <-- CAMBIAR ESTO DEPENDIENDO DEL LUGAR
    private static final int    PUERTO      = 8080;

    private static final String URL_EMULADOR  = "http://10.0.2.2:" + PUERTO + "/api/";
    private static final String URL_FISICO    = "http://" + IP_PC_LOCAL + ":" + PUERTO + "/api/";

    private static Retrofit retrofit;

    private static String getBaseUrl() {
        // Detecta si corre en emulador de Android
        boolean esEmulador = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic");

        return esEmulador ? URL_EMULADOR : URL_FISICO;
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Para forzar reinicio si cambia de dispositivo
    public static void reset() {
        retrofit = null;
    }

    // ApiService para PayPal
    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}
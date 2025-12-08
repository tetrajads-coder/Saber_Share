package com.example.saber_share.util.repository;

import android.content.Context;

import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.ServicioApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class ServicioRepository {

    private final ServicioApi api;

    public ServicioRepository(Context context) {
        api = RetrofitClient.getClient().create(ServicioApi.class);
    }

    public void obtenerTodos(Callback<List<ServicioDto>> callback) {
        api.lista().enqueue(callback);
    }
}

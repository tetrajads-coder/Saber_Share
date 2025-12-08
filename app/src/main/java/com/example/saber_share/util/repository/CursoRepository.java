package com.example.saber_share.util.repository;

import android.content.Context;

import com.example.saber_share.model.CursoDto;
import com.example.saber_share.util.api.CursoApi;
import com.example.saber_share.util.api.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class CursoRepository {

    private final CursoApi api;

    public CursoRepository(Context context) {
        api = RetrofitClient.getClient().create(CursoApi.class);
    }

    public void obtenerTodos(Callback<List<CursoDto>> callback) {
        api.lista().enqueue(callback);
    }
}

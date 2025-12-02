package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.saber_share.R;
import com.example.saber_share.util.local.SessionManager;

public class Perfil extends Fragment implements View.OnClickListener {

    Button btncerrarsesion;

    SessionManager sessionManager;


    public Perfil() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btncerrarsesion = view.findViewById(R.id.btnCerrarSesion);
        btncerrarsesion.setOnClickListener(this);
        sessionManager = new SessionManager(view.getContext());


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_perfil, container, false);
    }


    @Override
    public void onClick(View view) {
        sessionManager.logoutUser();
    }
}
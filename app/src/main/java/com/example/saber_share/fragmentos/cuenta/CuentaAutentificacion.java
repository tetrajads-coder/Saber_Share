package com.example.saber_share.fragmentos.cuenta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.saber_share.R;
import com.example.saber_share.util.local.SessionManager;

public class CuentaAutentificacion extends Fragment {

    public CuentaAutentificacion() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuenta_autentificacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Si ya hay sesión activa → ir directo al main
        if (SessionManager.getInstance(requireContext()).isLoggedIn()) {
            Navigation.findNavController(view)
                    .navigate(R.id.action_inicioSesion_to_main);
            return;
        }

        view.findViewById(R.id.btnIniciarSesion).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_cuentaAutentificacion_to_inicioSesion));

        view.findViewById(R.id.btnRegistrar).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_cuentaAutentificacion_to_registroSesion));
    }
}
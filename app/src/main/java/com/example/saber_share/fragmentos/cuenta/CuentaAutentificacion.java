package com.example.saber_share.fragmentos.cuenta;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.saber_share.R;


public class CuentaAutentificacion extends Fragment implements View.OnClickListener {

    Button btnIniciarSesion;
    Button btnRegistrar;

    public CuentaAutentificacion() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion);
        btnRegistrar = view.findViewById(R.id.btnRegistrar);

        btnIniciarSesion.setOnClickListener(this);
        btnRegistrar.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cuenta_autentificacion, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onClick(View view) {
        String opcion = ((Button)view).getText().toString();

        if (opcion.equals("Iniciar sesión")){
            Navigation.findNavController(view).navigate(R.id.action_cuentaAutentificacion_to_inicioSesion);
        } else if (opcion.equals("Registrarse")){
            Navigation.findNavController(view).navigate(R.id.action_cuentaAutentificacion_to_registroSesion);
        }
    }
}
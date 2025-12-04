package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.saber_share.R;
import com.example.saber_share.model.MetodoDePagoDto;
import com.example.saber_share.util.api.MetodoPagoApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarTarjeta extends Fragment {

    private EditText etNumero, etFecha, etTitular, etCvv;
    private Button btnGuardar;
    private SessionManager sessionManager;

    public AgregarTarjeta() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_agregar_tarjeta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        etNumero = view.findViewById(R.id.etNumeroTarjeta);
        etFecha = view.findViewById(R.id.etFechaVencimiento); // Input tipo "MM/AA"
        etTitular = view.findViewById(R.id.etTitular);
        etCvv = view.findViewById(R.id.etCvv);
        btnGuardar = view.findViewById(R.id.btnGuardarTarjeta);

        btnGuardar.setOnClickListener(v -> guardarTarjeta());

        view.findViewById(R.id.btnCancelarTarjeta).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

    private void guardarTarjeta() {
        String numero = etNumero.getText().toString().trim();
        String fechaInput = etFecha.getText().toString().trim();
        String titular = etTitular.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();

        if (TextUtils.isEmpty(numero) || numero.length() < 16) {
            etNumero.setError("Número inválido (16 dígitos)"); return;
        }
        if (TextUtils.isEmpty(titular)) {
            etTitular.setError("Requerido"); return;
        }

        // Conversión de fecha MM/AA -> YYYY-MM-01
        String fechaParaBd = "";
        try {
            if (!fechaInput.contains("/")) throw new Exception();
            String[] partes = fechaInput.split("/");
            String mes = partes[0];
            String anio = "20" + partes[1]; // Asumimos siglo 21 (ej. 25 -> 2025)
            // Validar mes básico
            int mesInt = Integer.parseInt(mes);
            if(mesInt < 1 || mesInt > 12) throw new Exception();

            fechaParaBd = anio + "-" + mes + "-01";
        } catch (Exception e) {
            etFecha.setError("Formato inválido (Use MM/AA)"); return;
        }

        // Lógica simple para detectar Visa/Mastercard
        String compania = numero.startsWith("4") ? "VISA" : "MasterCard";

        MetodoDePagoDto nuevaTarjeta = new MetodoDePagoDto();
        nuevaTarjeta.setCompania(compania);
        nuevaTarjeta.setNumeroTarjeta(numero);
        nuevaTarjeta.setCvv(cvv);
        nuevaTarjeta.setVencimiento(fechaParaBd);
        nuevaTarjeta.setTitular(titular);
        nuevaTarjeta.setUsuarioId(sessionManager.getUserId());

        enviarAlBackend(nuevaTarjeta);
    }

    private void enviarAlBackend(MetodoDePagoDto tarjeta) {
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        MetodoPagoApi api = RetrofitClient.getClient().create(MetodoPagoApi.class);
        api.crearTarjeta(tarjeta).enqueue(new Callback<MetodoDePagoDto>() {
            @Override
            public void onResponse(Call<MetodoDePagoDto> call, Response<MetodoDePagoDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Tarjeta agregada exitosamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                }
            }

            @Override
            public void onFailure(Call<MetodoDePagoDto> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                btnGuardar.setEnabled(true);
                btnGuardar.setText("Guardar");
            }
        });
    }
}
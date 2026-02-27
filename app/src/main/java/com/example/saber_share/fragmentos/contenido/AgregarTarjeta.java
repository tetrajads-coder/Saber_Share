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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_agregar_tarjeta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        etNumero  = view.findViewById(R.id.etNumeroTarjeta);
        etFecha   = view.findViewById(R.id.etFechaVencimiento); // MM/AA
        etTitular = view.findViewById(R.id.etTitular);
        etCvv     = view.findViewById(R.id.etCvv);
        btnGuardar = view.findViewById(R.id.btnGuardarTarjeta);

        btnGuardar.setOnClickListener(v -> guardarTarjeta());

        view.findViewById(R.id.btnCancelarTarjeta).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

    private void guardarTarjeta() {
        // ✅ Validar sesion
        int miId = sessionManager.getUserId();
        if (miId == -1) {
            Toast.makeText(getContext(), "Sesion invalida. Inicia sesion otra vez.", Toast.LENGTH_SHORT).show();
            sessionManager.logoutUser();
            return;
        }

        String numero = etNumero.getText().toString().trim().replace(" ", "");
        String fechaInput = etFecha.getText().toString().trim();
        String titular = etTitular.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();

        if (TextUtils.isEmpty(numero) || numero.length() != 16 || !numero.matches("\\d+")) {
            etNumero.setError("Numero invalido (16 digitos)");
            return;
        }

        if (TextUtils.isEmpty(titular)) {
            etTitular.setError("Requerido");
            return;
        }

        if (TextUtils.isEmpty(cvv) || !(cvv.length() == 3 || cvv.length() == 4) || !cvv.matches("\\d+")) {
            etCvv.setError("CVV invalido (3 o 4 digitos)");
            return;
        }

        // ✅ MM/AA -> YYYY-MM-01
        String fechaParaBd;
        try {
            if (!fechaInput.contains("/")) throw new Exception("sin /");

            String[] partes = fechaInput.split("/");
            if (partes.length != 2) throw new Exception("partes");

            String mesStr = partes[0].trim();
            String anio2 = partes[1].trim();

            int mesInt = Integer.parseInt(mesStr);
            if (mesInt < 1 || mesInt > 12) throw new Exception("mes");

            // forzar 2 digitos
            String mes = (mesInt < 10) ? ("0" + mesInt) : String.valueOf(mesInt);

            if (anio2.length() != 2) throw new Exception("anio");
            String anio = "20" + anio2;

            fechaParaBd = anio + "-" + mes + "-01";
        } catch (Exception e) {
            etFecha.setError("Formato invalido (MM/AA)");
            return;
        }

        // ✅ detectar compania
        String compania;
        if (numero.startsWith("4")) compania = "VISA";
        else if (numero.startsWith("5")) compania = "MASTERCARD";
        else compania = "DESCONOCIDA";

        MetodoDePagoDto nuevaTarjeta = new MetodoDePagoDto();
        nuevaTarjeta.setCompania(compania);
        nuevaTarjeta.setNumeroTarjeta(numero);
        nuevaTarjeta.setCvv(cvv);
        nuevaTarjeta.setVencimiento(fechaParaBd);
        nuevaTarjeta.setTitular(titular);

        // ✅ CLAVE: FK depende de esto
        nuevaTarjeta.setUsuarioId(miId);

        // Debug rapido (quitale despues)
        Toast.makeText(getContext(), "Guardando tarjeta para usuarioId=" + miId, Toast.LENGTH_SHORT).show();

        enviarAlBackend(nuevaTarjeta, miId);
    }

    private void enviarAlBackend(MetodoDePagoDto tarjeta, int miId) {
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        MetodoPagoApi api = RetrofitClient.getClient().create(MetodoPagoApi.class);
        api.crearTarjeta(tarjeta).enqueue(new Callback<MetodoDePagoDto>() {
            @Override
            public void onResponse(@NonNull Call<MetodoDePagoDto> call, @NonNull Response<MetodoDePagoDto> response) {
                btnGuardar.setEnabled(true);
                btnGuardar.setText("Guardar");

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Tarjeta agregada", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(getContext(),
                            "Error " + response.code() + " (usuarioId=" + miId + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MetodoDePagoDto> call, @NonNull Throwable t) {
                btnGuardar.setEnabled(true);
                btnGuardar.setText("Guardar");
                Toast.makeText(getContext(), "Fallo de conexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

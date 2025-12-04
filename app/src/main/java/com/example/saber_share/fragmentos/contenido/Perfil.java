package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Perfil extends Fragment {

    private SessionManager sessionManager;
    private TextView tvNombre, tvCorreo, tvCountCursos, tvCountClases, tvMetodoPagoStatus;
    private EditText etNombrePub, etCorreoPub;
    private Button btnGestionarTarjetas, btnHistorial, btnEstadisticas, btnCerrarSesion;

    public Perfil() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        // 1. Vincular Vistas
        tvNombre = view.findViewById(R.id.tvNombrePerfil);
        tvCorreo = view.findViewById(R.id.tvCorreoPerfil);
        tvCountCursos = view.findViewById(R.id.tvCountCursos);
        tvCountClases = view.findViewById(R.id.tvCountClases);
        tvMetodoPagoStatus = view.findViewById(R.id.tvMetodoPagoStatus);

        etNombrePub = view.findViewById(R.id.etNombrePublico);
        etCorreoPub = view.findViewById(R.id.etCorreoPublico);

        btnGestionarTarjetas = view.findViewById(R.id.btnGestionarTarjetas);
        btnHistorial = view.findViewById(R.id.btnHistorial);
        btnEstadisticas = view.findViewById(R.id.btnEstadisticas);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        // 2. Llenar datos del usuario (Simulado o desde SessionManager)
        // Nota: Idealmente tu SessionManager debería tener getNombre(), getCorreo()
        // Por ahora usamos placeholders o el ID
        int userId = sessionManager.getUserId();
        String usuarioEjemplo = "Usuario " + userId;
        String correoEjemplo = "usuario" + userId + "@sabershare.com";

        tvNombre.setText(usuarioEjemplo);
        tvCorreo.setText(correoEjemplo);
        etNombrePub.setText(usuarioEjemplo);
        etCorreoPub.setText(correoEjemplo);

        // 3. Configurar Botones
        btnCerrarSesion.setOnClickListener(v -> sessionManager.logoutUser());

        // Navegación a Tarjetas (LO IMPORTANTE AHORA)
        btnGestionarTarjetas.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_administrarTarjetas)
        );

        // Navegación a Historial / Estadísticas (Pendientes para futuros módulos)
        btnHistorial.setOnClickListener(v ->
                Toast.makeText(getContext(), "Módulo Historial pendiente", Toast.LENGTH_SHORT).show()
        );

        btnEstadisticas.setOnClickListener(v ->
                Toast.makeText(getContext(), "Módulo Estadísticas pendiente", Toast.LENGTH_SHORT).show()
        );

        // 4. Cargar resumen de tarjetas (Opcional pero recomendado para UX)
        verificarTarjetas(userId);
    }

    private void verificarTarjetas(int userId) {
        MetodoPagoApi api = RetrofitClient.getClient().create(MetodoPagoApi.class);
        api.listarTarjetas().enqueue(new Callback<List<MetodoDePagoDto>>() {
            @Override
            public void onResponse(Call<List<MetodoDePagoDto>> call, Response<List<MetodoDePagoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lógica simple: Buscar si hay alguna tarjeta de este usuario
                    boolean tieneTarjeta = false;
                    String ultimosDigitos = "";

                    for (MetodoDePagoDto m : response.body()) {
                        if (m.getUsuarioId() != null && m.getUsuarioId() == userId) {
                            tieneTarjeta = true;
                            String num = m.getNumeroTarjeta();
                            ultimosDigitos = (num != null && num.length() > 4) ? num.substring(num.length()-4) : "xxxx";
                            break; // Tomamos la primera que encontremos
                        }
                    }

                    if (tieneTarjeta) {
                        tvMetodoPagoStatus.setText("Tarjeta terminada en " + ultimosDigitos);
                        tvMetodoPagoStatus.setTextColor(getResources().getColor(R.color.black)); // O color primario
                    } else {
                        tvMetodoPagoStatus.setText("(aun no agregado)");
                    }
                }
            }
            @Override
            public void onFailure(Call<List<MetodoDePagoDto>> call, Throwable t) {
                // Silencioso, no molestamos al usuario en el perfil si falla esto
            }
        });
    }
}
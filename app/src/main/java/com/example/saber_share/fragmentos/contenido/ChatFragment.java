package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.contenido.adapter.MensajeChatAdapter;
import com.example.saber_share.model.MensajeCreateDto;
import com.example.saber_share.model.MensajeDto;
import com.example.saber_share.util.api.MensajeApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView rvMensajes;
    private TextView tvVacio, tvTitulo;
    private TextInputEditText etMensaje;
    private ImageButton btnEnviar;

    private MensajeChatAdapter adapter;
    private SessionManager sessionManager;

    private int emisorId = -1;
    private int receptorId = -1;
    private String receptorNombre = "";

    public ChatFragment() {
        super(R.layout.fragment_main_mensajes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        emisorId = sessionManager.getUserId();

        rvMensajes = view.findViewById(R.id.rvMensajes);
        tvVacio = view.findViewById(R.id.tvVacio);
        tvTitulo = view.findViewById(R.id.tvTituloChat);
        etMensaje = view.findViewById(R.id.etMensaje);
        btnEnviar = view.findViewById(R.id.btnEnviar);

        // 1) Intentar args
        Bundle args = getArguments();
        if (args != null) {
            // Soporta ambos nombres por si en otro lado mandas "nombreReceptor"
            receptorId = args.getInt("receptorId", -1);
            receptorNombre = args.getString("receptorNombre", "");
            if (TextUtils.isEmpty(receptorNombre)) {
                receptorNombre = args.getString("nombreReceptor", "");
            }
        }

        // 2) Si no hay args, usar ultimo chat guardado
        if (receptorId <= 0) {
            receptorId = sessionManager.getLastChatId();
            receptorNombre = sessionManager.getLastChatName();
        } else {
            // Si si venia con args, guardalo como ultimo chat
            sessionManager.setLastChat(receptorId, receptorNombre);
        }

        tvTitulo.setText(!TextUtils.isEmpty(receptorNombre) ? receptorNombre : "Chat");

        rvMensajes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MensajeChatAdapter(emisorId);
        rvMensajes.setAdapter(adapter);

        if (emisorId <= 0) {
            Toast.makeText(getContext(), "Sesion invalida. Inicia sesion.", Toast.LENGTH_SHORT).show();
            btnEnviar.setEnabled(false);
            return;
        }

        if (receptorId <= 0) {
            Toast.makeText(getContext(), "Abre un detalle y presiona Contactar para iniciar chat.", Toast.LENGTH_SHORT).show();
            btnEnviar.setEnabled(false);
            tvVacio.setVisibility(View.VISIBLE);
            rvMensajes.setVisibility(View.GONE);
            return;
        }

        btnEnviar.setEnabled(true);
        btnEnviar.setOnClickListener(v -> enviarMensaje());

        cargarConversacion();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (emisorId > 0 && receptorId > 0) cargarConversacion();
    }

    private void cargarConversacion() {
        MensajeApi api = RetrofitClient.getClient().create(MensajeApi.class);
        api.conversacion(emisorId, receptorId).enqueue(new Callback<List<MensajeDto>>() {
            @Override
            public void onResponse(Call<List<MensajeDto>> call, Response<List<MensajeDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MensajeDto> lista = response.body();

                    // ✅ ORDENAR POR ID (viejo -> nuevo)
                    java.util.Collections.sort(lista, (a, b) -> Integer.compare(a.getIdMensaje(), b.getIdMensaje()));

                    if (lista.isEmpty()) {
                        tvVacio.setVisibility(View.VISIBLE);
                        rvMensajes.setVisibility(View.GONE);
                    } else {
                        tvVacio.setVisibility(View.GONE);
                        rvMensajes.setVisibility(View.VISIBLE);
                    }

                    adapter.setDatos(lista);

                    // ✅ BAJAR AL ULTIMO
                    if (!lista.isEmpty()) rvMensajes.scrollToPosition(adapter.getItemCount() - 1);

                } else {
                    Toast.makeText(getContext(), "Error cargar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MensajeDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red al cargar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText() != null ? etMensaje.getText().toString().trim() : "";
        if (TextUtils.isEmpty(texto)) return;

        btnEnviar.setEnabled(false);

        MensajeCreateDto dto = new MensajeCreateDto(emisorId, receptorId, texto);

        MensajeApi api = RetrofitClient.getClient().create(MensajeApi.class);
        api.enviar(dto).enqueue(new Callback<MensajeDto>() {
            @Override
            public void onResponse(Call<MensajeDto> call, Response<MensajeDto> response) {
                btnEnviar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    etMensaje.setText("");

                    tvVacio.setVisibility(View.GONE);
                    rvMensajes.setVisibility(View.VISIBLE);

                    adapter.addMensaje(response.body());
                    rvMensajes.scrollToPosition(adapter.getItemCount() - 1);
                } else {
                    Toast.makeText(getContext(), "No envio: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MensajeDto> call, Throwable t) {
                btnEnviar.setEnabled(true);
                Toast.makeText(getContext(), "Error de red al enviar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

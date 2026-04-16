package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView          rvMensajes;
    private TextView              tvNombre, tvAvatarInicial;
    private TextInputEditText     etMensaje;
    private FloatingActionButton  fabEnviar;

    private MensajeChatAdapter adapter;
    private SessionManager     sessionManager;

    private int    emisorId       = -1;
    private int    receptorId     = -1;
    private String receptorNombre = "";

    public ChatFragment() {
        super(R.layout.fragment_chat);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        emisorId       = sessionManager.getUsuarioId();

        rvMensajes      = view.findViewById(R.id.rv_chat_mensajes);
        tvNombre        = view.findViewById(R.id.tv_chat_nombre);
        tvAvatarInicial = view.findViewById(R.id.tv_chat_avatar_inicial);
        etMensaje       = view.findViewById(R.id.et_mensaje);
        fabEnviar       = view.findViewById(R.id.fab_enviar);

        // Botón atrás
        view.findViewById(R.id.btn_chat_back).setOnClickListener(
                v -> requireActivity().onBackPressed());

        // Args
        Bundle args = getArguments();
        if (args != null) {
            receptorId     = args.getInt("receptorId", -1);
            receptorNombre = args.getString("receptorNombre", "");
            if (TextUtils.isEmpty(receptorNombre))
                receptorNombre = args.getString("nombreReceptor", "");
        }



        tvNombre.setText(!TextUtils.isEmpty(receptorNombre) ? receptorNombre : "Chat");
        tvAvatarInicial.setText((!TextUtils.isEmpty(receptorNombre))
                ? String.valueOf(receptorNombre.charAt(0)).toUpperCase() : "U");

        rvMensajes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MensajeChatAdapter(emisorId);
        rvMensajes.setAdapter(adapter);

        if (emisorId <= 0) {
            Toast.makeText(getContext(), "Sesión inválida. Inicia sesión.", Toast.LENGTH_SHORT).show();
            fabEnviar.setEnabled(false);
            return;
        }

        if (receptorId <= 0) {
            Toast.makeText(getContext(), "Abre un detalle y presiona Contactar para iniciar chat.", Toast.LENGTH_SHORT).show();
            fabEnviar.setEnabled(false);
            return;
        }

        fabEnviar.setOnClickListener(v -> enviarMensaje());
        cargarConversacion();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (emisorId > 0 && receptorId > 0) cargarConversacion();
    }

    private void cargarConversacion() {
        MensajeApi api = RetrofitClient.getInstance().create(MensajeApi.class);
        api.conversacion(emisorId, receptorId).enqueue(new Callback<List<MensajeDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<MensajeDto>> call,
                                   @NonNull Response<List<MensajeDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MensajeDto> lista = response.body();
                    Collections.sort(lista, (a, b) ->
                            Integer.compare(a.getIdMensaje(), b.getIdMensaje()));
                    adapter.setDatos(lista);
                    if (!lista.isEmpty())
                        rvMensajes.scrollToPosition(adapter.getItemCount() - 1);
                } else {
                    Toast.makeText(getContext(), "Error al cargar: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MensajeDto>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red al cargar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText() != null
                ? etMensaje.getText().toString().trim() : "";
        if (TextUtils.isEmpty(texto)) return;

        fabEnviar.setEnabled(false);
        MensajeApi api = RetrofitClient.getInstance().create(MensajeApi.class);
        api.enviar(new MensajeCreateDto(emisorId, receptorId, texto))
                .enqueue(new Callback<MensajeDto>() {
                    @Override
                    public void onResponse(@NonNull Call<MensajeDto> call,
                                           @NonNull Response<MensajeDto> response) {
                        fabEnviar.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            etMensaje.setText("");
                            adapter.addMensaje(response.body());
                            rvMensajes.scrollToPosition(adapter.getItemCount() - 1);
                        } else {
                            Toast.makeText(getContext(), "No se envió: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MensajeDto> call, @NonNull Throwable t) {
                        fabEnviar.setEnabled(true);
                        Toast.makeText(getContext(), "Error de red al enviar", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
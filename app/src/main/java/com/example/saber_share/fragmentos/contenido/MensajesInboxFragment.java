package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.contenido.adapter.InboxAdapter;
import com.example.saber_share.model.ConversacionDto;
import com.example.saber_share.util.api.MensajeApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MensajesInboxFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvVacio;
    private InboxAdapter adapter;

    private SessionManager sessionManager;
    private int myId;

    // para evitar spamear llamadas
    private boolean cargando = false;

    public MensajesInboxFragment() {
        super(R.layout.fragment_inbox);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        myId = sessionManager.getUserId();

        rv = view.findViewById(R.id.rvInbox);
        tvVacio = view.findViewById(R.id.tvVacio);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new InboxAdapter(c -> abrirChat(view, c.otroId, c.otroNombre));
        rv.setAdapter(adapter);

        if (myId <= 0) {
            Toast.makeText(getContext(), "Sesion invalida", Toast.LENGTH_SHORT).show();
            return;
        }

        cargarInbox(); // SOLO aqui
    }

    // ❌ QUITAR onResume() porque te genera spam de requests
    // @Override
    // public void onResume() {
    //     super.onResume();
    //     if (myId > 0) cargarInbox();
    // }

    private void cargarInbox() {
        if (cargando) return;   // evita doble llamada
        cargando = true;

        MensajeApi api = RetrofitClient.getClient().create(MensajeApi.class);
        api.inbox(myId).enqueue(new Callback<List<ConversacionDto>>() {
            @Override
            public void onResponse(Call<List<ConversacionDto>> call, Response<List<ConversacionDto>> response) {
                cargando = false;

                // si el fragment ya no esta pegado, no toques UI
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<ConversacionDto> lista = response.body();

                    adapter.setData(lista);

                    if (lista.isEmpty()) {
                        tvVacio.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    } else {
                        tvVacio.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                    }

                } else {
                    Toast.makeText(getContext(), "Inbox error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ConversacionDto>> call, Throwable t) {
                cargando = false;
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error red inbox", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirChat(View view, int receptorId, String receptorNombre) {

        // guardar ultimo chat (opcional pero util)
        sessionManager.setLastChat(receptorId, receptorNombre != null ? receptorNombre : "");

        Bundle b = new Bundle();
        b.putInt("receptorId", receptorId);
        b.putString("receptorNombre", receptorNombre != null ? receptorNombre : "");


        Navigation.findNavController(view).navigate(R.id.action_inbox_to_chat, b);
    }
}

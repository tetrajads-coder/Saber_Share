package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    private RecyclerView       rv;
    private View               layoutEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private InboxAdapter       adapter;

    private int     myId     = -1;
    private boolean cargando = false;

    public MensajesInboxFragment() {
        super(R.layout.fragment_main_mensajes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myId = SessionManager.getInstance(requireContext()).getUsuarioId();

        rv           = view.findViewById(R.id.rv_inbox);
        layoutEmpty  = view.findViewById(R.id.layout_empty_msgs);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_msgs);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InboxAdapter(c -> abrirChat(view, c.otroId, c.otroNombre));
        rv.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.accent_primary);
        swipeRefresh.setOnRefreshListener(this::cargarInbox);

        if (myId <= 0) {
            Toast.makeText(getContext(), "Sesión inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        cargarInbox();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (myId > 0) cargarInbox();
    }

    private void cargarInbox() {
        if (cargando) return;
        cargando = true;
        swipeRefresh.setRefreshing(true);

        RetrofitClient.getInstance().create(MensajeApi.class)
                .inbox(myId).enqueue(new Callback<List<ConversacionDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ConversacionDto>> call,
                                           @NonNull Response<List<ConversacionDto>> response) {
                        cargando = false;
                        swipeRefresh.setRefreshing(false);
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            List<ConversacionDto> lista = response.body();
                            adapter.setData(lista);
                            boolean vacio = lista.isEmpty();
                            layoutEmpty.setVisibility(vacio ? View.VISIBLE : View.GONE);
                            rv.setVisibility(vacio ? View.GONE : View.VISIBLE);
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al cargar mensajes: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ConversacionDto>> call,
                                          @NonNull Throwable t) {
                        cargando = false;
                        swipeRefresh.setRefreshing(false);
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirChat(View view, int receptorId, String receptorNombre) {
        Bundle b = new Bundle();
        b.putInt("receptorId",       receptorId);
        b.putString("receptorNombre", receptorNombre != null ? receptorNombre : "");
        Navigation.findNavController(view).navigate(R.id.chatFragment, b);
    }
}
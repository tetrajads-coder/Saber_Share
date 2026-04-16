package com.example.saber_share.fragmentos.contenido.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.model.MensajeDto;

import java.util.ArrayList;
import java.util.List;

public class MensajeChatAdapter extends RecyclerView.Adapter<MensajeChatAdapter.VH> {

    private final List<MensajeDto> data = new ArrayList<>();
    private final int myUserId;

    public MensajeChatAdapter(int myUserId) {
        this.myUserId = myUserId;
    }

    public void setDatos(List<MensajeDto> nuevos) {
        data.clear();
        if (nuevos != null) data.addAll(nuevos);
        notifyDataSetChanged();
    }

    public void addMensaje(MensajeDto m) {
        if (m == null) return;
        data.add(m);
        notifyItemInserted(data.size() - 1);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mensaje, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MensajeDto m = data.get(position);

        String contenido = m.getContenido() != null ? m.getContenido() : "";
        String hora      = m.getFechaEnvio() != null ? m.getFechaEnvio() : "";

        boolean esMio = m.getEmisorId() == myUserId;

        if (esMio) {
            h.layoutEnviado.setVisibility(View.VISIBLE);
            h.layoutRecibido.setVisibility(View.GONE);
            h.tvEnviadoTexto.setText(contenido);
            h.tvEnviadoHora.setText(hora);
        } else {
            h.layoutRecibido.setVisibility(View.VISIBLE);
            h.layoutEnviado.setVisibility(View.GONE);
            h.tvRecibidoTexto.setText(contenido);
            h.tvRecibidoHora.setText(hora);
        }
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View layoutEnviado, layoutRecibido;
        TextView tvEnviadoTexto, tvEnviadoHora;
        TextView tvRecibidoTexto, tvRecibidoHora;

        VH(@NonNull View itemView) {
            super(itemView);
            layoutEnviado   = itemView.findViewById(R.id.layout_msg_enviado);
            layoutRecibido  = itemView.findViewById(R.id.layout_msg_recibido);
            tvEnviadoTexto  = itemView.findViewById(R.id.tv_msg_enviado_texto);
            tvEnviadoHora   = itemView.findViewById(R.id.tv_msg_enviado_hora);
            tvRecibidoTexto = itemView.findViewById(R.id.tv_msg_recibido_texto);
            tvRecibidoHora  = itemView.findViewById(R.id.tv_msg_recibido_hora);
        }
    }
}
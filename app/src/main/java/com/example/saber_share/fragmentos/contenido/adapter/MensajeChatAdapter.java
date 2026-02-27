package com.example.saber_share.fragmentos.contenido.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensaje, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MensajeDto m = data.get(position);

        String contenido = (m.getContenido() != null) ? m.getContenido() : "";
        String fecha = (m.getFechaEnvio() != null) ? m.getFechaEnvio() : "";

        h.tvContenido.setText(contenido);
        h.tvFecha.setText(fecha);

        Integer emisorId = m.getEmisorId();
        boolean esMio = (emisorId != null && emisorId == myUserId);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) h.bubble.getLayoutParams();
        params.gravity = esMio ? Gravity.END : Gravity.START;
        h.bubble.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout bubble;
        TextView tvContenido, tvFecha;

        VH(@NonNull View itemView) {
            super(itemView);
            bubble = itemView.findViewById(R.id.bubble);
            tvContenido = itemView.findViewById(R.id.tvContenido);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}

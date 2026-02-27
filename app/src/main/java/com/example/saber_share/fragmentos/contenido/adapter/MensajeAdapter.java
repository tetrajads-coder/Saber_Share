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

public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.VH> {

    private final List<MensajeDto> data = new ArrayList<>();

    public void setData(List<MensajeDto> nuevos) {
        data.clear();
        if (nuevos != null) data.addAll(nuevos);
        notifyDataSetChanged();
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
        h.tvContenido.setText(m.getContenido());
        h.tvFecha.setText(m.getFechaEnvio());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvContenido, tvFecha;

        VH(@NonNull View itemView) {
            super(itemView);
            tvContenido = itemView.findViewById(R.id.tvContenido);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}
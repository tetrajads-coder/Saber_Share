package com.example.saber_share.fragmentos.contenido.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.model.AgendaDto;

import java.util.List;

public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.ViewHolder> {

    private final List<AgendaDto> slots;
    private final OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onReservarClick(AgendaDto slot);
    }

    public AgendaAdapter(List<AgendaDto> slots, OnSlotClickListener listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AgendaDto slot = slots.get(position);

        holder.tvFecha.setText("Fecha: " + (slot.getFecha() != null ? slot.getFecha() : ""));
        holder.tvHora.setText("Hora: " + (slot.getHora() != null ? slot.getHora() : ""));

        holder.btnReservar.setOnClickListener(v -> {
            if (listener != null) listener.onReservarClick(slot);
        });
    }

    @Override
    public int getItemCount() {
        return slots == null ? 0 : slots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvHora;
        Button btnReservar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFechaSlot);
            tvHora = itemView.findViewById(R.id.tvHoraSlot);
            btnReservar = itemView.findViewById(R.id.btnReservarSlot);
        }
    }
}

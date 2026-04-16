package com.example.saber_share.fragmentos.contenido.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.model.AgendaDto;

import java.util.List;

public class AgendaProfeAdapter extends RecyclerView.Adapter<AgendaProfeAdapter.ViewHolder> {

    private final List<AgendaDto> slots;
    private final OnSlotActionListener listener;

    public interface OnSlotActionListener {
        void onEliminarClick(int idAgenda);
        void onVerDetalleClick(AgendaDto slot);
    }

    public AgendaProfeAdapter(List<AgendaDto> slots, OnSlotActionListener listener) {
        this.slots    = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_horario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AgendaDto slot = slots.get(position);

        holder.tvFecha.setText("📅 " + (slot.getFecha() != null ? slot.getFecha() : ""));
        holder.tvHora.setText("🕐 "  + (slot.getHora()  != null ? slot.getHora()  : ""));

        if ("RESERVADA".equalsIgnoreCase(slot.getEstado())) {
            holder.btnAccion.setText("Ver alumno");
            holder.btnAccion.setBackgroundResource(R.drawable.bg_tipo_selected);
            holder.btnAccion.setOnClickListener(v -> {
                if (listener != null) listener.onVerDetalleClick(slot);
            });
        } else {
            holder.btnAccion.setText("Eliminar");
            holder.btnAccion.setBackgroundResource(0);
            holder.btnAccion.setBackgroundColor(0xFFD32F2F);
            holder.btnAccion.setOnClickListener(v -> {
                if (listener != null) listener.onEliminarClick(slot.getIdAgenda());
            });
        }
    }

    @Override
    public int getItemCount() { return slots == null ? 0 : slots.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvHora;
        Button   btnAccion;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha   = itemView.findViewById(R.id.tv_slot_fecha);
            tvHora    = itemView.findViewById(R.id.tv_slot_hora);
            btnAccion = itemView.findViewById(R.id.btn_slot_reservar);
        }
    }
}
package com.example.saber_share.fragmentos.contenido.adapter;

import android.graphics.Color;
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

    private List<AgendaDto> slots;
    private OnSlotActionListener listener;

    public interface OnSlotActionListener {
        void onEliminarClick(int idAgenda);
        void onVerDetalleClick(AgendaDto slot); // <--- NUEVO MÉTODO
    }

    public AgendaProfeAdapter(List<AgendaDto> slots, OnSlotActionListener listener) {
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

        holder.tvFecha.setText(slot.getFecha());
        holder.tvHora.setText(slot.getHora());

        if ("RESERVADA".equalsIgnoreCase(slot.getEstado())) {
            holder.btnAccion.setText("Ver Alumno"); // Cambiamos texto
            holder.btnAccion.setEnabled(true);      // Ahora SI está habilitado
            holder.btnAccion.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde

            holder.btnAccion.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                // Pasa el ID y Nombre del ALUMNO (que es quien reservó)
                bundle.putInt("otroId", slot.getAlumnoId());
                bundle.putString("otroNombre", slot.getNombreAlumno());

                // IMPORTANTE: Navegar a 'chatDirecto' (definido en main_nav), NO a 'mensajes' (bandeja)
                try {
                    Navigation.findNavController(v).navigate(R.id.chatFragment, bundle);
                } catch (Exception e) {
                    // Si 'chatDirecto' no existe, revisa tu main_nav.xml
                    // Alternativa: R.id.action_global_chatDirecto o similar
                    Toast.makeText(v.getContext(), "Error navegando al chat", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.btnAccion.setText("Eliminar");
            holder.btnAccion.setEnabled(true);
            holder.btnAccion.setBackgroundColor(Color.parseColor("#D32F2F")); // Rojo

            // Acción: Eliminar
            holder.btnAccion.setOnClickListener(v -> listener.onEliminarClick(slot.getIdAgenda()));
        }
    }

    @Override
    public int getItemCount() { return slots.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvHora;
        Button btnAccion;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFechaSlot);
            tvHora = itemView.findViewById(R.id.tvHoraSlot);
            btnAccion = itemView.findViewById(R.id.btnReservarSlot);
        }
    }
}
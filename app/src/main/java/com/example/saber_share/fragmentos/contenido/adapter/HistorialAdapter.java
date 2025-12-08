package com.example.saber_share.fragmentos.contenido.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.model.HistorialDto;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private final List<HistorialDto> datos;

    public HistorialAdapter(List<HistorialDto> datos) {
        this.datos = datos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorialDto h = datos.get(position);

        String tipo;
        if (h.getCursoId() != null) {
            tipo = "Curso #" + h.getCursoId();
        } else if (h.getServicioId() != null) {
            tipo = "Clase 1 a 1 #" + h.getServicioId();
        } else {
            tipo = "Movimiento";
        }

        holder.tvTipoTitulo.setText(tipo);
        holder.tvFecha.setText("Fecha: " + (h.getFechapago() != null ? h.getFechapago() : ""));
        holder.tvMonto.setText("$ " + (h.getPago() != null ? h.getPago() : 0.0));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipoTitulo, tvFecha, tvMonto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipoTitulo = itemView.findViewById(R.id.tvTipoTitulo);
            tvFecha      = itemView.findViewById(R.id.tvFecha);
            tvMonto      = itemView.findViewById(R.id.tvMonto);
        }
    }
}

package com.example.saber_share.fragmentos.contenido.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.model.MetodoDePagoDto;

import java.util.ArrayList;
import java.util.List;

public class MetodoPagoAdapter extends RecyclerView.Adapter<MetodoPagoAdapter.TarjetaViewHolder> {

    private List<MetodoDePagoDto> lista;
    private OnTarjetaActionListener listener;

    public interface OnTarjetaActionListener {
        void onEliminarClick(int idTarjeta);
        // Podrías añadir void onEditarClick(...) aquí
    }

    public MetodoPagoAdapter(List<MetodoDePagoDto> lista, OnTarjetaActionListener listener) {
        this.lista = lista != null ? lista : new ArrayList<>();
        this.listener = listener;
    }

    public void setDatos(List<MetodoDePagoDto> nuevosDatos) {
        this.lista = nuevosDatos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TarjetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarjeta, parent, false);
        return new TarjetaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TarjetaViewHolder holder, int position) {
        MetodoDePagoDto tarjeta = lista.get(position);

        holder.tvBanco.setText(tarjeta.getCompania() != null ? tarjeta.getCompania() : "Tarjeta");
        holder.tvTitular.setText(tarjeta.getTitular());

        // Formatear vencimiento (llega como YYYY-MM-DD, mostramos MM/YY)
        String fecha = tarjeta.getVencimiento();
        if (fecha != null && fecha.length() >= 7) {
            // Ejemplo 2025-12-01 -> 12/25
            String anio = fecha.substring(2, 4);
            String mes = fecha.substring(5, 7);
            holder.tvVencimiento.setText(mes + "/" + anio);
        } else {
            holder.tvVencimiento.setText("N/A");
        }

        // Enmascarar tarjeta
        String num = tarjeta.getNumeroTarjeta();
        if (num != null && num.length() > 4) {
            String ultimos = num.substring(num.length() - 4);
            holder.tvNumero.setText("**** **** **** " + ultimos);
        } else {
            holder.tvNumero.setText("****");
        }

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) listener.onEliminarClick(tarjeta.getId());
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class TarjetaViewHolder extends RecyclerView.ViewHolder {
        TextView tvBanco, tvNumero, tvTitular, tvVencimiento;
        ImageButton btnEliminar;

        public TarjetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBanco = itemView.findViewById(R.id.tvBanco);
            tvNumero = itemView.findViewById(R.id.tvNumeroOculto);
            tvTitular = itemView.findViewById(R.id.tvTitular);
            tvVencimiento = itemView.findViewById(R.id.tvVencimiento);
            btnEliminar = itemView.findViewById(R.id.btnEliminarTarjeta);
        }
    }
}
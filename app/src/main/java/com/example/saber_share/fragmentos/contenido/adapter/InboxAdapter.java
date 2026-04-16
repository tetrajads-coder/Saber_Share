package com.example.saber_share.fragmentos.contenido.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.model.ConversacionDto;

import java.util.ArrayList;
import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.VH> {

    public interface OnClick {
        void onClick(ConversacionDto c);
    }

    private final List<ConversacionDto> data = new ArrayList<>();
    private final OnClick onClick;

    public InboxAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void setData(List<ConversacionDto> nuevos) {
        data.clear();
        if (nuevos != null) data.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inbox, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ConversacionDto c = data.get(position);

        String nombre = c.otroNombre != null ? c.otroNombre : ("Usuario " + c.otroId);
        h.tvNombre.setText(nombre);
        h.tvInicial.setText(nombre.isEmpty() ? "U" : String.valueOf(nombre.charAt(0)).toUpperCase());
        h.tvUltimo.setText(c.ultimoMensaje != null ? c.ultimoMensaje : "");
        h.tvHora.setText(c.fechaUltimo != null ? c.fechaUltimo : "");

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(c);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvInicial, tvUltimo, tvHora;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNombre  = itemView.findViewById(R.id.tv_inbox_nombre);
            tvInicial = itemView.findViewById(R.id.tv_inbox_inicial);
            tvUltimo  = itemView.findViewById(R.id.tv_inbox_ultimo_mensaje);
            tvHora    = itemView.findViewById(R.id.tv_inbox_hora);
        }
    }
}
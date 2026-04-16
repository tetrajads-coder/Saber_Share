package com.example.saber_share.fragmentos.contenido.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.model.Publicacion;

import java.util.ArrayList;
import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Publicacion publicacion);
    }

    private final Context context;
    private List<Publicacion> listaOriginal;
    private List<Publicacion> listaFiltrada;
    private final int usuarioActualId;
    private final OnItemClickListener listener;

    public PublicacionAdapter(Context context, List<Publicacion> lista,
                              int usuarioActualId, OnItemClickListener listener) {
        this.context         = context;
        this.listaOriginal   = lista;
        this.listaFiltrada   = new ArrayList<>(lista);
        this.usuarioActualId = usuarioActualId;
        this.listener        = listener;
    }

    @NonNull
    @Override
    public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_publicacion, parent, false);
        return new PublicacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionViewHolder h, int position) {
        Publicacion p = listaFiltrada.get(position);

        // Tipo tag
        h.tvTipo.setText(Publicacion.TIPO_CURSO.equals(p.getTipo()) ? "CURSO" : "CLASE 1A1");

        // Título y descripción
        h.tvTitulo.setText(p.getTitulo());
        if (h.tvDescripcion != null)
            h.tvDescripcion.setText(p.getDescripcion() != null ? p.getDescripcion() : "");

        // Precio
        h.tvPrecio.setText(String.format("$%.2f", p.getPrecio()));

        // Vendedor
        boolean esMio = p.getIdAutor() == usuarioActualId;
        String nombreAutor = esMio ? "Tú" : (p.getAutor() != null ? p.getAutor() : "Anónimo");
        h.tvVendedor.setText(nombreAutor);
        h.tvVendedorInicial.setText(nombreAutor.isEmpty() ? "V"
                : String.valueOf(nombreAutor.charAt(0)).toUpperCase());

        // Calificación
        String calif = (p.getCalificacion() != null && !p.getCalificacion().equals("0"))
                ? "★ " + p.getCalificacion() : "★ 0.0";
        h.tvEstrellas.setText(calif);

        // Click en botón Ver y en la card completa
        h.btnVer.setOnClickListener(v -> { if (listener != null) listener.onItemClick(p); });
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(p); });
    }

    @Override
    public int getItemCount() { return listaFiltrada.size(); }

    public void setDatos(List<Publicacion> nuevosDatos) {
        this.listaOriginal = nuevosDatos;
        this.listaFiltrada = new ArrayList<>(nuevosDatos);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        if (texto == null || texto.isEmpty()) {
            listaFiltrada = new ArrayList<>(listaOriginal);
        } else {
            List<Publicacion> temp = new ArrayList<>();
            for (Publicacion p : listaOriginal) {
                if (p.getTitulo() != null &&
                        p.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                    temp.add(p);
                }
            }
            listaFiltrada = temp;
        }
        notifyDataSetChanged();
    }

    static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvTitulo, tvDescripcion, tvPrecio;
        TextView tvVendedor, tvVendedorInicial, tvEstrellas, tvNReviews;
        Button   btnVer;

        PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo            = itemView.findViewById(R.id.tv_item_tipo);
            tvTitulo          = itemView.findViewById(R.id.tv_item_titulo);
            tvDescripcion     = itemView.findViewById(R.id.tv_item_descripcion);
            tvPrecio          = itemView.findViewById(R.id.tv_item_precio);
            tvVendedor        = itemView.findViewById(R.id.tv_item_vendedor);
            tvVendedorInicial = itemView.findViewById(R.id.tv_item_vendedor_inicial);
            tvEstrellas       = itemView.findViewById(R.id.tv_item_estrellas);
            tvNReviews        = itemView.findViewById(R.id.tv_item_n_reviews);
            btnVer            = itemView.findViewById(R.id.btn_item_ver);
        }
    }
}
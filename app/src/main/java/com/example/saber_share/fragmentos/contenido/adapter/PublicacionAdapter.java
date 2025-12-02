package com.example.saber_share.fragmentos.contenido.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.model.Publicacion;

import java.util.ArrayList;
import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

    private Context context;
    private List<Publicacion> listaOriginal;
    private List<Publicacion> listaFiltrada;
    private int usuarioActualId; // <--- NUEVO: ID del usuario logueado
    private OnItemClickListener listener; // Para manejar el click

    // Interfaz para comunicar clicks al fragmento
    public interface OnItemClickListener {
        void onItemClick(Publicacion publicacion);
    }

    public PublicacionAdapter(Context context, List<Publicacion> lista, int usuarioActualId, OnItemClickListener listener) {
        this.context = context;
        this.listaOriginal = lista;
        this.listaFiltrada = new ArrayList<>(lista);
        this.usuarioActualId = usuarioActualId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_publicacion, parent, false);
        return new PublicacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionViewHolder holder, int position) {
        Publicacion p = listaFiltrada.get(position);

        String prefijo = Publicacion.TIPO_CURSO.equals(p.getTipo()) ? "Curso: " : "Clase 1 a 1: ";
        holder.tvTitulo.setText(prefijo + p.getTitulo());
        holder.tvPrecio.setText(String.format("$ %.2f MXN", p.getPrecio()));
        String calif = (p.getCalificacion() != null && !p.getCalificacion().equals("0")) ? p.getCalificacion() : "N/A";
        holder.tvCalificacion.setText(calif + " ★");

        // --- LÓGICA DE DUEÑO ---
        // OJO: Aquí asumimos que 'Publicacion' tiene un método getAutorId().
        // Si no lo tiene, necesitamos agregarlo al modelo Publicacion (ver Paso 1.1)
        boolean esMio = p.getIdAutor() == usuarioActualId;

        if (esMio) {
            holder.tvAutor.setText("Hecho por ti");
            holder.btnAccion.setText("Detalles");
            holder.btnAccion.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray)); // Opcional: cambiar color
        } else {
            holder.tvAutor.setText("Por: " + (p.getAutor() != null ? p.getAutor() : "Anónimo"));
            if (Publicacion.TIPO_CURSO.equals(p.getTipo())) {
                holder.btnAccion.setText("Pagar");
            } else {
                holder.btnAccion.setText("Agendar");
            }
            // Restaurar color original si es necesario
            // holder.btnAccion.setBackgroundTintList(...);
        }

        // Imágenes dummy
        if (Publicacion.TIPO_CURSO.equals(p.getTipo())) {
            holder.imgPortada.setImageResource(R.drawable.img);
        } else {
            holder.imgPortada.setImageResource(R.drawable.img_1);
        }

        // Click en el botón (Acción principal)
        holder.btnAccion.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(p);
        });

        // Click en la tarjeta completa (también lleva a detalles)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public void setDatos(List<Publicacion> nuevosDatos) {
        this.listaOriginal = nuevosDatos;
        this.listaFiltrada = new ArrayList<>(nuevosDatos);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        // ... (tu lógica de filtrado igual) ...
        // (Asegúrate de actualizar listaFiltrada y notifyDataSetChanged)
        if (texto.isEmpty()) {
            listaFiltrada = new ArrayList<>(listaOriginal);
        } else {
            List<Publicacion> temp = new ArrayList<>();
            for (Publicacion p : listaOriginal) {
                if (p.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                    temp.add(p);
                }
            }
            listaFiltrada = temp;
        }
        notifyDataSetChanged();
    }


    static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPortada;
        TextView tvTitulo, tvAutor, tvCalificacion, tvPrecio;
        Button btnAccion;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPortada = itemView.findViewById(R.id.imgPortada);
            tvTitulo = itemView.findViewById(R.id.tvTituloPub);
            tvAutor = itemView.findViewById(R.id.tvAutorPub);
            tvCalificacion = itemView.findViewById(R.id.tvCalificacionPub);
            tvPrecio = itemView.findViewById(R.id.tvPrecioPub);
            btnAccion = itemView.findViewById(R.id.btnAccionPub);
        }
    }
}
package com.example.saber_share.fragmentos.contenido.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.dialogs.CalificarDialog;
import com.example.saber_share.model.HistorialDto;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private final Context           context;
    private final List<HistorialDto> lista;

    public HistorialAdapter(Context context, List<HistorialDto> lista) {
        this.context = context;
        this.lista   = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        HistorialDto item = lista.get(position);

        // Título e ícono según tipo
        String titulo, icono, tipo;
        if (item.getCursoId() != null) {
            titulo = "Curso #" + item.getCursoId();
            icono  = "📚";
            tipo   = "CURSO";
        } else if (item.getServicioId() != null) {
            titulo = "Clase 1:1 #" + item.getServicioId();
            icono  = "🎓";
            tipo   = "SERVICIO";
        } else {
            titulo = "Movimiento";
            icono  = "💳";
            tipo   = null;
        }

        h.tvIcono.setText(icono);
        h.tvTitulo.setText(titulo);
        h.tvFecha.setText(item.getFechapago() != null ? item.getFechapago() : "---");
        h.tvMonto.setText(String.format("$%.2f", item.getPago() != null ? item.getPago() : 0.0));
        h.tvEstado.setText("PAGADO");

        // Click → abrir CalificarDialog con newInstance
        int itemId = item.getCursoId() != null ? item.getCursoId()
                : (item.getServicioId() != null ? item.getServicioId() : -1);

        if (itemId > 0 && tipo != null && context instanceof AppCompatActivity) {
            String tipoFinal = tipo;
            h.itemView.setOnClickListener(v -> {
                CalificarDialog dialog = CalificarDialog.newInstance(itemId, tipoFinal);
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(),
                        "calificar");
            });
        } else {
            h.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() { return lista != null ? lista.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcono, tvTitulo, tvFecha, tvMonto, tvEstado;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcono  = itemView.findViewById(R.id.tv_historial_icono);
            tvTitulo = itemView.findViewById(R.id.tv_historial_titulo);
            tvFecha  = itemView.findViewById(R.id.tv_historial_fecha);
            tvMonto  = itemView.findViewById(R.id.tv_historial_monto);
            tvEstado = itemView.findViewById(R.id.tv_historial_estado);
        }
    }
}
package com.example.saber_share.fragmentos.contenido.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saber_share.R;
import com.example.saber_share.fragmentos.dialogs.CalificarDialog;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.model.OpinionServicioDto;
import com.example.saber_share.model.OpinionesCursoDto;
import com.example.saber_share.util.api.OpinionApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private final Context context;
    private final List<HistorialDto> lista;

    public HistorialAdapter(Context context, List<HistorialDto> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorialDto item = lista.get(position);

        String titulo;
        String tipo;

        if (item.getCursoId() != null) {
            titulo = "Curso #" + item.getCursoId();
            tipo = "Curso";
        } else if (item.getServicioId() != null) {
            titulo = "Clase 1:1 #" + item.getServicioId();
            tipo = "Clase 1:1";
        } else {
            titulo = "Movimiento";
            tipo = "Compra";
        }

        holder.tvTitulo.setText(titulo);
        holder.tvTipo.setText(tipo);
        holder.tvFecha.setText(item.getFechapago() != null ? item.getFechapago() : "---");

        double pago = item.getPago() != null ? item.getPago() : 0.0;
        holder.tvPrecio.setText(String.format("$ %.2f", pago));

        // Si no es curso ni servicio, no se puede calificar
        boolean sePuedeCalificar = (item.getCursoId() != null || item.getServicioId() != null);
        holder.btnCalificar.setEnabled(sePuedeCalificar);
        holder.btnCalificar.setAlpha(sePuedeCalificar ? 1f : 0.4f);

        holder.btnCalificar.setOnClickListener(v -> {
            if (!sePuedeCalificar) {
                Toast.makeText(context, "Este movimiento no se puede calificar", Toast.LENGTH_SHORT).show();
                return;
            }

            if (context instanceof AppCompatActivity) {
                CalificarDialog dialog = new CalificarDialog();
                dialog.setListener((estrellas, comentario) -> enviarCalificacion(item, estrellas, comentario));
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "CalificarDialog");
            } else {
                Toast.makeText(context, "No se pudo abrir el dialog", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarCalificacion(HistorialDto item, int estrellas, String comentario) {
        OpinionApi api = RetrofitClient.getClient().create(OpinionApi.class);
        SessionManager session = new SessionManager(context);
        int miId = session.getUserId();

        if (item.getServicioId() != null) {
            OpinionServicioDto dto = new OpinionServicioDto(miId, item.getServicioId(), estrellas, comentario);

            api.calificarServicio(dto).enqueue(new Callback<OpinionServicioDto>() {
                @Override
                public void onResponse(Call<OpinionServicioDto> call, Response<OpinionServicioDto> response) {
                    Toast.makeText(context,
                            response.isSuccessful() ? "Clase calificada con exito" : "Error al calificar: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<OpinionServicioDto> call, Throwable t) {
                    Toast.makeText(context, "Fallo de conexion", Toast.LENGTH_SHORT).show();
                }
            });

        } else if (item.getCursoId() != null) {
            OpinionesCursoDto dto = new OpinionesCursoDto(miId, item.getCursoId(), estrellas, comentario);

            api.calificarCurso(dto).enqueue(new Callback<OpinionesCursoDto>() {
                @Override
                public void onResponse(Call<OpinionesCursoDto> call, Response<OpinionesCursoDto> response) {
                    Toast.makeText(context,
                            response.isSuccessful() ? "Curso calificado con exito" : "Error al calificar: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<OpinionesCursoDto> call, Throwable t) {
                    Toast.makeText(context, "Fallo de conexion", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvPrecio, tvTipo, tvFecha;
        Button btnCalificar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvHistorialTitulo);
            tvPrecio = itemView.findViewById(R.id.tvHistorialPrecio);
            tvTipo = itemView.findViewById(R.id.tvHistorialTipo);
            tvFecha = itemView.findViewById(R.id.tvHistorialFecha);
            btnCalificar = itemView.findViewById(R.id.btnCalificarHistorial);
        }
    }
}

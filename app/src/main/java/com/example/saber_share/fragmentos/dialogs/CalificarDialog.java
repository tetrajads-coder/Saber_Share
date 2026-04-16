package com.example.saber_share.fragmentos.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.saber_share.R;
import com.example.saber_share.model.OpinionServicioDto;
import com.example.saber_share.model.OpinionesCursoDto;
import com.example.saber_share.util.api.OpinionApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalificarDialog extends DialogFragment {

    private static final String ARG_ITEM_ID = "itemId";
    private static final String ARG_TIPO    = "tipo";

    public interface OnCalificarListener {
        void onCalificacionEnviada();
    }

    private OnCalificarListener listener;

    /** Crea el dialog pasando itemId y tipo */
    public static CalificarDialog newInstance(int itemId, String tipo) {
        CalificarDialog d = new CalificarDialog();
        Bundle args = new Bundle();
        args.putInt("itemId", itemId);
        args.putString("tipo", tipo);
        d.setArguments(args);
        return d;
    }

    public void setListener(OnCalificarListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int    itemId = getArguments() != null ? getArguments().getInt(ARG_ITEM_ID, -1) : -1;
        String tipo   = getArguments() != null ? getArguments().getString(ARG_TIPO, "CURSO") : "CURSO";
        int    userId = SessionManager.getInstance(requireContext()).getUsuarioId();

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_calificar, null);

        RatingBar         ratingBar    = view.findViewById(R.id.ratingBar);
        TextInputEditText etComentario = view.findViewById(R.id.etComentario);
        Button            btnEnviar    = view.findViewById(R.id.btnEnviarCalificacion);

        btnEnviar.setOnClickListener(v -> {
            int estrellas = (int) ratingBar.getRating();
            String comentario = etComentario.getText() != null
                    ? etComentario.getText().toString().trim() : "";
            if (comentario.isEmpty()) comentario = "Sin comentario";

            if (estrellas == 0) {
                Toast.makeText(getContext(), "Selecciona al menos 1 estrella",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnEnviar.setEnabled(false);
            btnEnviar.setText("Enviando...");
            enviarCalificacion(itemId, tipo, userId, estrellas, comentario, btnEnviar);
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Calificar")
                .create();
    }

    private void enviarCalificacion(int itemId, String tipo, int userId,
                                    int estrellas, String comentario, Button btnEnviar) {
        OpinionApi api = RetrofitClient.getInstance().create(OpinionApi.class);

        if ("CURSO".equals(tipo)) {
            OpinionesCursoDto dto = new OpinionesCursoDto();
            dto.setCalificacion(estrellas);
            dto.setComentario(comentario);
            dto.setUsuarioId(userId);
            dto.setCursoId(itemId);

            api.calificarCurso(dto).enqueue(new Callback<OpinionesCursoDto>() {
                @Override
                public void onResponse(@NonNull Call<OpinionesCursoDto> call,
                                       @NonNull Response<OpinionesCursoDto> response) {
                    manejarRespuesta(response.isSuccessful(), btnEnviar);
                }
                @Override
                public void onFailure(@NonNull Call<OpinionesCursoDto> call,
                                      @NonNull Throwable t) {
                    manejarError(btnEnviar);
                }
            });
        } else {
            OpinionServicioDto dto = new OpinionServicioDto();
            dto.setCalificacion(estrellas);
            dto.setComentario(comentario);
            dto.setUsuarioId(userId);
            dto.setServicioId(itemId);

            api.calificarServicio(dto).enqueue(new Callback<OpinionServicioDto>() {
                @Override
                public void onResponse(@NonNull Call<OpinionServicioDto> call,
                                       @NonNull Response<OpinionServicioDto> response) {
                    manejarRespuesta(response.isSuccessful(), btnEnviar);
                }
                @Override
                public void onFailure(@NonNull Call<OpinionServicioDto> call,
                                      @NonNull Throwable t) {
                    manejarError(btnEnviar);
                }
            });
        }
    }

    private void manejarRespuesta(boolean exito, Button btnEnviar) {
        if (!isAdded()) return;
        if (exito) {
            Toast.makeText(getContext(), "¡Calificación enviada!", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onCalificacionEnviada();
            dismiss();
        } else {
            Toast.makeText(getContext(), "Error al enviar calificación", Toast.LENGTH_SHORT).show();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar calificación");
        }
    }

    private void manejarError(Button btnEnviar) {
        if (!isAdded()) return;
        Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
        btnEnviar.setEnabled(true);
        btnEnviar.setText("Enviar calificación");
    }
}
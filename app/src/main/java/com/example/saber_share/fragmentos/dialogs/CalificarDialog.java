package com.example.saber_share.fragmentos.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.saber_share.R;

public class CalificarDialog extends DialogFragment {

    public interface OnCalificarListener {
        void onCalificar(int estrellas, String comentario);
    }

    private OnCalificarListener listener;

    public void setListener(OnCalificarListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Necesitamos crear este XML (dialog_calificar.xml) abajo
        View view = inflater.inflate(R.layout.dialog_calificar, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etComentario = view.findViewById(R.id.etComentario);
        Button btnEnviar = view.findViewById(R.id.btnEnviarCalificacion);

        btnEnviar.setOnClickListener(v -> {
            int estrellas = (int) ratingBar.getRating();
            String comentario = etComentario.getText().toString().trim();

            if (estrellas == 0) {
                Toast.makeText(getContext(), "Selecciona al menos 1 estrella", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onCalificar(estrellas, comentario);
            }
            dismiss();
        });

        builder.setView(view).setTitle("Calificar");
        return builder.create();
    }
}
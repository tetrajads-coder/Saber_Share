package com.example.saber_share.fragmentos.contenido;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.saber_share.R;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.util.api.HistorialApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.local.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstadisticasFragment extends Fragment {

    private TextView tvCursosTotal, tvClasesTotal, tvCursosMonto, tvClasesMonto, tvEstado;
    private BarChart barChart;
    private PieChart pieChart;

    private SessionManager sessionManager;

    public EstadisticasFragment() {
        super(R.layout.fragment_main_estadisticas);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        tvCursosTotal = view.findViewById(R.id.tvCursosTotal);
        tvClasesTotal = view.findViewById(R.id.tvClasesTotal);
        tvCursosMonto = view.findViewById(R.id.tvCursosMonto);
        tvClasesMonto = view.findViewById(R.id.tvClasesMonto);
        tvEstado = view.findViewById(R.id.tvEstado);

        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);

        configurarCharts();

        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            tvEstado.setText("Sesion invalida. Inicia sesion.");
            pintarVacio();
            return;
        }

        cargarStats(userId);
    }

    private void configurarCharts() {
        Description d1 = new Description();
        d1.setText("");
        barChart.setDescription(d1);
        barChart.getLegend().setEnabled(false);

        Description d2 = new Description();
        d2.setText("");
        pieChart.setDescription(d2);
        pieChart.getLegend().setEnabled(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHoleRadius(70f);
        pieChart.setTransparentCircleRadius(74f);
        pieChart.setCenterText("Cursos vs Clases");
        pieChart.setCenterTextSize(12f);
    }

    private void cargarStats(int userId) {
        tvEstado.setText("Cargando...");

        HistorialApi api = RetrofitClient.getClient().create(HistorialApi.class);
        api.historialPorUsuario(userId).enqueue(new Callback<List<HistorialDto>>() {
            @Override
            public void onResponse(Call<List<HistorialDto>> call, Response<List<HistorialDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvEstado.setText("Error al cargar estadisticas: " + response.code());
                    pintarVacio();
                    return;
                }

                List<HistorialDto> lista = response.body();
                if (lista.isEmpty()) {
                    tvEstado.setText("Aun no hay compras en tu historial.");
                    pintarVacio();
                    return;
                }

                // Contadores + montos
                int cursos = 0, clases = 0;
                double montoCursos = 0.0, montoClases = 0.0;

                // Barras: conteo por dia (ultimos 7)
                HashMap<String, Integer> conteoPorDia = new HashMap<>();
                SimpleDateFormat in1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat in2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat out = new SimpleDateFormat("MM-dd", Locale.getDefault());

                for (HistorialDto h : lista) {
                    if (h.getCursoId() != null) cursos++;
                    if (h.getServicioId() != null) clases++;

                    // Si tu HistorialDto tiene "pago" como numero:
                    // Si lo tienes como String, ajusta con Double.parseDouble(...)
                    try {
                        if (h.getPago() != null) {
                            double p = 0.0;
                            try {
                                p = Double.parseDouble(String.valueOf(h.getPago()));
                            } catch (Exception ignore) {}

                            if (h.getCursoId() != null) montoCursos += p;
                            if (h.getServicioId() != null) montoClases += p;
                        }
                    } catch (Exception ignore) {}

                    String fechaRaw = h.getFechapago(); // si tu campo se llama diferente, cambialo aqui
                    if (fechaRaw != null) {
                        String key = null;
                        try {
                            key = out.format(in1.parse(fechaRaw));
                        } catch (ParseException e1) {
                            try {
                                key = out.format(in2.parse(fechaRaw));
                            } catch (ParseException ignore) {}
                        }
                        if (key != null) {
                            Integer c = conteoPorDia.get(key);
                            conteoPorDia.put(key, (c == null ? 1 : c + 1));
                        }
                    }
                }

                tvCursosTotal.setText(String.valueOf(cursos));
                tvClasesTotal.setText(String.valueOf(clases));
                tvCursosMonto.setText(String.format(Locale.getDefault(), "$%.2f", montoCursos));
                tvClasesMonto.setText(String.format(Locale.getDefault(), "$%.2f", montoClases));

                pintarPie(cursos, clases);
                pintarBarras(conteoPorDia);

                tvEstado.setText("Listo");
            }

            @Override
            public void onFailure(Call<List<HistorialDto>> call, Throwable t) {
                tvEstado.setText("Error de red: " + (t.getMessage() != null ? t.getMessage() : ""));
                pintarVacio();
            }
        });
    }

    private void pintarPie(int cursos, int clases) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(Math.max(cursos, 0), "Cursos"));
        entries.add(new PieEntry(Math.max(clases, 0), "Clases"));

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setSliceSpace(2f);
        ds.setValueTextSize(12f);

        PieData data = new PieData(ds);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void pintarBarras(HashMap<String, Integer> conteoPorDia) {
        // Orden simple por clave (MM-dd). Si quieres 100% por fecha real, lo hacemos despues.
        ArrayList<String> keys = new ArrayList<>(conteoPorDia.keySet());
        java.util.Collections.sort(keys);

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            int v = conteoPorDia.get(k) != null ? conteoPorDia.get(k) : 0;
            entries.add(new BarEntry(i, v));
        }

        BarDataSet ds = new BarDataSet(entries, "Compras");
        ds.setValueTextSize(12f);

        BarData data = new BarData(ds);
        data.setBarWidth(0.7f);

        barChart.setData(data);
        barChart.getXAxis().setDrawLabels(false); // look iOS minimal
        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    private void pintarVacio() {
        tvCursosTotal.setText("0");
        tvClasesTotal.setText("0");
        tvCursosMonto.setText("$0.00");
        tvClasesMonto.setText("$0.00");

        pieChart.setData(null);
        pieChart.invalidate();

        barChart.setData(null);
        barChart.invalidate();
    }
}

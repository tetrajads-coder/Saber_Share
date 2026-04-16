package com.example.saber_share.fragmentos.contenido;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.saber_share.R;
import com.example.saber_share.model.CursoDto;
import com.example.saber_share.model.HistorialDto;
import com.example.saber_share.model.ServicioDto;
import com.example.saber_share.util.api.CursoApi;
import com.example.saber_share.util.api.HistorialApi;
import com.example.saber_share.util.api.RetrofitClient;
import com.example.saber_share.util.api.ServicioApi;
import com.example.saber_share.util.local.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstadisticasFragment extends Fragment {

    private TextView    tvIngresos, tvVentas, tvRating;
    private ProgressBar progressStats;
    private BarChart    chartVentasMes;
    private LineChart   chartIngresos;
    private PieChart    chartCalificaciones;

    private SessionManager sessionManager;

    // IDs de mis publicaciones (cursos + servicios propios)
    private final Set<Integer> misCursoIds    = new HashSet<>();
    private final Set<Integer> misServicioIds = new HashSet<>();
    private final int[] pendientesApi = {3}; // cursos + servicios + historial

    // Datos acumulados
    private List<HistorialDto> todoElHistorial = new ArrayList<>();

    public EstadisticasFragment() {
        super(R.layout.fragment_main_estadisticas);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());

        tvIngresos          = view.findViewById(R.id.tv_stats_ingresos);
        tvVentas            = view.findViewById(R.id.tv_stats_ventas);
        tvRating            = view.findViewById(R.id.tv_stats_rating);
        progressStats       = view.findViewById(R.id.progress_stats);
        chartVentasMes      = view.findViewById(R.id.chart_ventas_mes);
        chartIngresos       = view.findViewById(R.id.chart_ingresos);
        chartCalificaciones = view.findViewById(R.id.chart_calificaciones);

        configurarCharts();

        int userId = sessionManager.getUsuarioId();
        if (userId <= 0) { pintarVacio(); return; }

        progressStats.setVisibility(View.VISIBLE);
        cargarMisCursos(userId);
        cargarMisServicios(userId);
        cargarHistorialGlobal();
    }

    // ── Configuración visual de charts ───────────────────────────────────────

    private void configurarCharts() {
        Description desc = new Description();
        desc.setText("");

        chartVentasMes.setDescription(desc);
        chartVentasMes.getLegend().setEnabled(false);
        chartVentasMes.getAxisRight().setEnabled(false);
        chartVentasMes.getXAxis().setDrawGridLines(false);
        chartVentasMes.getAxisLeft().setTextColor(Color.WHITE);
        chartVentasMes.getXAxis().setTextColor(Color.WHITE);

        chartIngresos.setDescription(desc);
        chartIngresos.getLegend().setEnabled(false);
        chartIngresos.getAxisRight().setEnabled(false);
        chartIngresos.getAxisLeft().setTextColor(Color.WHITE);
        chartIngresos.getXAxis().setTextColor(Color.WHITE);

        chartCalificaciones.setDescription(desc);
        chartCalificaciones.setDrawEntryLabels(false);
        chartCalificaciones.setHoleRadius(60f);
        chartCalificaciones.setTransparentCircleRadius(64f);
        chartCalificaciones.setCenterText("Ventas");
        chartCalificaciones.setCenterTextSize(12f);
        chartCalificaciones.setCenterTextColor(Color.WHITE);
        chartCalificaciones.setHoleColor(Color.TRANSPARENT);
    }

    // ── Carga de datos ────────────────────────────────────────────────────────

    private void cargarMisCursos(int userId) {
        RetrofitClient.getInstance().create(CursoApi.class)
                .lista().enqueue(new Callback<List<CursoDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CursoDto>> call,
                                           @NonNull Response<List<CursoDto>> response) {
                        if (response.isSuccessful() && response.body() != null)
                            for (CursoDto c : response.body())
                                if (c.getUsuarioId() != null && c.getUsuarioId() == userId
                                        && c.getIdCurso() != null)
                                    misCursoIds.add(c.getIdCurso());
                        verificarYProcesar();
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<CursoDto>> call, @NonNull Throwable t) {
                        verificarYProcesar();
                    }
                });
    }

    private void cargarMisServicios(int userId) {
        RetrofitClient.getInstance().create(ServicioApi.class)
                .lista().enqueue(new Callback<List<ServicioDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ServicioDto>> call,
                                           @NonNull Response<List<ServicioDto>> response) {
                        if (response.isSuccessful() && response.body() != null)
                            for (ServicioDto s : response.body())
                                if (s.getUsuarioId() != null && s.getUsuarioId() == userId
                                        && s.getServicioId() != null)
                                    misServicioIds.add(s.getServicioId());
                        verificarYProcesar();
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<ServicioDto>> call, @NonNull Throwable t) {
                        verificarYProcesar();
                    }
                });
    }

    // Cargamos TODO el historial y luego filtramos solo los que son de mis publicaciones
    private void cargarHistorialGlobal() {
        // El backend no tiene endpoint "ventas del vendedor", así que usamos
        // historialPorUsuario con un userId=0 o el endpoint lista si existe.
        // Por ahora cargamos el historial del propio vendedor y cruzamos con sus IDs.
        int userId = sessionManager.getUsuarioId();
        RetrofitClient.getInstance().create(HistorialApi.class)
                .historialPorUsuario(userId).enqueue(new Callback<List<HistorialDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<HistorialDto>> call,
                                           @NonNull Response<List<HistorialDto>> response) {
                        if (response.isSuccessful() && response.body() != null)
                            todoElHistorial = response.body();
                        verificarYProcesar();
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<HistorialDto>> call, @NonNull Throwable t) {
                        verificarYProcesar();
                    }
                });
    }

    // Espera a que las 3 APIs terminen antes de calcular
    private void verificarYProcesar() {
        pendientesApi[0]--;
        if (pendientesApi[0] > 0) return;

        if (getContext() == null) return;
        progressStats.setVisibility(View.GONE);

        // Filtrar historial: solo registros donde el comprador pagó por MIS publicaciones
        List<HistorialDto> misVentas = new ArrayList<>();
        for (HistorialDto h : todoElHistorial) {
            boolean esMiCurso    = h.getCursoId()    != null && misCursoIds.contains(h.getCursoId());
            boolean esMiServicio = h.getServicioId() != null && misServicioIds.contains(h.getServicioId());
            if (esMiCurso || esMiServicio) misVentas.add(h);
        }

        if (misVentas.isEmpty()) { pintarVacio(); return; }

        calcularYPintar(misVentas);
    }

    private void calcularYPintar(List<HistorialDto> ventas) {
        double totalIngresos = 0.0;
        HashMap<String, Double> ingresosPorMes  = new HashMap<>();
        HashMap<String, Integer> ventasPorMes   = new HashMap<>();

        SimpleDateFormat inFmt  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outFmt = new SimpleDateFormat("MMM yy", Locale.getDefault());

        for (HistorialDto h : ventas) {
            double pago = 0;
            try { pago = Double.parseDouble(String.valueOf(h.getPago())); } catch (Exception ignore) {}
            totalIngresos += pago;

            String mesKey = "?";
            if (h.getFechapago() != null) {
                try { mesKey = outFmt.format(inFmt.parse(h.getFechapago())); }
                catch (ParseException ignore) {}
            }

            Double actual = ingresosPorMes.get(mesKey);
            ingresosPorMes.put(mesKey, (actual == null ? 0 : actual) + pago);

            Integer cnt = ventasPorMes.get(mesKey);
            ventasPorMes.put(mesKey, (cnt == null ? 0 : cnt) + 1);
        }

        tvIngresos.setText(String.format(Locale.getDefault(), "$%.0f", totalIngresos));
        tvVentas.setText(String.valueOf(ventas.size()));
        tvRating.setText("★ 0.0");

        pintarBarrasVentas(ventasPorMes);
        pintarLineaIngresos(ingresosPorMes);
        pintarPie(ventas.size());
    }

    private void pintarBarrasVentas(HashMap<String, Integer> datos) {
        List<String> keys = new ArrayList<>(datos.keySet());
        Collections.sort(keys);
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++)
            entries.add(new BarEntry(i, datos.get(keys.get(i))));

        BarDataSet ds = new BarDataSet(entries, "Ventas por mes");
        ds.setColor(0xFF7ED321);
        ds.setValueTextColor(Color.WHITE);
        ds.setValueTextSize(10f);

        BarData data = new BarData(ds);
        data.setBarWidth(0.6f);
        chartVentasMes.setData(data);
        chartVentasMes.getXAxis().setDrawLabels(false);
        chartVentasMes.invalidate();
    }

    private void pintarLineaIngresos(HashMap<String, Double> datos) {
        List<String> keys = new ArrayList<>(datos.keySet());
        Collections.sort(keys);
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++)
            entries.add(new Entry(i, datos.get(keys.get(i)).floatValue()));

        LineDataSet ds = new LineDataSet(entries, "Ingresos");
        ds.setColor(0xFF7ED321);
        ds.setCircleColor(0xFF7ED321);
        ds.setValueTextColor(Color.WHITE);
        ds.setLineWidth(2f);
        ds.setCircleRadius(4f);
        ds.setValueTextSize(10f);

        chartIngresos.setData(new LineData(ds));
        chartIngresos.getXAxis().setDrawLabels(false);
        chartIngresos.invalidate();
    }

    private void pintarPie(int total) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(total, "Ventas"));
        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColor(0xFF7ED321);
        ds.setValueTextColor(Color.WHITE);
        ds.setValueTextSize(11f);
        chartCalificaciones.setData(new PieData(ds));
        chartCalificaciones.invalidate();
    }

    private void pintarVacio() {
        if (tvIngresos != null) tvIngresos.setText("$0");
        if (tvVentas   != null) tvVentas.setText("0");
        if (tvRating   != null) tvRating.setText("★ 0.0");
        if (chartVentasMes      != null) { chartVentasMes.setData(null);      chartVentasMes.invalidate(); }
        if (chartIngresos       != null) { chartIngresos.setData(null);       chartIngresos.invalidate(); }
        if (chartCalificaciones != null) { chartCalificaciones.setData(null); chartCalificaciones.invalidate(); }
    }
}
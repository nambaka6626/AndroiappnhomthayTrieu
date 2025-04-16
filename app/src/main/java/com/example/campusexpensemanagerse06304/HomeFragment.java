package com.example.campusexpensemanagerse06304;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.campusexpensemanagerse06304.database.BudgetDb;
import com.example.campusexpensemanagerse06304.model.Expense;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HomeFragment extends Fragment {

    private HorizontalBarChart chartByMonth, chartByCategory;
    private BudgetDb budgetDb;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chartByMonth = view.findViewById(R.id.chart_by_month);
        chartByCategory = view.findViewById(R.id.chart_by_category);
        budgetDb = new BudgetDb(requireContext());

        loadMonthlyExpensesChart();
        loadCategoryExpensesChart();

        // Thêm xử lý nút cập nhật biểu đồ
        Button btnRefresh = view.findViewById(R.id.btn_refresh_charts);
        btnRefresh.setOnClickListener(v -> {
            loadMonthlyExpensesChart();
            loadCategoryExpensesChart();
        });
    }

    private void loadMonthlyExpensesChart() {
        List<Expense> expenses = budgetDb.getAllExpenses();
        Map<String, Double> monthlyTotals = new TreeMap<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        for (Expense e : expenses) {
            try {
                Date date = inputFormat.parse(e.getDate());
                String monthKey = monthFormat.format(date);
                monthlyTotals.put(monthKey, monthlyTotals.getOrDefault(monthKey, 0.0) + e.getAmount());
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        setupBarChart(chartByMonth, monthlyTotals);
    }

    private void loadCategoryExpensesChart() {
        List<Expense> expenses = budgetDb.getAllExpenses();
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense e : expenses) {
            String category = e.getCategory();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + e.getAmount());
        }

        setupBarChart(chartByCategory, categoryTotals);
    }

    private void setupBarChart(HorizontalBarChart chart, Map<String, Double> dataMap) {
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(dataMap.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue()); // Tăng dần

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < sortedEntries.size(); i++) {
            Map.Entry<String, Double> entry = sortedEntries.get(i);
            entries.add(new BarEntry(i, entry.getValue().floatValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu");
        dataSet.setColor(Color.parseColor("#3F51B5")); // Màu xanh

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        barData.setValueTextSize(12f);

        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setFitBars(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < labels.size() ? labels.get(index) : "";
            }
        });

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setGranularity(1f);
        chart.invalidate(); // refresh
    }

    @Override
    public void onResume() {
        super.onResume();
        if (budgetDb != null) {
            budgetDb.setExpenseUpdateListener(new BudgetDb.ExpenseUpdateListener() {
                @Override
                public void onExpenseUpdated() {
                    loadMonthlyExpensesChart();
                    loadCategoryExpensesChart();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (budgetDb != null) budgetDb.close();
    }
}

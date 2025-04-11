package com.example.campusexpensemanagerse06304;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.campusexpensemanagerse06304.adapter.BudgetAdapter;
import com.example.campusexpensemanagerse06304.database.BudgetDb;
import com.example.campusexpensemanagerse06304.model.Budget;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {
    private RecyclerView rvBudgetCategories;
    private FloatingActionButton btnAddBudget;
    private Button btnUpdateChart;
    private ProgressBar progressBudget;
    private TextView tvTotalBudget, tvBudgetProgress;
    private PieChart pieChart;
    private List<Budget> budgetList;
    private BudgetAdapter budgetAdapter;
    private BudgetDb budgetDb;

    public BudgetFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        budgetDb = new BudgetDb(getContext());

        // UI mapping
        rvBudgetCategories = view.findViewById(R.id.rvBudgetCategories);
        btnAddBudget = view.findViewById(R.id.btnAddBudget);
        btnUpdateChart = view.findViewById(R.id.btnUpdateChart);
        progressBudget = view.findViewById(R.id.progressBudget);
        tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
        tvBudgetProgress = view.findViewById(R.id.tvBudgetProgress);
        pieChart = view.findViewById(R.id.pieChartBudget);

        rvBudgetCategories.setLayoutManager(new LinearLayoutManager(getActivity()));

        budgetList = budgetDb.getAllBudgets();
//        if (budgetList.isEmpty()) {
//            budgetDb.insertBudget("Ăn uống", 2000000, 1800000);
//            budgetDb.insertBudget("Giải trí", 1000000, 500000);
//            budgetDb.insertBudget("Di chuyển", 800000, 900000);
//            budgetDb.insertBudget("Khác", 500000, 200000);
//            budgetList = budgetDb.getAllBudgets();
//        }

        budgetAdapter = new BudgetAdapter(getActivity(), budgetList, budgetDb, this::updateUI);
        rvBudgetCategories.setAdapter(budgetAdapter);

        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());
        btnUpdateChart.setOnClickListener(v -> updateUI());

        updateUI();

        return view;
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm ngân sách");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        EditText edtCategory = view.findViewById(R.id.edtCategory);
        EditText edtAmount = view.findViewById(R.id.edtAmount);
        EditText edtSpent = view.findViewById(R.id.edtSpent);

        builder.setView(view);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String category = edtCategory.getText().toString();
            String amountStr = edtAmount.getText().toString();
            String spentStr = edtSpent.getText().toString();

            if (!category.isEmpty() && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    double spent = spentStr.isEmpty() ? 0 : Double.parseDouble(spentStr);

                    if (spent > amount) {
                        Toast.makeText(getContext(), "Số tiền đã chi không thể lớn hơn ngân sách", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long result = budgetDb.insertBudget(category, amount, spent);
                    if (result != -1) {
                        budgetList.clear();
                        budgetList.addAll(budgetDb.getAllBudgets());
                        budgetAdapter.notifyDataSetChanged();
                        updateUI();
                        Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateUI() {
        updatePieChart();
        updateSummary();
    }

    private void updatePieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Budget budget : budgetList) {
            if (budget.getSpent() > 0) {
                entries.add(new PieEntry((float) budget.getSpent(), budget.getCategory()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu");

        // Tạo mảng màu cố định
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 99, 71));    // Màu đỏ cam
        colors.add(Color.rgb(60, 179, 113));   // Màu xanh lá
        colors.add(Color.rgb(106, 90, 205));   // Màu tím
        colors.add(Color.rgb(255, 165, 0));    // Màu cam
        colors.add(Color.rgb(0, 191, 255));    // Màu xanh dương
        colors.add(Color.rgb(255, 192, 203));  // Màu hồng
        colors.add(Color.rgb(128, 128, 128));  // Màu xám

        dataSet.setColors(colors);
        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);

        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void updateSummary() {
        double totalBudget = 0;
        double totalSpent = 0;

        for (Budget budget : budgetList) {
            totalBudget += budget.getAmount();
            totalSpent += budget.getSpent();
        }

        tvTotalBudget.setText(String.format("Tổng ngân sách: %,.0f VNĐ", totalBudget));
        tvBudgetProgress.setText(String.format("Đã chi tiêu: %,.0f VNĐ", totalSpent));

        int progress = totalBudget > 0 ? (int) ((totalSpent / totalBudget) * 100) : 0;
        progressBudget.setProgress(progress);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (budgetDb != null) {
            budgetDb.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load lại danh sách ngân sách mới nhất từ DB
        budgetList.clear();
        budgetList.addAll(budgetDb.getAllBudgets());
        budgetAdapter.notifyDataSetChanged();
        updateUI(); // Cập nhật lại PieChart và Summary
    }

}
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

    public BudgetFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // Ánh xạ UI
        rvBudgetCategories = view.findViewById(R.id.rvBudgetCategories);
        btnAddBudget = view.findViewById(R.id.btnAddBudget);
        btnUpdateChart = view.findViewById(R.id.btnUpdateChart);
        pieChart = view.findViewById(R.id.pieChartBudget);

        // Cấu hình RecyclerView
        rvBudgetCategories.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Dữ liệu mẫu
        budgetList = new ArrayList<>();
        budgetList.add(new Budget("Ăn uống", 2000000, 1800000));
        budgetList.add(new Budget("Giải trí", 1000000, 500000));
        budgetList.add(new Budget("Di chuyển", 800000, 900000));
        budgetList.add(new Budget("Khác", 500000, 200000));

        // Adapter
        budgetAdapter = new BudgetAdapter(getActivity(), budgetList);
        rvBudgetCategories.setAdapter(budgetAdapter);

        // Sự kiện thêm ngân sách
        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());

        // Sự kiện cập nhật biểu đồ
        btnUpdateChart.setOnClickListener(v -> updatePieChart());

        // Hiển thị biểu đồ ban đầu
        updatePieChart();

        return view;
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm ngân sách");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        EditText edtCategory = view.findViewById(R.id.edtCategory);
        EditText edtAmount = view.findViewById(R.id.edtAmount);

        builder.setView(view);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String category = edtCategory.getText().toString();
            String amountStr = edtAmount.getText().toString();

            if (!category.isEmpty() && !amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                budgetList.add(new Budget(category, amount, 0));
                budgetAdapter.notifyDataSetChanged();
                updatePieChart();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updatePieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Budget budget : budgetList) {
            if (budget.getSpent() > 0) {
                entries.add(new PieEntry((float) budget.getSpent(), budget.getCategory()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);

        pieChart.setData(pieData);
        pieChart.invalidate(); // Refresh biểu đồ
    }
}

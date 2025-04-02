package com.example.campusexpensemanagerse06304;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.campusexpensemanagerse06304.adapter.TransactionAdapter;
import com.example.campusexpensemanagerse06304.model.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass representing the Home screen.
 */
public class HomeFragment extends Fragment {

    private TextView tvBalance, tvTotalExpense;
    private PieChart pieChart;
    private RecyclerView rvRecentTransactions;
    private FloatingActionButton btnAddExpense;
    private List<Transaction> transactionList;
    private TransactionAdapter transactionAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ view
        tvBalance = view.findViewById(R.id.tvBalance);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        pieChart = view.findViewById(R.id.pieChart);
        rvRecentTransactions = view.findViewById(R.id.rvRecentTransactions);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);

        // Dữ liệu mẫu
        loadMockData();

        // Thiết lập biểu đồ
        setupPieChart();

        // Thiết lập danh sách giao dịch gần đây
        setupRecyclerView();

        // Sự kiện khi bấm nút thêm giao dịch
        btnAddExpense.setOnClickListener(v -> {
            // TODO: Mở dialog hoặc activity để thêm giao dịch
        });

        return view;
    }

    private void loadMockData() {
        transactionList = new ArrayList<>();
        transactionList.add(new Transaction("Mua sắm", 500000, "20/03/2025"));
        transactionList.add(new Transaction("Ăn uống", 200000, "19/03/2025"));
        transactionList.add(new Transaction("Giải trí", 150000, "18/03/2025"));
        transactionList.add(new Transaction("Đi lại", 100000, "17/03/2025"));
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(500000, "Mua sắm"));
        entries.add(new PieEntry(200000, "Ăn uống"));
        entries.add(new PieEntry(150000, "Giải trí"));
        entries.add(new PieEntry(100000, "Đi lại"));

        PieDataSet dataSet = new PieDataSet(entries, "Danh mục");
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(transactionList);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentTransactions.setAdapter(transactionAdapter);
    }
}

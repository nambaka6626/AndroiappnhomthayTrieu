package com.example.campusexpensemanagerse06304;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.campusexpensemanagerse06304.adapter.ExpensesAdapter;
import com.example.campusexpensemanagerse06304.model.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExpensesFragment extends Fragment {
    private RecyclerView rvExpenses;
    private FloatingActionButton btnAddExpense;
    private EditText etSearchDate; // Thêm EditText để tìm kiếm ngày
    private List<Expense> expenseList;
    private List<Expense> filteredList; // Danh sách đã lọc
    private ExpensesAdapter expensesAdapter;

    public ExpensesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        rvExpenses = view.findViewById(R.id.rvExpenses);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);
        etSearchDate = view.findViewById(R.id.etSearchDate); // Liên kết EditText
        rvExpenses.setLayoutManager(new LinearLayoutManager(getActivity()));

        expenseList = new ArrayList<>();
        filteredList = new ArrayList<>();
        loadSampleExpenses();
        filteredList.addAll(expenseList); // Ban đầu hiển thị toàn bộ danh sách
        sortByDateDescending(); // Sắp xếp ban đầu theo ngày giảm dần

        expensesAdapter = new ExpensesAdapter(filteredList);
        rvExpenses.setAdapter(expensesAdapter);

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        // Xử lý tìm kiếm theo ngày
        etSearchDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterExpensesByDate(s.toString());
            }
        });

        expensesAdapter.setOnExpenseClickListener(new ExpensesAdapter.OnExpenseClickListener() {
            @Override
            public void onEdit(Expense expense, int position) {
                showEditExpenseDialog(expense, position);
            }

            @Override
            public void onDelete(int position) {
                showDeleteConfirmDialog(filteredList.get(position));
            }
        });

        return view;
    }

    private void loadSampleExpenses() {
        expenseList.add(new Expense("Bữa sáng tại quán cà phê", "Ăn uống", 50000, "2025-03-30"));
        expenseList.add(new Expense("Xe buýt đi học", "Di chuyển", 15000, "2025-03-29"));
        expenseList.add(new Expense("Mua sách lập trình", "Mua sắm", 200000, "2025-03-28"));
        expenseList.add(new Expense("Xem phim cùng bạn bè", "Giải trí", 80000, "2025-03-27"));
        expenseList.add(new Expense("Xem phim cùng bạn bè", "Giải trí", 80000, "2025-03-27"));
    }

    // Hàm lọc danh sách theo ngày
    private void filterExpensesByDate(String date) {
        filteredList.clear();
        if (date.isEmpty()) {
            filteredList.addAll(expenseList); // Nếu không nhập ngày, hiển thị tất cả
        } else {
            for (Expense expense : expenseList) {
                if (expense.getDate().equals(date)) {
                    filteredList.add(expense); // Chỉ thêm các khoản chi tiêu khớp với ngày
                }
            }
        }
        sortByDateDescending(); // Sắp xếp sau khi lọc
        expensesAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    // Hàm sắp xếp theo ngày giảm dần (ngày gần nhất lên đầu)
    private void sortByDateDescending() {
        Collections.sort(filteredList, new Comparator<Expense>() {
            @Override
            public int compare(Expense e1, Expense e2) {
                return e2.getDate().compareTo(e1.getDate()); // Sắp xếp giảm dần
            }
        });
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        Spinner spCategory = dialogView.findViewById(R.id.spCategory);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Tái sử dụng danh mục từ filter_options đã định nghĩa trong layout chính
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            try {
                String description = etDescription.getText().toString().trim();
                String amountStr = etAmount.getText().toString().trim();
                String date = etDate.getText().toString().trim();
                String category = spCategory.getSelectedItem().toString();

                if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
                    Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(getActivity(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    Toast.makeText(getActivity(), "Ngày phải có định dạng YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                    return;
                }

                Expense newExpense = new Expense(description, category, amount, date);
                expenseList.add(newExpense);
                filterExpensesByDate(etSearchDate.getText().toString()); // Cập nhật danh sách lọc sau khi thêm
                Toast.makeText(getActivity(), "Đã thêm khoản chi tiêu", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } catch (Exception e) {
                Toast.makeText(getActivity(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showEditExpenseDialog(Expense expense, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        Spinner spCategory = dialogView.findViewById(R.id.spCategory);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        etDescription.setText(expense.getDescription());
        etAmount.setText(String.valueOf(expense.getAmount()));
        etDate.setText(expense.getDate());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        btnSave.setOnClickListener(v -> {
            String newDescription = etDescription.getText().toString();
            double newAmount = Double.parseDouble(etAmount.getText().toString());
            String newDate = etDate.getText().toString();

            if (newDescription.isEmpty() || newDate.isEmpty()) {
                Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            expense.setDescription(newDescription);
            expense.setAmount(newAmount);
            expense.setDate(newDate);
            filterExpensesByDate(etSearchDate.getText().toString()); // Cập nhật danh sách lọc sau khi chỉnh sửa
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteConfirmDialog(Expense expenseToDelete) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Xóa khoản chi tiêu")
                .setMessage("Bạn có chắc chắn muốn xóa khoản chi tiêu: " + expenseToDelete.getDescription() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    expenseList.remove(expenseToDelete);
                    filterExpensesByDate(etSearchDate.getText().toString()); // Cập nhật danh sách lọc sau khi xóa
                    Toast.makeText(getActivity(), "Đã xóa khoản chi tiêu", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
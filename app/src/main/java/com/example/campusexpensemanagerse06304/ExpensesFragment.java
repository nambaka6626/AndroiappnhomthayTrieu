    package com.example.campusexpensemanagerse06304;
    
    import android.app.AlertDialog;
    import android.os.Bundle;
    import androidx.fragment.app.Fragment;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Spinner;
    import android.widget.Toast;
    import com.example.campusexpensemanagerse06304.adapter.ExpensesAdapter;
    import com.example.campusexpensemanagerse06304.database.BudgetDb;
    import com.example.campusexpensemanagerse06304.model.Budget;
    import com.example.campusexpensemanagerse06304.model.Expense;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Comparator;
    import java.util.List;
    
    public class ExpensesFragment extends Fragment {
        private RecyclerView rvExpenses;
        private FloatingActionButton btnAddExpense;
        private EditText etSearchDate;
        private List<Expense> expenseList;
        private List<Expense> filteredList;
        private ExpensesAdapter expensesAdapter;
        private BudgetDb budgetDb;
        private List<Budget> budgetList;
    
        public ExpensesFragment() {}


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_expenses, container, false);
    
            budgetDb = new BudgetDb(getContext());
            budgetList = budgetDb.getAllBudgets();
    
            rvExpenses = view.findViewById(R.id.rvExpenses);
            btnAddExpense = view.findViewById(R.id.btnAddExpense);
            etSearchDate = view.findViewById(R.id.etSearchDate);
            rvExpenses.setLayoutManager(new LinearLayoutManager(getActivity()));
    
            expenseList = new ArrayList<>();
            filteredList = new ArrayList<>();
            loadSampleExpenses();
            filteredList.addAll(expenseList);
            sortByDateDescending();
    
            expensesAdapter = new ExpensesAdapter(filteredList);
            rvExpenses.setAdapter(expensesAdapter);
    
            btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    
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
            expenseList.clear();
            expenseList.addAll(budgetDb.getAllExpenses());
        }


        private void filterExpensesByDate(String date) {
            filteredList.clear();
            if (date.isEmpty()) {
                filteredList.addAll(expenseList);
            } else {
                for (Expense expense : expenseList) {
                    if (expense.getDate().equals(date)) {
                        filteredList.add(expense);
                    }
                }
            }
            sortByDateDescending();
            expensesAdapter.notifyDataSetChanged();
        }
    
        private void sortByDateDescending() {
            Collections.sort(filteredList, new Comparator<Expense>() {
                @Override
                public int compare(Expense e1, Expense e2) {
                    return e2.getDate().compareTo(e1.getDate());
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

            // Lấy danh sách category từ BudgetDb
            List<String> categories = new ArrayList<>();
            budgetList = budgetDb.getAllBudgets(); // Luôn lấy danh sách mới
            categories.clear();
            for (Budget budget : budgetList) {
                categories.add(budget.getCategory());
            }


            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, categories);
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

                    // Kiểm tra ngân sách và cập nhật nếu đủ
                    for (Budget budget : budgetList) {
                        if (budget.getCategory().equals(category)) {
                            double newSpent = budget.getSpent() + amount;
                            if (newSpent > budget.getAmount()) {
                                Toast.makeText(getActivity(), "Số tiền chi vượt quá ngân sách cho danh mục này", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Cập nhật chi tiêu vào ngân sách
                            budget.setSpent(newSpent);
                            budgetDb.updateBudget(budget);
                            break;
                        }
                    }


                    boolean added = budgetDb.addExpense(amount, category, date, description);
                    if (added) {
                        Expense newExpense = new Expense(description, category, amount, date);
                        expenseList.add(newExpense);
                        filterExpensesByDate(etSearchDate.getText().toString());
                        Toast.makeText(getActivity(), "Đã thêm khoản chi tiêu", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getActivity(), "Không thể thêm: lỗi khi lưu khoản chi tiêu", Toast.LENGTH_SHORT).show();
                    }

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
    
            // Lấy danh sách category từ BudgetDb
            List<String> categories = new ArrayList<>();
            for (Budget budget : budgetList) {
                categories.add(budget.getCategory());
            }
    
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spCategory.setAdapter(adapter);
    
            // Đặt lại category hiện tại của expense
            int categoryPosition = adapter.getPosition(expense.getCategory());
            if (categoryPosition >= 0) {
                spCategory.setSelection(categoryPosition);
            }
    
            AlertDialog dialog = builder.create();
            btnSave.setOnClickListener(v -> {
                String newDescription = etDescription.getText().toString();
                double newAmount = Double.parseDouble(etAmount.getText().toString());
                String newDate = etDate.getText().toString();
                String newCategory = spCategory.getSelectedItem().toString();
    
                if (newDescription.isEmpty() || newDate.isEmpty()) {
                    Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
    
                // Cập nhật số tiền đã chi trong BudgetDb
                if (!expense.getCategory().equals(newCategory) || expense.getAmount() != newAmount) {
                    // Trừ số tiền cũ
                    for (Budget budget : budgetList) {
                        if (budget.getCategory().equals(expense.getCategory())) {
                            budget.setSpent(budget.getSpent() - expense.getAmount());
                            budgetDb.updateBudget(budget);
                            break;
                        }
                    }
    
                    // Cộng số tiền mới
                    for (Budget budget : budgetList) {
                        if (budget.getCategory().equals(newCategory)) {
                            double newSpent = budget.getSpent() + newAmount;
                            if (newSpent > budget.getAmount()) {
                                Toast.makeText(getActivity(), "Số tiền chi vượt quá ngân sách cho danh mục này", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            budget.setSpent(newSpent);
                            budgetDb.updateBudget(budget);
                            break;
                        }
                    }
                }
    
                expense.setDescription(newDescription);
                expense.setAmount(newAmount);
                expense.setDate(newDate);
                expense.setCategory(newCategory);
                filterExpensesByDate(etSearchDate.getText().toString());
                dialog.dismiss();
            });
    
            dialog.show();
        }

        private void showDeleteConfirmDialog(Expense expenseToDelete) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Xóa khoản chi tiêu")
                    .setMessage("Bạn có chắc chắn muốn xóa khoản chi tiêu: " + expenseToDelete.getDescription() + "?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // 1. Cập nhật số tiền đã chi trong BudgetDb
                        for (Budget budget : budgetList) {
                            if (budget.getCategory().equals(expenseToDelete.getCategory())) {
                                budget.setSpent(budget.getSpent() - expenseToDelete.getAmount());
                                budgetDb.updateBudget(budget);
                                break;
                            }
                        }

                        // 2. Xóa khỏi database
                        budgetDb.deleteExpense(expenseToDelete); // <-- dòng này bạn cần thêm

                        // 3. Xóa khỏi danh sách hiển thị
                        expenseList.remove(expenseToDelete);
                        filterExpensesByDate(etSearchDate.getText().toString());

                        Toast.makeText(getActivity(), "Đã xóa khoản chi tiêu", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            if (budgetDb != null) {
                budgetDb.close();
            }
        }
    }
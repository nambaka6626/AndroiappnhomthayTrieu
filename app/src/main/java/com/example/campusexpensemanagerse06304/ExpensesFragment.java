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
            Collections.sort(filteredList, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));
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

            budgetList = budgetDb.getAllBudgets();
            List<String> categories = new ArrayList<>();
            for (Budget budget : budgetList) {
                categories.add(budget.getCategory());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spCategory.setAdapter(adapter);

            AlertDialog dialog = builder.create();

            btnSave.setOnClickListener(v -> {
                String description = etDescription.getText().toString().trim();
                String amountStr = etAmount.getText().toString().trim();
                String date = etDate.getText().toString().trim();

                // Kiểm tra nếu không có mục nào được chọn
                String category = (spCategory.getSelectedItem() != null) ? spCategory.getSelectedItem().toString() : null;

                if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty() || category == null) {
                    Toast.makeText(getActivity(), "Hiện Tại Bạn Không Có Ngân Sách Để Chi Tiêu", Toast.LENGTH_SHORT).show();
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

                // Kiểm tra xem có ngân sách cho danh mục này không
                boolean budgetFound = false;
                double totalSpent = 0; // Tính lại tổng số tiền đã chi cho danh mục này
                for (Budget budget : budgetList) {
                    if (budget.getCategory().equals(category)) {
                        budgetFound = true;
                        // Tính tổng chi tiêu đã chi cho danh mục này
                        totalSpent = budgetDb.getTotalSpentForCategory(category); // Hàm này sẽ tính tổng chi tiêu cho danh mục
                        double updatedSpent = totalSpent + amount;
                        if (updatedSpent > budget.getAmount()) {
                            Toast.makeText(getActivity(), "Ngân sách cho danh mục này không đủ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;
                    }
                }

                if (!budgetFound) {
                    Toast.makeText(getActivity(), "Không tìm thấy ngân sách cho danh mục này", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    long insertedId = budgetDb.addExpense(amount, category, date, description);
                    if (insertedId != -1) {
                        // ✅ Cập nhật ngân sách sau khi thêm chi tiêu thành công
                        budgetDb.updateBudgetSpent(category, totalSpent + amount); // Cập nhật lại số tiền đã chi

                        Expense newExpense = new Expense(description, category, amount, date);
                        newExpense.setId((int) insertedId);
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

            List<String> categories = new ArrayList<>();
            for (Budget budget : budgetList) {
                categories.add(budget.getCategory());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spCategory.setAdapter(adapter);

            int categoryPosition = adapter.getPosition(expense.getCategory());
            if (categoryPosition >= 0) {
                spCategory.setSelection(categoryPosition);
            }

            AlertDialog dialog = builder.create();

            btnSave.setOnClickListener(v -> {
                String newDescription = etDescription.getText().toString().trim();
                String newAmountStr = etAmount.getText().toString().trim();
                String newDate = etDate.getText().toString().trim();
                String newCategory = spCategory.getSelectedItem().toString();

                if (newDescription.isEmpty() || newAmountStr.isEmpty() || newDate.isEmpty()) {
                    Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                double newAmount;
                try {
                    newAmount = Double.parseDouble(newAmountStr);
                    if (newAmount <= 0) {
                        Toast.makeText(getActivity(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate budget
                if (!expense.getCategory().equals(newCategory) || expense.getAmount() != newAmount) {
                    // Reduce old budget amount
                    for (Budget oldBudget : budgetList) {
                        if (oldBudget.getCategory().equals(expense.getCategory())) {
                            oldBudget.setSpent(oldBudget.getSpent() - expense.getAmount());
                            if (oldBudget.getSpent() < 0) oldBudget.setSpent(0);
                            budgetDb.updateBudget(oldBudget);
                            break;
                        }
                    }

                    // Add new amount to the selected category
                    for (Budget newBudget : budgetList) {
                        if (newBudget.getCategory().equals(newCategory)) {
                            double tempSpent = newBudget.getSpent() + newAmount;
                            if (tempSpent > newBudget.getAmount()) {
                                Toast.makeText(getActivity(), "Số tiền chi vượt quá ngân sách cho danh mục này", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            newBudget.setSpent(tempSpent);
                            budgetDb.updateBudget(newBudget);
                            break;
                        }
                    }

                    // Update the budget list
                    budgetList = budgetDb.getAllBudgets();
                }

                // Update expense data
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
            if (expenseToDelete == null || expenseToDelete.getId() == 0) {
                Log.e("EXPENSE_DELETE", "Expense object is invalid.");
                Toast.makeText(getActivity(), "Khoản chi tiêu không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(getActivity())
                    .setTitle("Xóa khoản chi tiêu")
                    .setMessage("Bạn có chắc chắn muốn xóa khoản chi tiêu: " + expenseToDelete.getDescription() + "?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean isDeleted = budgetDb.deleteExpense(expenseToDelete);
                        if (isDeleted) {
                            Toast.makeText(getActivity(), "Đã xóa khoản chi tiêu", Toast.LENGTH_SHORT).show();
                            expenseList.remove(expenseToDelete);
                            filterExpensesByDate(etSearchDate.getText().toString());
                        } else {
                            Toast.makeText(getActivity(), "Không thể xóa khoản chi tiêu", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }

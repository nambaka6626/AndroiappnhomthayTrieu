package com.example.campusexpensemanagerse06304.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.R;
import com.example.campusexpensemanagerse06304.database.BudgetDb;
import com.example.campusexpensemanagerse06304.model.Budget;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private List<Budget> budgetList;
    private Context context;
    private BudgetDb budgetDb;
    private Runnable updateChartCallback;

    public BudgetAdapter(Context context, List<Budget> budgetList, BudgetDb budgetDb, Runnable updateChartCallback) {
        this.context = context;
        this.budgetList = budgetList;
        this.budgetDb = budgetDb;
        this.updateChartCallback = updateChartCallback;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);

        // Hiển thị tên danh mục ngân sách và số tiền đã chi
        holder.tvCategory.setText(budget.getCategory());
        holder.tvAmount.setText(String.format("%,.0f VNĐ", budget.getAmount()));
        holder.tvSpent.setText(String.format("Đã chi: %,.0f VNĐ", budget.getSpent()));

        // Tính số tiền còn lại
        double remaining = budget.getAmount() - budget.getSpent();
        holder.tvRemaining.setText(String.format("Số tiền còn lại: %,.0f VNĐ", remaining));

        // Đổi màu sắc của số tiền ngân sách khi đã chi trên 80% ngân sách
        if (budget.getSpent() > budget.getAmount() * 0.8) {
            holder.tvAmount.setTextColor(Color.RED);
        } else {
            holder.tvAmount.setTextColor(Color.BLACK);
        }

        // Xử lý sự kiện khi nhấn vào item ngân sách để chỉnh sửa
        holder.itemView.setOnClickListener(v -> showEditBudgetDialog(position));

        // Xử lý sự kiện xóa ngân sách
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa ngân sách")
                    .setMessage("Bạn có chắc chắn muốn xóa danh mục này? Nếu đã có chi tiêu, không thể xóa ngân sách.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Kiểm tra nếu ngân sách đã có chi tiêu
                        if (budget.getSpent() > 0) {
                            Toast.makeText(context, "Không thể xóa ngân sách có chi tiêu đã được ghi nhận", Toast.LENGTH_SHORT).show();
                        } else {
                            // Nếu chưa có chi tiêu, thực hiện xóa ngân sách
                            if (budgetDb.deleteBudget(budget.getId())) {
                                budgetList.remove(position);
                                notifyDataSetChanged();
                                updateChartCallback.run();
                                Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void showEditBudgetDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chỉnh sửa ngân sách");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_budget, null);
        EditText edtCategory = view.findViewById(R.id.edtCategory);
        EditText edtAmount = view.findViewById(R.id.edtAmount);
        EditText edtSpent = view.findViewById(R.id.edtSpent);

        // Sửa: chỉ cho phép sửa ngân sách
        edtAmount.setEnabled(true);
        edtSpent.setEnabled(false);

        Budget budget = budgetList.get(position);
        edtCategory.setText(budget.getCategory());
        edtAmount.setText(String.valueOf(budget.getAmount()));
        edtSpent.setText(String.valueOf(budget.getSpent()));

        builder.setView(view);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String category = edtCategory.getText().toString();
            String amountStr = edtAmount.getText().toString();

            if (!category.isEmpty() && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    double spent = budget.getSpent(); // Không thay đổi spent, giữ nguyên giá trị cũ

                    if (spent > amount) {
                        Toast.makeText(context, "Số tiền đã chi không thể lớn hơn ngân sách", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Budget updatedBudget = new Budget(category, amount, spent);
                    updatedBudget.setId(budget.getId());

                    if (budgetDb.updateBudget(updatedBudget) > 0) {
                        budgetList.set(position, updatedBudget);
                        notifyDataSetChanged();
                        updateChartCallback.run();
                        Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }


    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvSpent, tvRemaining;  // Thêm tvRemaining ở đây
        Button btnDelete;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            tvRemaining = itemView.findViewById(R.id.tvRemaining);  // Khai báo tvRemaining ở đây
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

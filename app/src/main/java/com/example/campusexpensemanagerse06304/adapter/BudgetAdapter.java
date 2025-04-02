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
import com.example.campusexpensemanagerse06304.model.Budget;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private List<Budget> budgetList;
    private Context context;

    public BudgetAdapter(Context context, List<Budget> budgetList) {
        this.context = context;
        this.budgetList = budgetList;
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
        holder.tvCategory.setText(budget.getCategory());
        holder.tvAmount.setText(String.format("%,.0f VNĐ", budget.getAmount()));

        // Đổi màu chữ nếu gần vượt ngân sách
        if (budget.getSpent() > budget.getAmount() * 0.8) {
            holder.tvAmount.setTextColor(Color.RED);
        } else {
            holder.tvAmount.setTextColor(Color.BLACK);
        }

        // Xử lý khi nhấn vào danh mục để chỉnh sửa
        holder.itemView.setOnClickListener(v -> showEditBudgetDialog(position));

        // Xử lý nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa ngân sách")
                    .setMessage("Bạn có chắc chắn muốn xóa danh mục này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        budgetList.remove(position);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    // Hiển thị dialog chỉnh sửa ngân sách
    private void showEditBudgetDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chỉnh sửa ngân sách");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_budget, null);
        EditText edtCategory = view.findViewById(R.id.edtCategory);
        EditText edtAmount = view.findViewById(R.id.edtAmount);

        Budget budget = budgetList.get(position);
        edtCategory.setText(budget.getCategory());
        edtAmount.setText(String.valueOf(budget.getAmount()));

        builder.setView(view);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String category = edtCategory.getText().toString();
            String amountStr = edtAmount.getText().toString();

            if (!category.isEmpty() && !amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                budgetList.set(position, new Budget(category, amount, budget.getSpent()));
                notifyDataSetChanged();
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
        TextView tvCategory, tvAmount;
        Button btnDelete;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

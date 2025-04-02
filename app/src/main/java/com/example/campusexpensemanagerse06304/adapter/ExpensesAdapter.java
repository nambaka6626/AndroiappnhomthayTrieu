package com.example.campusexpensemanagerse06304.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Thêm import cho Button
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campusexpensemanagerse06304.R;
import com.example.campusexpensemanagerse06304.model.Expense;
import java.util.List;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder> {
    private List<Expense> expenseList;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onEdit(Expense expense, int position);
        void onDelete(int position);
    }

    public void setOnExpenseClickListener(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    public ExpensesAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.tvDescription.setText(expense.getDescription());
        holder.tvCategory.setText(expense.getCategory());
        holder.tvAmount.setText(String.format("%.0f VNĐ", expense.getAmount()));
        holder.tvDate.setText(expense.getDate());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(expense, position);
            }
        });

        // Giữ nguyên nhấn lâu nếu bạn vẫn muốn dùng
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDelete(position);
            }
            return true;
        });

        // Thêm sự kiện cho nút Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvCategory, tvAmount, tvDate;
        Button btnDelete; // Thêm biến cho nút Xóa

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete); // Liên kết với nút Xóa
        }
    }
}
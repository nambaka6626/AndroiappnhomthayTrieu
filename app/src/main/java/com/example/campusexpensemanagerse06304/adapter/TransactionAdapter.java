package com.example.campusexpensemanagerse06304.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campusexpensemanagerse06304.R;
import com.example.campusexpensemanagerse06304.model.Transaction;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvAmount.setText(transaction.getAmount() + " VND");
        holder.tvDate.setText(transaction.getDate());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}

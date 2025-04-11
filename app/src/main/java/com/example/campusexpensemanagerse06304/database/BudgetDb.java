package com.example.campusexpensemanagerse06304.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.campusexpensemanagerse06304.model.Budget;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.util.ArrayList;
import java.util.List;

public class BudgetDb {
    private final SQLiteDatabase dbRead;
    private final SQLiteDatabase dbWrite;

    public BudgetDb(Context context) {
        DatabaseContext dbHelper = new DatabaseContext(context);
        dbRead = dbHelper.getReadableDatabase();
        dbWrite = dbHelper.getWritableDatabase();
    }

    public long insertBudget(String category, double amount, double spent) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseContext.CATEGORY_COL, category);
            values.put(DatabaseContext.AMOUNT_COL, amount);
            values.put(DatabaseContext.SPENT_COL, spent);
            return dbWrite.insert(DatabaseContext.TABLE_BUDGETS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<Budget> getAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
        try {
            Cursor cursor = dbRead.rawQuery("SELECT * FROM " + DatabaseContext.TABLE_BUDGETS, null);
            if (cursor.moveToFirst()) {
                do {
                    Budget budget = new Budget();
                    budget.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContext.BUDGET_ID_COL)));
                    budget.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CATEGORY_COL)));
                    budget.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.AMOUNT_COL)));
                    budget.setSpent(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.SPENT_COL)));
                    budgets.add(budget);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return budgets;
    }

    public int updateBudget(Budget budget) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseContext.CATEGORY_COL, budget.getCategory());
            values.put(DatabaseContext.AMOUNT_COL, budget.getAmount());
            values.put(DatabaseContext.SPENT_COL, budget.getSpent());
            return dbWrite.update(DatabaseContext.TABLE_BUDGETS, values,
                    DatabaseContext.BUDGET_ID_COL + " = ?",
                    new String[]{String.valueOf(budget.getId())});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean deleteBudget(int id) {
        try {
            int rows = dbWrite.delete(DatabaseContext.TABLE_BUDGETS,
                    DatabaseContext.BUDGET_ID_COL + " = ?",
                    new String[]{String.valueOf(id)});
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        if (dbRead != null && dbRead.isOpen()) dbRead.close();
        if (dbWrite != null && dbWrite.isOpen()) dbWrite.close();
    }

    public boolean addExpense(double amount, String category, String date, String description) {
        try {
            // 1. Lấy ngân sách hiện tại theo danh mục
            Cursor cursor = dbRead.rawQuery("SELECT " + DatabaseContext.AMOUNT_COL + ", " + DatabaseContext.SPENT_COL +
                            " FROM " + DatabaseContext.TABLE_BUDGETS +
                            " WHERE " + DatabaseContext.CATEGORY_COL + " = ?",
                    new String[]{category});

            if (cursor.moveToFirst()) {
                double amountBudgeted = cursor.getDouble(0); // tổng ngân sách hiện tại
                double spent = cursor.getDouble(1);          // đã chi
                cursor.close();

                double remaining = amountBudgeted - spent;

                // 2. Kiểm tra còn đủ ngân sách không
                if (amount > remaining) {
                    Log.e("BUDGET_DB", "Không đủ ngân sách. Đang cố chi " + amount + " nhưng chỉ còn " + remaining);
                    return false;
                }

                // 3. Thêm vào bảng EXPENSES
                ContentValues values = new ContentValues();
                values.put(DatabaseContext.EXPENSE_AMOUNT_COL, amount);
                values.put(DatabaseContext.EXPENSE_CATEGORY_COL, category);
                values.put(DatabaseContext.EXPENSE_DATE_COL, date);
                values.put(DatabaseContext.EXPENSE_DESC_COL, description);

                long result = dbWrite.insert(DatabaseContext.TABLE_EXPENSES, null, values);
                if (result == -1) {
                    Log.e("BUDGET_DB", "Insert thất bại với values: " + values.toString());
                    return false;
                }

                // 4. Trừ vào ngân sách: giảm 'amount', tăng 'spent'
                double newAmount = amountBudgeted - amount;
                double newSpent = spent + amount;

                ContentValues budgetUpdate = new ContentValues();
                budgetUpdate.put(DatabaseContext.AMOUNT_COL, newAmount);
                budgetUpdate.put(DatabaseContext.SPENT_COL, newSpent);

                dbWrite.update(DatabaseContext.TABLE_BUDGETS, budgetUpdate,
                        DatabaseContext.CATEGORY_COL + " = ?", new String[]{category});

                return true;
            } else {
                cursor.close();
                Log.e("BUDGET_DB", "Không tìm thấy ngân sách cho category: " + category);
                return false;
            }

        } catch (Exception e) {
            Log.e("BUDGET_DB", "Lỗi khi thêm chi tiêu: " + e.getMessage());
            return false;
        }
    }

    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        Cursor cursor = dbRead.rawQuery("SELECT * FROM " + DatabaseContext.TABLE_EXPENSES, null);

        if (cursor.moveToFirst()) {
            do {
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.EXPENSE_DESC_COL));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.EXPENSE_CATEGORY_COL));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.EXPENSE_AMOUNT_COL));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.EXPENSE_DATE_COL));

                expenses.add(new Expense(description, category, amount, date));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return expenses;
    }

    public boolean deleteExpense(Expense expense) {
        int rows = dbWrite.delete("expenses", "description=? AND category=? AND amount=? AND date=?",
                new String[] {
                        expense.getDescription(),
                        expense.getCategory(),
                        String.valueOf(expense.getAmount()),
                        expense.getDate()
                });
        return rows > 0;
    }
    public double getTotalSpentForCategory(String category) {
        double total = 0;
        try {
            Cursor cursor = dbRead.rawQuery(
                    "SELECT SUM(amount) FROM expenses WHERE category = ?",
                    new String[]{category}
            );
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }









}



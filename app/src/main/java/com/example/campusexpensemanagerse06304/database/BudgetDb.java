package com.example.campusexpensemanagerse06304.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.campusexpensemanagerse06304.model.Budget;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.util.ArrayList;
import java.util.List;

public class BudgetDb {
    private final SQLiteDatabase dbRead;
    private final SQLiteDatabase dbWrite;
    private ExpenseUpdateListener expenseUpdateListener;

    public BudgetDb(Context context) {
        DatabaseContext dbHelper = new DatabaseContext(context);
        dbRead = dbHelper.getReadableDatabase();
        dbWrite = dbHelper.getWritableDatabase();
    }

    public interface ExpenseUpdateListener {
        void onExpenseUpdated();
    }

    public void setExpenseUpdateListener(ExpenseUpdateListener listener) {
        this.expenseUpdateListener = listener;
    }

    public long insertBudget(String category, double amount, double spent) {
        // Kiểm tra số tiền nếu bằng 0 thì không cho phép tạo ngân sách
        if (amount <= 0) {
            Log.e("BUDGET_DB", "Số tiền ngân sách phải lớn hơn 0.");
            return -1; // Không cho phép tạo ngân sách với số tiền bằng 0
        }

        // Đảm bảo số tiền đã chi (spent) mặc định là 0 nếu không có chi tiêu
        spent = 0;  // Giá trị mặc định của spent khi tạo ngân sách là 0

        // Tạo ContentValues để chèn ngân sách vào cơ sở dữ liệu
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.CATEGORY_COL, category);
        values.put(DatabaseContext.AMOUNT_COL, amount);
        values.put(DatabaseContext.SPENT_COL, spent);  // Đảm bảo spent là 0

        // Chèn ngân sách vào bảng ngân sách và trả về ID của ngân sách đã tạo
        return dbWrite.insert(DatabaseContext.TABLE_BUDGETS, null, values);
    }


    public List<Budget> getAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
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
        return budgets;
    }

    public int updateBudget(Budget budget) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.CATEGORY_COL, budget.getCategory());
        values.put(DatabaseContext.AMOUNT_COL, budget.getAmount());
        values.put(DatabaseContext.SPENT_COL, budget.getSpent());

        return dbWrite.update(DatabaseContext.TABLE_BUDGETS, values,
                DatabaseContext.BUDGET_ID_COL + " = ?",
                new String[]{String.valueOf(budget.getId())});
    }

    public boolean deleteBudget(int id) {
        Cursor cursor = dbRead.rawQuery("SELECT " + DatabaseContext.CATEGORY_COL + " FROM " + DatabaseContext.TABLE_BUDGETS +
                " WHERE " + DatabaseContext.BUDGET_ID_COL + " = ?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            String category = cursor.getString(0);

            Cursor expenseCursor = dbRead.rawQuery("SELECT COUNT(*) FROM " + DatabaseContext.TABLE_EXPENSES +
                    " WHERE " + DatabaseContext.EXPENSE_CATEGORY_COL + " = ?", new String[]{category});

            if (expenseCursor.moveToFirst() && expenseCursor.getInt(0) > 0) {
                expenseCursor.close();
                cursor.close();
                return false;
            }
            expenseCursor.close();
        } else {
            cursor.close();
            return false;
        }
        cursor.close();

        int rows = dbWrite.delete(DatabaseContext.TABLE_BUDGETS,
                DatabaseContext.BUDGET_ID_COL + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public long addExpense(double amount, String category, String date, String description) {
        long insertedId = -1;
        Cursor cursor = null;
        boolean transactionStarted = false;

        try {
            cursor = dbRead.rawQuery("SELECT " + DatabaseContext.AMOUNT_COL + ", " + DatabaseContext.SPENT_COL +
                    " FROM " + DatabaseContext.TABLE_BUDGETS +
                    " WHERE " + DatabaseContext.CATEGORY_COL + " = ?", new String[]{category});

            if (cursor.moveToFirst()) {
                double amountBudgeted = cursor.getDouble(0);
                double spent = cursor.getDouble(1);
                double remaining = amountBudgeted - spent;

                if (amount > remaining) return -1; // Nếu số tiền chi vượt quá ngân sách còn lại thì không cho phép

                dbWrite.beginTransaction();
                transactionStarted = true;

                // Lưu chi tiêu vào bảng expenses
                ContentValues values = new ContentValues();
                values.put(DatabaseContext.EXPENSE_AMOUNT_COL, amount);
                values.put(DatabaseContext.EXPENSE_CATEGORY_COL, category);
                values.put(DatabaseContext.EXPENSE_DATE_COL, date);
                values.put(DatabaseContext.EXPENSE_DESC_COL, description);

                insertedId = dbWrite.insert(DatabaseContext.TABLE_EXPENSES, null, values);
                if (insertedId == -1) return -1;

                // Cập nhật số tiền đã chi cho danh mục ngân sách
                double newSpent = spent + amount;
                ContentValues update = new ContentValues();
                update.put(DatabaseContext.SPENT_COL, newSpent);

                int rowsUpdated = dbWrite.update(DatabaseContext.TABLE_BUDGETS, update,
                        DatabaseContext.CATEGORY_COL + " = ?", new String[]{category});
                if (rowsUpdated == 0) return -1;

                dbWrite.setTransactionSuccessful();

                if (expenseUpdateListener != null) expenseUpdateListener.onExpenseUpdated();
            }

        } catch (Exception e) {
            Log.e("BUDGET_DB", "Error adding expense: " + e.getMessage());
            insertedId = -1;
        } finally {
            if (transactionStarted) dbWrite.endTransaction();
            if (cursor != null) cursor.close();
        }

        return insertedId;
    }



    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        Cursor cursor = dbRead.rawQuery("SELECT * FROM " + DatabaseContext.TABLE_EXPENSES, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContext.EXPENSE_ID_COL));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.EXPENSE_DESC_COL));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.EXPENSE_CATEGORY_COL));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.EXPENSE_AMOUNT_COL));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.EXPENSE_DATE_COL));

                expenses.add(new Expense(id, description, category, amount, date));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return expenses;
    }

    public boolean deleteExpense(Expense expense) {
        if (expense == null || expense.getId() == 0) return false;

        Cursor cursor = null;
        boolean transactionStarted = false;

        try {
            dbWrite.beginTransaction();
            transactionStarted = true;

            cursor = dbRead.rawQuery("SELECT " + DatabaseContext.SPENT_COL + " FROM " + DatabaseContext.TABLE_BUDGETS +
                    " WHERE " + DatabaseContext.CATEGORY_COL + " = ?", new String[]{expense.getCategory()});

            if (cursor.moveToFirst()) {
                double currentSpent = cursor.getDouble(0);
                double newSpent = currentSpent - expense.getAmount();

                ContentValues update = new ContentValues();
                update.put(DatabaseContext.SPENT_COL, newSpent);

                int rows = dbWrite.update(DatabaseContext.TABLE_BUDGETS, update,
                        DatabaseContext.CATEGORY_COL + " = ?", new String[]{expense.getCategory()});
                if (rows == 0) return false;

                int deleted = dbWrite.delete(DatabaseContext.TABLE_EXPENSES,
                        DatabaseContext.EXPENSE_ID_COL + " = ?", new String[]{String.valueOf(expense.getId())});
                if (deleted == 0) return false;

                dbWrite.setTransactionSuccessful();

                if (expenseUpdateListener != null) expenseUpdateListener.onExpenseUpdated();

                return true;
            }

            return false;

        } catch (Exception e) {
            Log.e("BUDGET_DB", "Error deleting expense: " + e.getMessage());
            return false;
        } finally {
            if (transactionStarted) dbWrite.endTransaction();
            if (cursor != null) cursor.close();
        }
    }

    public double getTotalSpentForCategory(String category) {
        double totalSpent = 0;
        String query = "SELECT SUM(" + DatabaseContext.EXPENSE_AMOUNT_COL + ") FROM " + DatabaseContext.TABLE_EXPENSES +
                " WHERE " + DatabaseContext.EXPENSE_CATEGORY_COL + " = ?";
        Cursor cursor = dbRead.rawQuery(query, new String[]{category});

        if (cursor.moveToFirst()) {
            totalSpent = cursor.getDouble(0);
        }
        cursor.close();
        return totalSpent;
    }

    public void close() {
        if (dbRead != null && dbRead.isOpen()) dbRead.close();
        if (dbWrite != null && dbWrite.isOpen()) dbWrite.close();
    }

    public void updateBudgetSpent(String category, double amount) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.SPENT_COL, amount);
        dbWrite.update(DatabaseContext.TABLE_BUDGETS, values, DatabaseContext.CATEGORY_COL + " = ?", new String[]{category});
    }





}

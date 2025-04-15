package com.example.campusexpensemanagerse06304.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseContext extends SQLiteOpenHelper {
    private static final String DB_NAME = "campus_expenses";
    private static final int DB_VERSION = 3;  // Tăng version lên 3 để có thể áp dụng thay đổi

    // create cols for table users
    public static final String TABLE_NAME = "users";
    public static final String ID_COL = "id";
    public static final String USERNAME_COL = "username";
    public static final String PASSWORD_COL = "password";
    public static final String EMAIL_COL = "email";
    public static final String PHONE_COL = "phone";
    public static final String ROLE_ID_COL = "role_id";

    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String DELETED_AT = "deleted_at";

    // dinh nghia cho bang budgets
    public static final String TABLE_BUDGETS = "budgets";
    public static final String BUDGET_ID_COL = "id";
    public static final String CATEGORY_COL = "category";
    public static final String AMOUNT_COL = "amount";
    public static final String SPENT_COL = "spent";
    public static final String REMAINING_COL = "remaining";  // Khai báo cột remaining

    // định nghĩa cho bảng expenses
    public static final String TABLE_EXPENSES = "expenses";
    public static final String EXPENSE_ID_COL = "id";
    public static final String EXPENSE_AMOUNT_COL = "amount";
    public static final String EXPENSE_CATEGORY_COL = "category";
    public static final String EXPENSE_DATE_COL = "date";
    public static final String EXPENSE_DESC_COL = "description";

    public DatabaseContext(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng users
        String query = "CREATE TABLE " + TABLE_NAME + " ( "
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERNAME_COL + " VARCHAR(60) NOT NULL, "
                + PASSWORD_COL + " VARCHAR(200) NOT NULL, "
                + EMAIL_COL + " VARCHAR(60) NOT NULL, "
                + PHONE_COL + " VARCHAR(30), "
                + ROLE_ID_COL + " INTEGER, "
                + CREATED_AT + " DATETIME, "
                + UPDATED_AT + " DATETIME, "
                + DELETED_AT + " DATETIME ) ";
        db.execSQL(query);

        // Tạo bảng budgets với cột "remaining"
        String budgetTableQuery = "CREATE TABLE " + TABLE_BUDGETS + " ("
                + BUDGET_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CATEGORY_COL + " TEXT NOT NULL, "
                + AMOUNT_COL + " REAL NOT NULL, "
                + SPENT_COL + " REAL NOT NULL, "
                + REMAINING_COL + " REAL NOT NULL DEFAULT 0)";  // Thêm cột remaining
        db.execSQL(budgetTableQuery);

        // Tạo bảng expenses
        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                EXPENSE_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXPENSE_AMOUNT_COL + " REAL, " +
                EXPENSE_CATEGORY_COL + " TEXT, " +
                EXPENSE_DATE_COL + " TEXT, " +
                EXPENSE_DESC_COL + " TEXT)";
        db.execSQL(createExpensesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nếu là phiên bản cũ hơn (DB_VERSION < 3), thêm cột "remaining" vào bảng "budgets"
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_BUDGETS + " ADD COLUMN " + REMAINING_COL + " REAL NOT NULL DEFAULT 0");
        }

        // Xóa bảng nếu cần thiết và tạo lại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);

        onCreate(db);  // Tạo lại các bảng
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}

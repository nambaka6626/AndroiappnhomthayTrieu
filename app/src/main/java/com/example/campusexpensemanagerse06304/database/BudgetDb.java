package com.example.campusexpensemanagerse06304.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

public class BudgetDb {
    private final SQLiteDatabase dbRead, dbWrite;
    public BudgetDb(@Nullable Context context){
        DatabaseContext helper = new DatabaseContext(context);
        dbRead = helper.getReadableDatabase();
        dbWrite = helper.getWritableDatabase();
    }
    // truy van lam viec voi bang budgets
}

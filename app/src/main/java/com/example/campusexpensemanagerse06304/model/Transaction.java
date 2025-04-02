package com.example.campusexpensemanagerse06304.model;

public class Transaction {
    private String category;
    private int amount;
    private String date;

    public Transaction(String category, int amount, String date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public int getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}

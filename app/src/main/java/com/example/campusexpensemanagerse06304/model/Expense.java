package com.example.campusexpensemanagerse06304.model;

public class Expense {
    private String description;
    private String category;
    private double amount;
    private String date;

    public Expense(String description, String category, double amount, String date) {
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

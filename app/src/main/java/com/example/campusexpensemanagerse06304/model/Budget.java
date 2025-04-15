package com.example.campusexpensemanagerse06304.model;

public class Budget {
    private int id;
    private String category;
    private double amount;
    private double spent;

    public Budget() {}

    public Budget(String category, double amount, double spent) {
        this.category = category;
        this.amount = amount;
        this.spent = spent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }
}
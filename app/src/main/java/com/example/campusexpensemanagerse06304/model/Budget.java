package com.example.campusexpensemanagerse06304.model;

public class Budget {
    private String category;
    private double amount;
    private double spent;

    public Budget(String category, double amount, double spent) {
        this.category = category;
        this.amount = amount;
        this.spent = spent;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public double getSpent() {
        return spent;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

}

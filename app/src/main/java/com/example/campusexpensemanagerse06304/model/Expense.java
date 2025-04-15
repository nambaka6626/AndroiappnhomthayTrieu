package com.example.campusexpensemanagerse06304.model;

public class Expense {
    private int id;
    private String description;
    private String category;
    private double amount;
    private String date;

    // Constructor 4 tham số (dùng khi thêm mới)
    public Expense(String description, String category, double amount, String date) {
        this.id = 0;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    // Constructor 5 tham số (dùng khi đọc từ DB)
    public Expense(int id, String description, String category, double amount, String date) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }




    // Getter và setter cho id
    public int getId() {  // Phương thức getId() để lấy id
        return id;
    }

    public void setId(int id) {  // Phương thức setId() để gán id
        this.id = id;
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

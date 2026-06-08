package com.expensetracker.model;

import jakarta.persistence.*;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "budgets")
public class Budget {

    @ManyToOne
@JoinColumn(name = "user_id")
private User user;

public User getUser() {
    return user;
}

public void setUser(User user) {
    this.user = user;
}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int month;
    private int year;
    private double amount;

    public Budget() {
    }

    public int getId() {
        return id;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public double getAmount() {
        return amount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
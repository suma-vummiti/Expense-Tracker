package com.expensetracker.repository;

import com.expensetracker.model.Budget;
import com.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    Budget findByUserAndMonthAndYear(User user, int month, int year);
}
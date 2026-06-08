package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    List<Expense> findByCategoryContainingIgnoreCase(String category);

    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    List<Expense> findByUser(User user);

    List<Expense> findByUserAndCategoryContainingIgnoreCase(User user, String category);

    List<Expense> findByUserAndExpenseDateBetween(User user, LocalDate startDate, LocalDate endDate);

    @Query(value = """
            SELECT *
            FROM expenses
            WHERE user_id = :userId
            AND MONTH(expense_date) = :month
            AND YEAR(expense_date) = :year
            """, nativeQuery = true)
    List<Expense> findByUserAndMonthAndYear(
            @Param("userId") int userId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query(value = """
SELECT
DATE_FORMAT(expense_date, '%M %Y') AS monthYear,
SUM(amount) AS total
FROM expenses
WHERE user_id = :userId
GROUP BY DATE_FORMAT(expense_date, '%M %Y')
ORDER BY MIN(expense_date)
""", nativeQuery = true)
List<Object[]> getMonthlyReportByUser(
        @Param("userId") int userId
);

    @Query("""
        SELECT e.category, SUM(e.amount)
        FROM Expense e
        GROUP BY e.category
        ORDER BY SUM(e.amount) DESC
        """)
List<Object[]> getCategoryWiseExpenses();
}
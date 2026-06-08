package com.expensetracker.controller;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public HomeController(ExpenseRepository expenseRepository,
                          BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) Integer month,
                       @RequestParam(required = false) Integer year,
                       Model model,
                       HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("loggedInUser", user);

        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        if (year == null) {
            year = LocalDate.now().getYear();
        }

        // This list is only for selected dashboard month
        List<Expense> monthExpenses =
                expenseRepository.findByUserAndMonthAndYear(
                        user.getId(),
                        month,
                        year
                );

        // This list is only for Recent Expenses table
        List<Expense> displayedExpenses = monthExpenses;

        if (keyword != null && !keyword.isEmpty()) {
            displayedExpenses =
                    expenseRepository.findByUserAndCategoryContainingIgnoreCase(user, keyword);
        }

        if (startDate != null && !startDate.isEmpty()
                && endDate != null && !endDate.isEmpty()) {

            LocalDate from = LocalDate.parse(startDate);
            LocalDate to = LocalDate.parse(endDate);

            displayedExpenses =
                    expenseRepository.findByUserAndExpenseDateBetween(user, from, to);
        }

        // Dashboard calculations should use selected month expenses only
        double totalExpenses = monthExpenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        long totalTransactions = monthExpenses.size();

        double highestExpense = monthExpenses.stream()
                .mapToDouble(Expense::getAmount)
                .max()
                .orElse(0);

        Budget budgetObj =
                budgetRepository.findByUserAndMonthAndYear(user, month, year);

        double budget = 0;

        if (budgetObj != null) {
            budget = budgetObj.getAmount();
        }

        double remaining = budget - totalExpenses;

        double budgetPercentage = 0;

        if (budget > 0) {
            budgetPercentage = (totalExpenses / budget) * 100;

            if (budgetPercentage > 100) {
                budgetPercentage = 100;
            }
        }

        String budgetStatus;

        if (remaining < 0) {
            budgetStatus = "Budget exceeded by ₹" + Math.abs(remaining);
        } else {
            budgetStatus = "You are within budget. Remaining ₹" + remaining;
        }

        // Top category and pie chart should also use selected month expenses only
        Map<String, Double> categoryMap = monthExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        List<Object[]> categoryData = categoryMap.entrySet()
                .stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .toList();

        String topCategory = "No Data";

        if (!categoryData.isEmpty()) {
            topCategory = categoryData.get(0)[0].toString();
        }

        model.addAttribute("expenses", displayedExpenses);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("budget", budget);
        model.addAttribute("remaining", remaining);
        model.addAttribute("budgetPercentage", Math.round(budgetPercentage));
        model.addAttribute("budgetStatus", budgetStatus);
        model.addAttribute("isBudgetExceeded", remaining < 0);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("highestExpense", highestExpense);
        model.addAttribute("topCategory", topCategory);
        model.addAttribute("categoryData", categoryData);
        model.addAttribute("monthlyReport",
                expenseRepository.getMonthlyReportByUser(user.getId()));

        return "index";
    }
}
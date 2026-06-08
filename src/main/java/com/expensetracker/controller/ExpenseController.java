package com.expensetracker.controller;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;



    @GetMapping("/export-pdf")
public void exportPDF(HttpServletResponse response,
                      HttpSession session) throws Exception {

    User user = (User) session.getAttribute("loggedInUser");

    if (user == null) {
        response.sendRedirect("/login");
        return;
    }

    List<Expense> expenses = expenseRepository.findByUser(user);

    double total = expenses.stream()
            .mapToDouble(Expense::getAmount)
            .sum();

    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=expense-report.pdf");

    Document document = new Document();
    PdfWriter.getInstance(document, response.getOutputStream());

    document.open();

    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

    Paragraph title = new Paragraph("Expense Tracker Report", titleFont);
    title.setAlignment(Element.ALIGN_CENTER);
    document.add(title);

    document.add(new Paragraph(" "));
    document.add(new Paragraph("User: " + user.getName(), normalFont));
    document.add(new Paragraph("Total Expense: Rs. " + total, normalFont));
    document.add(new Paragraph(" "));

    PdfPTable table = new PdfPTable(5);
    table.setWidthPercentage(100);

    table.addCell("ID");
    table.addCell("Title");
    table.addCell("Amount");
    table.addCell("Category");
    table.addCell("Date");

    for (Expense expense : expenses) {
        table.addCell(String.valueOf(expense.getId()));
        table.addCell(expense.getTitle());
        table.addCell("Rs. " + expense.getAmount());
        table.addCell(expense.getCategory());
        table.addCell(String.valueOf(expense.getExpenseDate()));
    }

    document.add(table);
    document.close();
}
    public ExpenseController(ExpenseRepository expenseRepository,
                             BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    @PostMapping("/add-expense")
    public String addExpense(Expense expense, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        expense.setUser(user);
        expenseRepository.save(expense);

        return "redirect:/";
    }

    @GetMapping("/edit-expense/{id}")
    public String editExpense(@PathVariable int id,
                              Model model,
                              HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Expense expense = expenseRepository.findById(id).orElse(null);

        if (expense == null || expense.getUser() == null ||
                expense.getUser().getId() != user.getId()) {
            return "redirect:/";
        }

        model.addAttribute("expense", expense);

        return "edit-expense";
    }

    @PostMapping("/update-expense")
    public String updateExpense(Expense expense, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        expense.setUser(user);
        expenseRepository.save(expense);

        return "redirect:/";
    }

    @PostMapping("/delete-expense/{id}")
    public String deleteExpense(@PathVariable int id,
                                HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Expense expense = expenseRepository.findById(id).orElse(null);

        if (expense != null && expense.getUser() != null &&
                expense.getUser().getId() == user.getId()) {
            expenseRepository.deleteById(id);
        }

        return "redirect:/";
    }

    @PostMapping("/set-budget")
public String setBudget(Budget budget,
                        HttpSession session) {

    User user = (User) session.getAttribute("loggedInUser");

    if (user == null) {
        return "redirect:/login";
    }

    Budget existingBudget =
            budgetRepository.findByUserAndMonthAndYear(
                    user,
                    budget.getMonth(),
                    budget.getYear()
            );

    if (existingBudget != null) {

        existingBudget.setAmount(budget.getAmount());

        budgetRepository.save(existingBudget);

    } else {

        budget.setUser(user);

        budgetRepository.save(budget);
    }

    return "redirect:/";
}
    @GetMapping("/export-csv")
    public void exportCSV(HttpServletResponse response,
                          HttpSession session) throws Exception {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=expense-report.csv"
        );

        List<Expense> expenses = expenseRepository.findByUser(user);

        double total = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        PrintWriter writer = response.getWriter();

        writer.println("Expense Tracker Report");
        writer.println("Generated Date," + LocalDate.now());
        writer.println("User," + user.getName());
        writer.println("Total Expenses," + total);
        writer.println();

        writer.println("ID,Title,Amount,Category,Date");

        for (Expense expense : expenses) {
            writer.println(
                    expense.getId() + "," +
                            expense.getTitle() + "," +
                            expense.getAmount() + "," +
                            expense.getCategory() + "," +
                            expense.getExpenseDate()
            );
        }

        writer.flush();
    }
}
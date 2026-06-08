package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasswordController {

    private final UserRepository userRepository;

    public PasswordController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/change-password")
    public String showChangePassword() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("loggedInUser");

        if (!user.getPassword().equals(currentPassword)) {

            model.addAttribute(
                    "error",
                    "Current password is incorrect"
            );

            return "change-password";
        }

        user.setPassword(newPassword);

        userRepository.save(user);

        model.addAttribute(
                "success",
                "Password changed successfully"
        );

        return "change-password";
    }
}
package com.expensetracker.controller;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(User user, Model model) {

        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute(
                    "error",
                    "Email already registered. Please login."
            );
            return "register";
        }

        userRepository.save(user);

        model.addAttribute(
                "success",
                "Registration successful. Please login."
        );

        return "login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(String email,
                        String password,
                        HttpSession session,
                        Model model) {

        User user =
                userRepository.findByEmailAndPassword(email, password);

        if (user == null) {

            model.addAttribute(
                    "error",
                    "Invalid email or password."
            );

            return "login";
        }

        session.setAttribute("loggedInUser", user);

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
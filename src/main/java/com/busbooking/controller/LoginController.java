package com.busbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm() {
        System.out.println("Rendering login page"); // For debugging
        return "login"; // Maps to src/main/resources/templates/login.html
    }
}
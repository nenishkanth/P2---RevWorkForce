package com.revworkforce.revworkforce.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginViewController {

    @GetMapping("/")
    public String landingPage() {
        return "landing";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
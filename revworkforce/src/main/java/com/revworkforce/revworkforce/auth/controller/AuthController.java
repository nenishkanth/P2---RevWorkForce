package com.revworkforce.revworkforce.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.service.EmployeeService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam String email, Model model){

        Employee emp = employeeService.getEmployeeByEmail(email);

        model.addAttribute("email", email);
        model.addAttribute("q1", emp.getSecurityQuestion1());
        model.addAttribute("q2", emp.getSecurityQuestion2());
        model.addAttribute("q3", emp.getSecurityQuestion3());

        return "auth/security-questions";
    }
    
    @PostMapping("/verify-answers")
    public String verifyAnswers(@RequestParam String email,
                                @RequestParam String a1,
                                @RequestParam String a2,
                                @RequestParam String a3,
                                Model model){

        Employee emp = employeeService.getEmployeeByEmail(email);

        boolean match1 = passwordEncoder.matches(a1, emp.getSecurityAnswer1());
        boolean match2 = passwordEncoder.matches(a2, emp.getSecurityAnswer2());
        boolean match3 = passwordEncoder.matches(a3, emp.getSecurityAnswer3());

        if(match1 && match2 && match3){

            model.addAttribute("email", email);

            return "auth/reset-password";
        }

        throw new RuntimeException("Incorrect security answers");
    }
    
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String password){

        Employee emp = employeeService.getEmployeeByEmail(email);

        emp.setPassword(passwordEncoder.encode(password));

        employeeService.updateEmployee(emp);

        return "redirect:/login?passwordReset";
    }
    
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

}



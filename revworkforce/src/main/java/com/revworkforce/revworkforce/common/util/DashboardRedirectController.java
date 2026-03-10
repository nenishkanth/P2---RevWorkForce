package com.revworkforce.revworkforce.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardRedirectController {

    @GetMapping("/dashboard")
    public String redirectDashboard(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .get()
                .getAuthority();

        if ("ROLE_ADMIN".equals(role)) {
            return "redirect:/admin/dashboard";
        }

        if ("ROLE_MANAGER".equals(role)) {
            return "redirect:/manager/dashboard";
        }

        if ("ROLE_EMPLOYEE".equals(role)) {
            return "redirect:/employee/dashboard";
        }

        return "redirect:/login";
    }
}
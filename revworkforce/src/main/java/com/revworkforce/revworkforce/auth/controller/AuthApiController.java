package com.revworkforce.revworkforce.auth.controller;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.revworkforce.revworkforce.auth.security.JwtUtil;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.LeaveBalance;
import com.revworkforce.revworkforce.employee.entity.LeavePolicy;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveBalanceRepository;
import com.revworkforce.revworkforce.employee.service.EmployeeService;
import com.revworkforce.revworkforce.employee.service.LeavePolicyService;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final EmployeeRepository repository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final EmployeeService employeeService;   // 🔥 ADD THIS
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeavePolicyService leavePolicyService;
   

    public AuthApiController(EmployeeRepository repository,
            PasswordEncoder encoder,
            JwtUtil jwtUtil,
            EmployeeService employeeService,
            LeavePolicyService leavePolicyService,
            LeaveBalanceRepository leaveBalanceRepository) {

    		this.repository = repository;
    		this.encoder = encoder;
    		this.jwtUtil = jwtUtil;
    		this.employeeService = employeeService;
    		this.leavePolicyService = leavePolicyService;
    		this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @PostMapping("/register")
    public String register(@RequestBody Employee employee) {

        if (repository.findByEmail(employee.getEmail()).isPresent()) {
            return "Email already exists!";
        }

        // 🔥 Generate Role-Based Employee ID
        String generatedId = employeeService.generateEmployeeId(employee.getRole());
        LeavePolicy policy = leavePolicyService.getPolicyByRole(employee.getRole());
        employee.setEmployeeId(generatedId);

        employee.setPassword(encoder.encode(employee.getPassword()));
        employee.setCreatedAt(LocalDateTime.now());

        // 🔥 Save employee first
        Employee savedEmployee = repository.save(employee);

        // 🔥 Create Leave Balance automatically
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(savedEmployee);
        balance.setCasualLeave(policy.getCasualLeave());
        balance.setSickLeave(policy.getSickLeave());
        balance.setPaidLeave(policy.getPaidLeave());

        leaveBalanceRepository.save(balance);

        return "User Registered Successfully with ID: " + generatedId;
    }

    @PostMapping("/login")
    public String login(@RequestBody Employee employee) {

        Employee dbUser = repository.findByEmail(employee.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(employee.getPassword(), dbUser.getPassword())) {
            return "Invalid password";
        }

        return jwtUtil.generateToken(
                dbUser.getEmail(),
                dbUser.getRole().name()
        );
    }
}
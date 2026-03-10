package com.revworkforce.revworkforce.manager.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.Leave;
import com.revworkforce.revworkforce.employee.service.EmployeeService;
import com.revworkforce.revworkforce.employee.service.LeaveService;

@RestController
@RequestMapping("/manager/leaves")
public class ManagerLeaveController {

	private final LeaveService service;
	private final EmployeeService employeeService;

	public ManagerLeaveController(LeaveService service,
	                               EmployeeService employeeService) {
	    this.service = service;
	    this.employeeService = employeeService;
	}

    // 🔹 View pending leaves
    @GetMapping("/pending")
    public List<Leave> getPendingLeaves() {
        return service.getPendingLeaves();
    }

 // 🔹 Approve leave
    @PutMapping("/{leaveId}/approve")
    public Leave approveLeave(@PathVariable Long leaveId,
                              @RequestParam String comment) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Employee manager = employeeService.getEmployeeByEmail(email);

        return service.approveLeave(leaveId, comment, manager.getId());
    }


    // 🔹 Reject leave
    @PutMapping("/{leaveId}/reject")
    public Leave rejectLeave(@PathVariable Long leaveId,
                             @RequestParam String comment) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Employee manager = employeeService.getEmployeeByEmail(email);

        return service.rejectLeave(leaveId, comment, manager.getId());
    }
}
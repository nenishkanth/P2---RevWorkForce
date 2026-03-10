package com.revworkforce.revworkforce.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.EmployeeStatus;
import com.revworkforce.revworkforce.employee.service.EmployeeService;

@RestController
@RequestMapping("/api/admin/employees")
public class AdminEmployeeController {

    private final EmployeeService service;

    public AdminEmployeeController(EmployeeService service) {
        this.service = service;
    }

    // GET ALL EMPLOYEES
    @GetMapping
    public List<Employee> getAllEmployees() {
        return service.getAllEmployees();
    }

    // GET EMPLOYEE BY ID
    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable Long id) {
        return service.getEmployeeById(id);
    }

    // UPDATE EMPLOYEE
    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id,
                                   @RequestBody Employee employee) {
        return service.updateEmployee(id, employee);
    }

    // DELETE EMPLOYEE
    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        service.deleteEmployee(id);
        return "Employee deleted successfully";
    }

    // UPDATE EMPLOYEE STATUS
    @PutMapping("/{id}/status")
    public Employee updateStatus(@PathVariable Long id,
                                 @RequestParam String status) {

        return service.changeEmployeeStatus(
                id,
                EmployeeStatus.valueOf(status)
        );
    }
}
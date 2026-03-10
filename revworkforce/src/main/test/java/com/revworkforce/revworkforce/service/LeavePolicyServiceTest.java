package com.revworkforce.revworkforce.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revworkforce.revworkforce.admin.entity.Role;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.Leave;
import com.revworkforce.revworkforce.employee.entity.LeaveBalance;
import com.revworkforce.revworkforce.employee.entity.LeavePolicy;
import com.revworkforce.revworkforce.employee.entity.LeaveStatus;
import com.revworkforce.revworkforce.employee.entity.LeaveType;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveBalanceRepository;
import com.revworkforce.revworkforce.employee.repository.LeavePolicyRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveRepository;
import com.revworkforce.revworkforce.employee.service.LeavePolicyService;

@ExtendWith(MockitoExtension.class)
class LeavePolicyServiceTest {

    @Mock private LeavePolicyRepository repository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private LeaveBalanceRepository leaveBalanceRepository;
    @Mock private LeaveRepository leaveRepository;

    @InjectMocks private LeavePolicyService leavePolicyService;

    private LeavePolicy existingPolicy;
    private LeavePolicy newPolicyData;
    private Employee employee;
    private LeaveBalance balance;

    @BeforeEach
    void setUp() {
        // Existing policy in DB
        existingPolicy = new LeavePolicy();
        existingPolicy.setRole(Role.EMPLOYEE);
        existingPolicy.setCasualLeave(12);
        existingPolicy.setSickLeave(10);
        existingPolicy.setPaidLeave(8);

        // New values admin wants to set
        newPolicyData = new LeavePolicy();
        newPolicyData.setCasualLeave(15);
        newPolicyData.setSickLeave(12);
        newPolicyData.setPaidLeave(10);

        // A test employee
        employee = new Employee();
        employee.setId(1L);
        employee.setRole(Role.EMPLOYEE);

        // Their current balance
        balance = new LeaveBalance();
        balance.setCasualLeave(12);
        balance.setSickLeave(10);
        balance.setPaidLeave(8);
    }

    // ════════════════════════════════════════════════
    // setPolicy()
    // ════════════════════════════════════════════════

    @Test
    void setPolicy_shouldUpdatePolicyValues() {
        when(repository.findByRole(Role.EMPLOYEE)).thenReturn(Optional.of(existingPolicy));
        when(repository.save(any())).thenReturn(existingPolicy);
        when(employeeRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(leaveRepository.findByEmployeeIdAndStatus(1L, LeaveStatus.APPROVED)).thenReturn(List.of());

        LeavePolicy result = leavePolicyService.setPolicy(Role.EMPLOYEE, newPolicyData);

        assertNotNull(result);
        verify(repository).save(any(LeavePolicy.class));
    }

    @Test
    void setPolicy_shouldRecalculateBalanceForAllEmployees() {
        // Employee has used 3 casual days (approved leave)
        Leave approvedLeave = new Leave();
        approvedLeave.setLeaveType(LeaveType.CASUAL);
        approvedLeave.setStartDate(LocalDate.of(2025, 6, 2));
        approvedLeave.setEndDate(LocalDate.of(2025, 6, 4)); // 3 days

        when(repository.findByRole(Role.EMPLOYEE)).thenReturn(Optional.of(existingPolicy));
        when(repository.save(any())).thenReturn(existingPolicy);
        when(employeeRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(leaveRepository.findByEmployeeIdAndStatus(1L, LeaveStatus.APPROVED))
                .thenReturn(List.of(approvedLeave));

        leavePolicyService.setPolicy(Role.EMPLOYEE, newPolicyData);

        // new casual = 15 - 3 used = 12
        assertEquals(12, balance.getCasualLeave());
        verify(leaveBalanceRepository).save(balance);
    }

    @Test
    void setPolicy_shouldSetBalanceToZero_whenUsedMoreThanNewPolicy() {
        // Employee used 20 days but new policy only gives 15
        Leave approvedLeave = new Leave();
        approvedLeave.setLeaveType(LeaveType.CASUAL);
        approvedLeave.setStartDate(LocalDate.of(2025, 1, 1));
        approvedLeave.setEndDate(LocalDate.of(2025, 1, 20)); // 20 days

        when(repository.findByRole(Role.EMPLOYEE)).thenReturn(Optional.of(existingPolicy));
        when(repository.save(any())).thenReturn(existingPolicy);
        when(employeeRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(leaveRepository.findByEmployeeIdAndStatus(1L, LeaveStatus.APPROVED))
                .thenReturn(List.of(approvedLeave));

        leavePolicyService.setPolicy(Role.EMPLOYEE, newPolicyData);

        // Math.max(0, 15 - 20) = 0 — should never go negative
        assertEquals(0, balance.getCasualLeave());
    }

    @Test
    void setPolicy_shouldCreateNewPolicy_whenNoneExists() {
        // No existing policy in DB
        when(repository.findByRole(Role.EMPLOYEE)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(newPolicyData);
        when(employeeRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of());

        LeavePolicy result = leavePolicyService.setPolicy(Role.EMPLOYEE, newPolicyData);

        assertNotNull(result);
        verify(repository).save(any());
    }

    @Test
    void setPolicy_shouldSkipEmployee_whenNoBalanceRecord() {
        // Employee exists but has no LeaveBalance record yet
        when(repository.findByRole(Role.EMPLOYEE)).thenReturn(Optional.of(existingPolicy));
        when(repository.save(any())).thenReturn(existingPolicy);
        when(employeeRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.empty());

        // Should not throw — just skips that employee
        assertDoesNotThrow(() -> leavePolicyService.setPolicy(Role.EMPLOYEE, newPolicyData));
        verify(leaveBalanceRepository, never()).save(any());
    }

    // ════════════════════════════════════════════════
    // getPolicyByRole()
    // ════════════════════════════════════════════════

    @Test
    void getPolicyByRole_shouldReturnPolicy_whenExists() {
        when(repository.findByRole(Role.EMPLOYEE)).thenReturn(Optional.of(existingPolicy));

        LeavePolicy result = leavePolicyService.getPolicyByRole(Role.EMPLOYEE);

        assertNotNull(result);
        assertEquals(12, result.getCasualLeave());
    }

    @Test
    void getPolicyByRole_shouldThrow_whenPolicyNotFound() {
        when(repository.findByRole(Role.MANAGER)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leavePolicyService.getPolicyByRole(Role.MANAGER));

        assertEquals("Policy not found", ex.getMessage());
    }

    // ════════════════════════════════════════════════
    // getAllPolicies()
    // ════════════════════════════════════════════════

    @Test
    void getAllPolicies_shouldReturnAllThreePolicies() {
        LeavePolicy p1 = new LeavePolicy(); p1.setRole(Role.EMPLOYEE);
        LeavePolicy p2 = new LeavePolicy(); p2.setRole(Role.MANAGER);
        LeavePolicy p3 = new LeavePolicy(); p3.setRole(Role.ADMIN);

        when(repository.findAll()).thenReturn(List.of(p1, p2, p3));

        List<LeavePolicy> result = leavePolicyService.getAllPolicies();

        assertEquals(3, result.size());
    }
}
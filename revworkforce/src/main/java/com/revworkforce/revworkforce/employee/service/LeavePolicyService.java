package com.revworkforce.revworkforce.employee.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

@Service
public class LeavePolicyService {

    private final LeavePolicyRepository repository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRepository leaveRepository;

    public LeavePolicyService(LeavePolicyRepository repository,
                              EmployeeRepository employeeRepository,
                              LeaveBalanceRepository leaveBalanceRepository,
                              LeaveRepository leaveRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveRepository = leaveRepository;
    }

    public LeavePolicy setPolicy(Role role, LeavePolicy policyData) {

        LeavePolicy policy = repository.findByRole(role)
                .orElse(new LeavePolicy());

        policy.setRole(role);
        policy.setCasualLeave(policyData.getCasualLeave());
        policy.setSickLeave(policyData.getSickLeave());
        policy.setPaidLeave(policyData.getPaidLeave());

        LeavePolicy savedPolicy = repository.save(policy);

        // Recalculate each employee's balance from scratch:
        // newBalance = newPolicyTotal - leavesAlreadyUsed (APPROVED only)
        List<Employee> employees = employeeRepository.findByRole(role);
        for (Employee employee : employees) {
            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeId(employee.getId())
                    .orElse(null);

            if (balance != null) {
                // Count approved leaves taken per type
                List<Leave> approvedLeaves = leaveRepository
                        .findByEmployeeIdAndStatus(employee.getId(), LeaveStatus.APPROVED);

                int casualUsed = 0, sickUsed = 0, paidUsed = 0;

                for (Leave leave : approvedLeaves) {
                    long days = java.time.temporal.ChronoUnit.DAYS
                            .between(leave.getStartDate(), leave.getEndDate()) + 1;
                    if (leave.getLeaveType() == LeaveType.CASUAL) casualUsed += days;
                    else if (leave.getLeaveType() == LeaveType.SICK)   sickUsed  += days;
                    else if (leave.getLeaveType() == LeaveType.PAID)   paidUsed  += days;
                }

                // Set balance = new policy total - what they've already used
                balance.setCasualLeave(Math.max(0, policyData.getCasualLeave() - casualUsed));
                balance.setSickLeave  (Math.max(0, policyData.getSickLeave()   - sickUsed));
                balance.setPaidLeave  (Math.max(0, policyData.getPaidLeave()   - paidUsed));

                leaveBalanceRepository.save(balance);
            }
        }

        return savedPolicy;
    }

    public LeavePolicy getPolicyByRole(Role role) {
        return repository.findByRole(role)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
    }

    public List<LeavePolicy> getAllPolicies() {
        return repository.findAll();
    }

    public LeavePolicy savePolicy(LeavePolicy policy) {
        return repository.save(policy);
    }
}
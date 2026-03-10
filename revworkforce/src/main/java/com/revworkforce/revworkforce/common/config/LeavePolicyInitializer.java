package com.revworkforce.revworkforce.common.config;

import org.springframework.stereotype.Component;

import com.revworkforce.revworkforce.admin.entity.Role;
import com.revworkforce.revworkforce.employee.entity.LeavePolicy;
import com.revworkforce.revworkforce.employee.repository.LeavePolicyRepository;

import jakarta.annotation.PostConstruct;

@Component
public class LeavePolicyInitializer {

    private final LeavePolicyRepository leavePolicyRepository;

    public LeavePolicyInitializer(LeavePolicyRepository leavePolicyRepository) {
        this.leavePolicyRepository = leavePolicyRepository;
    }

    @PostConstruct
    public void initPolicies() {

        if(leavePolicyRepository.count() == 0){

            LeavePolicy emp = new LeavePolicy();
            emp.setRole(Role.EMPLOYEE);
            emp.setCasualLeave(12);
            emp.setSickLeave(10);
            emp.setPaidLeave(8);

            LeavePolicy mgr = new LeavePolicy();
            mgr.setRole(Role.MANAGER);
            mgr.setCasualLeave(15);
            mgr.setSickLeave(12);
            mgr.setPaidLeave(10);

            LeavePolicy admin = new LeavePolicy();
            admin.setRole(Role.ADMIN);
            admin.setCasualLeave(18);
            admin.setSickLeave(15);
            admin.setPaidLeave(12);

            leavePolicyRepository.save(emp);
            leavePolicyRepository.save(mgr);
            leavePolicyRepository.save(admin);
        }
    }
}
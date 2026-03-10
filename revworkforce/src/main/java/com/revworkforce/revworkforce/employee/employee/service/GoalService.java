package com.revworkforce.revworkforce.employee.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.Goal;
import com.revworkforce.revworkforce.employee.entity.GoalPriority;
import com.revworkforce.revworkforce.employee.entity.GoalStatus;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.repository.GoalRepository;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public GoalService(GoalRepository goalRepository,
                       EmployeeRepository employeeRepository,
                       NotificationService notificationService) {

        this.goalRepository = goalRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    // 🔹 Employee adds goal
    public Goal addGoal(Long employeeId, Goal goal) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        goal.setEmployee(employee);
        goal.setStatus(GoalStatus.NOT_STARTED);
        if (goal.getPriority() == null) {
            goal.setPriority(GoalPriority.MEDIUM);
        }

        Goal saved = goalRepository.save(goal);

        // 🔔 Notify Manager
        if (employee.getReportingManager() != null) {
            notificationService.createNotification(
                    employee.getReportingManager(),
                    "New goal created by " + employee.getEmployeeId()
            );
        }

        return saved;
    }

    // 🔹 Employee views goals
    public List<Goal> getEmployeeGoals(Long employeeId) {
        return goalRepository.findByEmployeeId(employeeId);
    }

    // 🔹 Manager views team goals
    public List<Goal> getTeamGoals(Long managerId) {
        return goalRepository.findByEmployee_ReportingManager_Id(managerId);
    }

    // 🔹 Manager updates goal status
    public Goal updateGoalStatus(Long goalId,
                                 GoalStatus status,
                                 Long managerId) {

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (goal.getEmployee().getReportingManager() == null ||
            !goal.getEmployee().getReportingManager().getId().equals(managerId)) {

            throw new RuntimeException("Unauthorized update");
        }

        goal.setStatus(status);

        Goal updated = goalRepository.save(goal);

        // 🔔 Notify Employee
        notificationService.createNotification(
                goal.getEmployee(),
                "Your goal status has been updated to " + status.name()
        );

        return updated;
    }

    // 🔹 Manager adds their own personal goal
    public Goal addManagerGoal(Long managerId, Goal goal) {
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        goal.setEmployee(manager);
        goal.setStatus(GoalStatus.NOT_STARTED);
        if (goal.getPriority() == null) {
            goal.setPriority(GoalPriority.MEDIUM);
        }
        return goalRepository.save(goal);
    }

    // 🔹 Manager views their own personal goals
    public List<Goal> getManagerOwnGoals(Long managerId) {
        return goalRepository.findByEmployeeId(managerId);
    }

    // 🔹 Manager updates their own goal's priority
    public Goal updateManagerGoalPriority(Long goalId, GoalPriority priority, Long managerId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        if (!goal.getEmployee().getId().equals(managerId)) {
            throw new RuntimeException("Unauthorized");
        }
        goal.setPriority(priority);
        return goalRepository.save(goal);
    }

    // 🔹 Manager updates their own goal's status
    public Goal updateManagerGoalStatus(Long goalId, GoalStatus status, Long managerId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        if (!goal.getEmployee().getId().equals(managerId)) {
            throw new RuntimeException("Unauthorized");
        }
        goal.setStatus(status);
        return goalRepository.save(goal);
    }

    // 🔹 Manager deletes their own goal
    public void deleteManagerGoal(Long goalId, Long managerId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        if (!goal.getEmployee().getId().equals(managerId)) {
            throw new RuntimeException("Unauthorized");
        }
        goalRepository.delete(goal);
    }

    // 🔹 Manager updates team goal priority
    public Goal updateGoalPriority(Long goalId,
                                   GoalPriority priority,
                                   Long managerId) {

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (goal.getEmployee().getReportingManager() == null ||
            !goal.getEmployee().getReportingManager().getId().equals(managerId)) {

            throw new RuntimeException("Unauthorized update");
        }

        goal.setPriority(priority);

        Goal updated = goalRepository.save(goal);

        // 🔔 Notify Employee
        notificationService.createNotification(
                goal.getEmployee(),
                "Your goal priority has been updated to " + priority.name()
        );

        return updated;
    }
}
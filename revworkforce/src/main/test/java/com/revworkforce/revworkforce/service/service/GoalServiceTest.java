package com.revworkforce.revworkforce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.Goal;
import com.revworkforce.revworkforce.employee.entity.GoalStatus;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.repository.GoalRepository;
import com.revworkforce.revworkforce.employee.service.GoalService;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock private GoalRepository goalRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private GoalService goalService;

    private Employee employee;
    private Employee manager;
    private Goal goal;

    @BeforeEach
    void setUp() {
        manager = new Employee();
        manager.setId(10L);

        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");
        employee.setReportingManager(manager);

        goal = new Goal();
        goal.setId(1L);
        goal.setGoalName("Complete module");
        goal.setEmployee(employee);
        goal.setStatus(GoalStatus.NOT_STARTED);
    }

    // ════════════════════════════════════════════════
    // addGoal()
    // ════════════════════════════════════════════════

    @Test
    void addGoal_shouldSaveGoal_whenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(goalRepository.save(any())).thenReturn(goal);

        Goal result = goalService.addGoal(1L, goal);

        assertNotNull(result);
        assertEquals(GoalStatus.NOT_STARTED, result.getStatus());
        verify(goalRepository).save(any());
    }

    @Test
    void addGoal_shouldThrow_whenEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> goalService.addGoal(99L, goal));

        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    void addGoal_shouldNotifyManager_whenManagerExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(goalRepository.save(any())).thenReturn(goal);

        goalService.addGoal(1L, goal);

        // Notification should be sent to manager
        verify(notificationService).createNotification(eq(manager), anyString());
    }

    @Test
    void addGoal_shouldNotNotifyManager_whenNoManagerAssigned() {
        employee.setReportingManager(null); // no manager

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(goalRepository.save(any())).thenReturn(goal);

        goalService.addGoal(1L, goal);

        // No notification should be sent
        verify(notificationService, never()).createNotification(any(), anyString());
    }

    // ════════════════════════════════════════════════
    // updateGoalStatus()
    // ════════════════════════════════════════════════

    @Test
    void updateGoalStatus_shouldUpdate_whenManagerIsAuthorized() {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any())).thenReturn(goal);

        Goal result = goalService.updateGoalStatus(1L, GoalStatus.IN_PROGRESS, 10L);

        assertEquals(GoalStatus.IN_PROGRESS, result.getStatus());
        verify(goalRepository).save(any());
    }

    @Test
    void updateGoalStatus_shouldThrow_whenGoalNotFound() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> goalService.updateGoalStatus(99L, GoalStatus.COMPLETED, 10L));

        assertEquals("Goal not found", ex.getMessage());
    }

    @Test
    void updateGoalStatus_shouldThrow_whenManagerUnauthorized() {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        // Manager 99 is NOT the reporting manager (correct is 10)
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> goalService.updateGoalStatus(1L, GoalStatus.COMPLETED, 99L));

        assertEquals("Unauthorized update", ex.getMessage());
    }

    @Test
    void updateGoalStatus_shouldThrow_whenEmployeeHasNoManager() {
        employee.setReportingManager(null);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> goalService.updateGoalStatus(1L, GoalStatus.COMPLETED, 10L));

        assertEquals("Unauthorized update", ex.getMessage());
    }

    @Test
    void updateGoalStatus_shouldNotifyEmployee_afterUpdate() {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any())).thenReturn(goal);

        goalService.updateGoalStatus(1L, GoalStatus.COMPLETED, 10L);

        verify(notificationService).createNotification(eq(employee), anyString());
    }

    // ════════════════════════════════════════════════
    // getEmployeeGoals() and getTeamGoals()
    // ════════════════════════════════════════════════

    @Test
    void getEmployeeGoals_shouldReturnGoals() {
        when(goalRepository.findByEmployeeId(1L)).thenReturn(List.of(goal));

        List<Goal> result = goalService.getEmployeeGoals(1L);

        assertEquals(1, result.size());
        assertEquals("Complete module", result.get(0).getGoalName());
    }

    @Test
    void getTeamGoals_shouldReturnTeamGoals() {
        when(goalRepository.findByEmployee_ReportingManager_Id(10L)).thenReturn(List.of(goal));

        List<Goal> result = goalService.getTeamGoals(10L);

        assertEquals(1, result.size());
    }
}
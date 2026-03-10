package com.revworkforce.revworkforce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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

import com.revworkforce.revworkforce.calendar.repository.HolidayRepository;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.Leave;
import com.revworkforce.revworkforce.employee.entity.LeaveBalance;
import com.revworkforce.revworkforce.employee.entity.LeaveStatus;
import com.revworkforce.revworkforce.employee.entity.LeaveType;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveBalanceRepository;
import com.revworkforce.revworkforce.employee.repository.LeavePolicyRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveRepository;
import com.revworkforce.revworkforce.employee.service.LeaveService;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    // ── Mocks (fake DB — no real database needed) ──
    @Mock private LeaveRepository leaveRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private LeaveBalanceRepository leaveBalanceRepository;
    @Mock private NotificationService notificationService;
    @Mock private HolidayRepository holidayRepository;
    @Mock private LeavePolicyRepository leavePolicyRepository;

    // ── The real class we are testing ──
    @InjectMocks private LeaveService leaveService;

    // ── Shared test data ──
    private Employee employee;
    private LeaveBalance balance;
    private Leave leaveRequest;

    @BeforeEach
    void setUp() {
        // Build a fake employee
        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");

        // Build a fake leave balance (12 casual, 10 sick, 8 paid)
        balance = new LeaveBalance();
        balance.setCasualLeave(12);
        balance.setSickLeave(10);
        balance.setPaidLeave(8);

        // Build a basic leave request (Monday to Wednesday = 3 working days)
        leaveRequest = new Leave();
        leaveRequest.setStartDate(LocalDate.of(2025, 6, 2));  // Monday
        leaveRequest.setEndDate(LocalDate.of(2025, 6, 4));    // Wednesday
        leaveRequest.setLeaveType(LeaveType.CASUAL);
        leaveRequest.setReason("Personal work");
    }

    // ════════════════════════════════════════════════
    // applyLeave() — HAPPY PATH
    // ════════════════════════════════════════════════

    @Test
    void applyLeave_shouldSaveLeave_whenEverythingIsValid() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(holidayRepository.findByDateBetween(any(), any())).thenReturn(List.of());
        when(leaveRepository.save(any())).thenReturn(leaveRequest);

        // Act
        Leave result = leaveService.applyLeave(1L, leaveRequest);

        // Assert
        assertNotNull(result);
        verify(leaveRepository, times(1)).save(any());
    }

    // ════════════════════════════════════════════════
    // applyLeave() — ERROR CASES
    // ════════════════════════════════════════════════

    @Test
    void applyLeave_shouldThrow_whenEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(99L, leaveRequest));

        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    void applyLeave_shouldThrow_whenStartDateIsNull() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));

        leaveRequest.setStartDate(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(1L, leaveRequest));

        assertEquals("Start date and End date are required", ex.getMessage());
    }

    @Test
    void applyLeave_shouldThrow_whenEndDateIsNull() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));

        leaveRequest.setEndDate(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(1L, leaveRequest));

        assertEquals("Start date and End date are required", ex.getMessage());
    }

    @Test
    void applyLeave_shouldThrow_whenStartDateIsAfterEndDate() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));

        leaveRequest.setStartDate(LocalDate.of(2025, 6, 10));
        leaveRequest.setEndDate(LocalDate.of(2025, 6, 5));   // end before start

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(1L, leaveRequest));

        assertEquals("Invalid leave date range", ex.getMessage());
    }

    @Test
    void applyLeave_shouldThrow_whenDatesAreOnlyWeekend() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(holidayRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        // Saturday and Sunday only
        leaveRequest.setStartDate(LocalDate.of(2025, 6, 7));  // Saturday
        leaveRequest.setEndDate(LocalDate.of(2025, 6, 8));    // Sunday

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(1L, leaveRequest));

        assertEquals("Selected dates fall only on weekends/holidays", ex.getMessage());
    }

    @Test
    void applyLeave_shouldThrow_whenInsufficientCasualLeave() {
        balance.setCasualLeave(1); // only 1 day left, but requesting 3 days

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(holidayRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        leaveRequest.setLeaveType(LeaveType.CASUAL);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(1L, leaveRequest));

        assertEquals("Insufficient casual leave balance", ex.getMessage());
    }

    @Test
    void applyLeave_shouldThrow_whenInsufficientSickLeave() {
        balance.setSickLeave(0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(holidayRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        leaveRequest.setLeaveType(LeaveType.SICK);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(1L, leaveRequest));

        assertEquals("Insufficient sick leave balance", ex.getMessage());
    }

    @Test
    void applyLeave_shouldThrow_whenInsufficientPaidLeave() {
        balance.setPaidLeave(0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(holidayRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        leaveRequest.setLeaveType(LeaveType.PAID);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(1L, leaveRequest));

        assertEquals("Insufficient paid leave balance", ex.getMessage());
    }

    // ════════════════════════════════════════════════
    // approveLeave()
    // ════════════════════════════════════════════════

    @Test
    void approveLeave_shouldApproveAndDeductBalance() {
        Employee manager = new Employee(); manager.setId(10L);
        employee.setReportingManager(manager);

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setLeaveType(LeaveType.CASUAL);
        leave.setStartDate(LocalDate.of(2025, 6, 2));
        leave.setEndDate(LocalDate.of(2025, 6, 4));
        leave.setStatus(LeaveStatus.PENDING);

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(leaveRepository.save(any())).thenReturn(leave);

        Leave result = leaveService.approveLeave(1L, "Approved", 10L);

        assertEquals(LeaveStatus.APPROVED, result.getStatus());
        verify(leaveBalanceRepository).save(any()); // balance was saved
    }

    @Test
    void approveLeave_shouldThrow_whenLeaveNotFound() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.approveLeave(99L, "ok", 10L));

        assertEquals("Leave not found", ex.getMessage());
    }

    @Test
    void approveLeave_shouldThrow_whenManagerIsUnauthorized() {
        Employee correctManager = new Employee(); correctManager.setId(10L);
        employee.setReportingManager(correctManager);

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setLeaveType(LeaveType.CASUAL);

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));

        // managerId=99 is NOT the reporting manager (correct is 10)
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.approveLeave(1L, "ok", 99L));

        assertEquals("Unauthorized approval attempt", ex.getMessage());
    }

    // ════════════════════════════════════════════════
    // rejectLeave()
    // ════════════════════════════════════════════════

    @Test
    void rejectLeave_shouldRejectLeave_whenAuthorized() {
        Employee manager = new Employee(); manager.setId(10L);
        employee.setReportingManager(manager);

        Leave leave = new Leave();
        leave.setEmployee(employee);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any())).thenReturn(leave);

        Leave result = leaveService.rejectLeave(1L, "Not approved", 10L);

        assertEquals(LeaveStatus.REJECTED, result.getStatus());
    }

    @Test
    void rejectLeave_shouldThrow_whenUnauthorized() {
        Employee manager = new Employee(); manager.setId(10L);
        employee.setReportingManager(manager);

        Leave leave = new Leave();
        leave.setEmployee(employee);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.rejectLeave(1L, "no", 99L));

        assertEquals("Unauthorized rejection attempt", ex.getMessage());
    }

    // ════════════════════════════════════════════════
    // cancelLeave()
    // ════════════════════════════════════════════════

    @Test
    void cancelLeave_shouldCancel_whenPending() {
        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setStatus(LeaveStatus.PENDING);

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any())).thenReturn(leave);

        Leave result = leaveService.cancelLeave(1L, 1L);

        assertEquals(LeaveStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelLeave_shouldRestoreBalance_whenApproved() {
        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setLeaveType(LeaveType.CASUAL);
        leave.setStartDate(LocalDate.of(2025, 6, 2));
        leave.setEndDate(LocalDate.of(2025, 6, 4));

        balance.setCasualLeave(9); // was 12, used 3

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(balance));
        when(leaveRepository.save(any())).thenReturn(leave);

        leaveService.cancelLeave(1L, 1L);

        // balance should be restored: 9 + 3 = 12
        assertEquals(12, balance.getCasualLeave());
    }

    @Test
    void cancelLeave_shouldThrow_whenAlreadyCancelled() {
        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setStatus(LeaveStatus.CANCELLED);

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.cancelLeave(1L, 1L));

        assertEquals("Leave already cancelled", ex.getMessage());
    }

    @Test
    void cancelLeave_shouldThrow_whenUnauthorized() {
        Employee other = new Employee(); other.setId(99L);
        Leave leave = new Leave();
        leave.setEmployee(other); // belongs to employee 99

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));

        // employee 1 trying to cancel employee 99's leave
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.cancelLeave(1L, 1L));

        assertEquals("Unauthorized cancellation", ex.getMessage());
    }
}
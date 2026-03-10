package com.revworkforce.revworkforce.employee.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.calendar.entity.Holiday;
import com.revworkforce.revworkforce.calendar.repository.HolidayRepository;
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
import com.revworkforce.revworkforce.notification.service.NotificationService;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final NotificationService notificationService;
    private final HolidayRepository holidayRepository;
    private final LeavePolicyRepository leavePolicyRepository;


    public LeaveService(LeaveRepository leaveRepository,
                        EmployeeRepository employeeRepository,
                        LeaveBalanceRepository leaveBalanceRepository,
                        NotificationService notificationService,HolidayRepository holidayRepository,
                        LeavePolicyRepository leavePolicyRepository) {

        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.notificationService = notificationService;
        this.holidayRepository = holidayRepository;
        this.leavePolicyRepository = leavePolicyRepository;
    }

    // 🔹 Employee applies for leave


    public Leave applyLeave(Long employeeId, Leave leaveRequest) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeId(employeeId)
                .orElseGet(() -> createLeaveBalance(employeeId));

        // ✅ Validate date range
        if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
            throw new RuntimeException("Start date and End date are required");
        }

        if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
            throw new RuntimeException("Invalid leave date range");
        }

        LocalDate start = leaveRequest.getStartDate();
        LocalDate end = leaveRequest.getEndDate();

        // 🔥 Fetch holidays in range
        List<Holiday> holidays = holidayRepository.findByDateBetween(start, end);

        Set<LocalDate> holidayDates = holidays.stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        long effectiveLeaveDays = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {

            boolean isWeekend =
                    current.getDayOfWeek() == DayOfWeek.SATURDAY ||
                            current.getDayOfWeek() == DayOfWeek.SUNDAY;

            boolean isHoliday = holidayDates.contains(current);

            if (!isWeekend && !isHoliday) {
                effectiveLeaveDays++;
            }

            current = current.plusDays(1);
        }

        if (effectiveLeaveDays <= 0) {
            throw new RuntimeException("Selected dates fall only on weekends/holidays");
        }

        int days = (int) effectiveLeaveDays;

        // 🔥 Validate Leave Balance
        switch (leaveRequest.getLeaveType()) {

            case CASUAL:
                if (balance.getCasualLeave() < days)
                    throw new RuntimeException("Insufficient casual leave balance");
                break;

            case SICK:
                if (balance.getSickLeave() < days)
                    throw new RuntimeException("Insufficient sick leave balance");
                break;

            case PAID:
                if (balance.getPaidLeave() < days)
                    throw new RuntimeException("Insufficient paid leave balance");
                break;

            default:
                throw new RuntimeException("Invalid leave type");
        }

        // 🔥 Set Leave Details
        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setAppliedAt(LocalDateTime.now());

        Leave savedLeave = leaveRepository.save(leaveRequest);

        // 🔔 Notify Manager
        if (employee.getReportingManager() != null) {
            notificationService.createNotification(
                    employee.getReportingManager(),
                    "New leave request from " + employee.getEmployeeId()
            );
        }

        // 🔔 Notify Employee
        notificationService.createNotification(
                employee,
                "Your leave request has been submitted successfully"
        );

        return savedLeave;
    }

    // 🔹 Employee views their leaves
    public List<Leave> getEmployeeLeaves(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    // 🔹 Manager views all pending leaves
    public List<Leave> getPendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING);
    }


    // 🔹 Manager approves leave (only for their team)
    public Leave approveLeave(Long leaveId, String comment, Long managerId) {

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        if (!leave.getEmployee()
                .getReportingManager()
                .getId()
                .equals(managerId)) {

            throw new RuntimeException("Unauthorized approval attempt");
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeId(leave.getEmployee().getId())
                .orElseGet(() -> createLeaveBalance(leave.getEmployee().getId()));

        int days = (int) ChronoUnit.DAYS.between(
                leave.getStartDate(),
                leave.getEndDate()) + 1;

        switch (leave.getLeaveType()) {

            case CASUAL:
                balance.setCasualLeave(balance.getCasualLeave() - days);
                break;

            case SICK:
                balance.setSickLeave(balance.getSickLeave() - days);
                break;

            case PAID:
                balance.setPaidLeave(balance.getPaidLeave() - days);
                break;

            default:
                throw new RuntimeException("Invalid leave type");
        }

        leaveBalanceRepository.save(balance);

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setManagerComments(comment);

        Leave savedLeave = leaveRepository.save(leave);

        // 🔔 Notify Employee
        notificationService.createNotification(
                leave.getEmployee(),
                "Your leave from " + leave.getStartDate() +
                        " to " + leave.getEndDate() +
                        " has been APPROVED."
        );

        return savedLeave;
    }

    // 🔹 Manager rejects leave (only for their team)
    public Leave rejectLeave(Long leaveId, String comment, Long managerId) {

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        if (!leave.getEmployee()
                .getReportingManager()
                .getId()
                .equals(managerId)) {

            throw new RuntimeException("Unauthorized rejection attempt");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setManagerComments(comment);

        Leave savedLeave = leaveRepository.save(leave);

        // 🔔 Notify Employee
        notificationService.createNotification(
                leave.getEmployee(),
                "Your leave from " + leave.getStartDate() +
                        " to " + leave.getEndDate() +
                        " has been REJECTED. Reason: " + comment
        );

        return savedLeave;
    }

    public List<Leave> getTeamLeaves(Long managerId) {
        return leaveRepository.findByEmployee_ReportingManager_Id(managerId);
    }
    public List<Leave> getTeamLeavesByStatus(Long managerId, LeaveStatus status) {
        return leaveRepository.findByEmployeeReportingManagerIdAndStatus(managerId, status);
    }

    public Leave cancelLeave(Long leaveId, Long employeeId) {

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        if (!leave.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Unauthorized cancellation");
        }

        if (leave.getStatus() == LeaveStatus.CANCELLED) {
            throw new RuntimeException("Leave already cancelled");
        }

        if (leave.getStatus() == LeaveStatus.REJECTED) {
            leave.setStatus(LeaveStatus.CANCELLED);
            return leaveRepository.save(leave);
        }

        if (leave.getStatus() == LeaveStatus.PENDING) {
            leave.setStatus(LeaveStatus.CANCELLED);
            return leaveRepository.save(leave);
        }

        if (leave.getStatus() == LeaveStatus.APPROVED) {

            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeId(employeeId)
                    .orElseGet(() -> createLeaveBalance(employeeId));

            long days = ChronoUnit.DAYS.between(
                    leave.getStartDate(),
                    leave.getEndDate()) + 1;

            switch (leave.getLeaveType()) {
                case CASUAL:
                    balance.setCasualLeave(balance.getCasualLeave() + (int) days);
                    break;
                case SICK:
                    balance.setSickLeave(balance.getSickLeave() + (int) days);
                    break;
                case PAID:
                    balance.setPaidLeave(balance.getPaidLeave() + (int) days);
                    break;
            }

            leaveBalanceRepository.save(balance);

            leave.setStatus(LeaveStatus.CANCELLED);
            Leave savedLeave = leaveRepository.save(leave);

            // 🔔 Notify Manager
            if (leave.getEmployee().getReportingManager() != null) {
                notificationService.createNotification(
                        leave.getEmployee().getReportingManager(),
                        leave.getEmployee().getEmployeeId() + " cancelled approved leave"
                );
            }

            return savedLeave;
        }

        throw new RuntimeException("Cannot cancel this leave");
    }

    public LeaveBalance getLeaveBalance(Long employeeId) {

        return leaveBalanceRepository
                .findByEmployeeId(employeeId)
                .orElseGet(() -> createLeaveBalance(employeeId));
    }

    public List<Leave> getEmployeeLeavesByStatus(Long employeeId, LeaveStatus status) {
        return leaveRepository.findByEmployeeIdAndStatus(employeeId, status);
    }

    public Map<String, Long> getTeamLeaveStats(Long managerId) {

        List<Object[]> results = leaveRepository.countTeamLeavesByStatus(managerId);

        Map<String, Long> stats = new HashMap<>();

        for (Object[] row : results) {
            LeaveStatus status = (LeaveStatus) row[0];
            Long count = (Long) row[1];
            stats.put(status.name(), count);
        }

        return stats;
    }



    public List<Leave> filterLeaves(
            LeaveStatus status,
            LeaveType type,
            Long departmentId,
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate) {

        return leaveRepository.filterLeaves(
                status, type, departmentId, employeeId, startDate, endDate);
    }

    public List<Object[]> getMonthlyLeaveReport() {
        return leaveRepository.getMonthlyLeaveReport();
    }

    public List<Object[]> getEmployeeLeaveReport() {

        return leaveRepository.getEmployeeLeaveReport();

    }

    public List<Object[]> getDepartmentLeaveReport() {

        return leaveRepository.getDepartmentLeaveReport();

    }



    private LeaveBalance createLeaveBalance(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeavePolicy policy = leavePolicyRepository
                .findByRole(employee.getRole())
                .orElseThrow(() -> new RuntimeException("Leave policy not configured"));

        LeaveBalance balance = new LeaveBalance();

        balance.setEmployee(employee);
        balance.setCasualLeave(policy.getCasualLeave());
        balance.setSickLeave(policy.getSickLeave());
        balance.setPaidLeave(policy.getPaidLeave());

        return leaveBalanceRepository.save(balance);
    }

    public List<Leave> getEmployeeLeaveReport(Long employeeId) {
        return leaveRepository.findLeavesByEmployeeId(employeeId);
    }



    public List<Leave> getAllLeaves() {

        return leaveRepository.findAllByOrderByStartDateDesc();

    }

    public List<Leave> getTeamUpcomingLeaves(Long managerId){
        return leaveRepository.findUpcomingLeaves(managerId);
    }

    public long countLeavesByType(Long managerId, LeaveType type) {
        return leaveRepository.countByEmployee_ReportingManager_IdAndLeaveType(managerId, type);
    }

    public void initializeLeaveBalance(Long employeeId,
                                       int casual,
                                       int sick,
                                       int paid){

        LeaveBalance balance = new LeaveBalance();

        balance.setEmployee(employeeRepository.findById(employeeId).orElseThrow());
        balance.setCasualLeave(casual);
        balance.setSickLeave(sick);
        balance.setPaidLeave(paid);

        leaveBalanceRepository.save(balance);
    }



    // Admin approves a manager's leave directly (bypasses manager ownership check)
    public Leave approveLeaveByAdmin(Long leaveId, String comment) {

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeId(leave.getEmployee().getId())
                .orElseGet(() -> createLeaveBalance(leave.getEmployee().getId()));

        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(
                leave.getStartDate(), leave.getEndDate()) + 1;

        switch (leave.getLeaveType()) {
            case CASUAL: balance.setCasualLeave(balance.getCasualLeave() - days); break;
            case SICK:   balance.setSickLeave(balance.getSickLeave()     - days); break;
            case PAID:   balance.setPaidLeave(balance.getPaidLeave()     - days); break;
            default: throw new RuntimeException("Invalid leave type");
        }

        leaveBalanceRepository.save(balance);

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setManagerComments(comment);
        Leave saved = leaveRepository.save(leave);

        notificationService.createNotification(
                leave.getEmployee(),
                "Your leave from " + leave.getStartDate() +
                        " to " + leave.getEndDate() + " has been APPROVED by Admin."
        );

        return saved;
    }

    // Admin rejects a manager's leave directly
    public Leave rejectLeaveByAdmin(Long leaveId, String comment) {

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setManagerComments(comment);
        Leave saved = leaveRepository.save(leave);

        notificationService.createNotification(
                leave.getEmployee(),
                "Your leave from " + leave.getStartDate() +
                        " to " + leave.getEndDate() + " has been REJECTED by Admin. Reason: " + comment
        );

        return saved;
    }

}
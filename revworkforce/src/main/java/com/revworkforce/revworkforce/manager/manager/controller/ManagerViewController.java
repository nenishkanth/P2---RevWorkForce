package com.revworkforce.revworkforce.manager.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.revworkforce.revworkforce.admin.service.ActivityLogService;
import com.revworkforce.revworkforce.admin.service.AnnouncementService;
import com.revworkforce.revworkforce.calendar.service.HolidayService;
import com.revworkforce.revworkforce.employee.dto.EmployeeProfileUpdateRequest;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.Goal;
import com.revworkforce.revworkforce.employee.entity.GoalPriority;
import com.revworkforce.revworkforce.employee.entity.GoalStatus;
import com.revworkforce.revworkforce.employee.entity.Leave;
import com.revworkforce.revworkforce.employee.entity.LeaveStatus;
import com.revworkforce.revworkforce.employee.entity.LeaveType;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.service.EmployeeService;
import com.revworkforce.revworkforce.employee.service.GoalService;
import com.revworkforce.revworkforce.employee.service.LeaveService;
import com.revworkforce.revworkforce.manager.service.PerformanceReviewService;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@Controller
public class ManagerViewController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final GoalService goalService;
    private final PerformanceReviewService reviewService;
    private final NotificationService notificationService;
    private final HolidayService holidayService;
    private final AnnouncementService announcementService;
    private final ActivityLogService activityLogService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmployeeRepository repository;

    public ManagerViewController(EmployeeService employeeService,
                                 LeaveService leaveService,
                                 GoalService goalService,
                                 PerformanceReviewService reviewService,
                                 NotificationService notificationService,
                                 HolidayService holidayService,
                                 AnnouncementService announcementService,
                                 ActivityLogService activityLogService,
                                 BCryptPasswordEncoder passwordEncoder,
                                 EmployeeRepository repository) {

        this.employeeService = employeeService;
        this.leaveService = leaveService;
        this.goalService = goalService;
        this.reviewService = reviewService;
        this.notificationService = notificationService;
        this.holidayService = holidayService;
        this.announcementService = announcementService;
        this.activityLogService = activityLogService;
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Authentication auth, Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        Long managerId = manager.getId();

        model.addAttribute("manager", manager);

        // Team size
        int teamSize = employeeService.getTeamMembers(managerId).size();
        model.addAttribute("teamSize", teamSize);

        // Leave stats
        int pendingLeaves =
                leaveService.getTeamLeavesByStatus(managerId, LeaveStatus.PENDING).size();

        int approvedLeaves =
                leaveService.getTeamLeavesByStatus(managerId, LeaveStatus.APPROVED).size();

        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("approvedLeaves", approvedLeaves);

        // Leave chart data
        long casualLeaves =
                leaveService.countLeavesByType(managerId, LeaveType.CASUAL);

        long sickLeaves =
                leaveService.countLeavesByType(managerId, LeaveType.SICK);

        long paidLeaves =
                leaveService.countLeavesByType(managerId, LeaveType.PAID);

        model.addAttribute("casualLeaves", casualLeaves);
        model.addAttribute("sickLeaves", sickLeaves);
        model.addAttribute("paidLeaves", paidLeaves);

        // Upcoming leaves
        model.addAttribute("upcomingLeaves",
                leaveService.getTeamUpcomingLeaves(managerId));

        // Recent leaves
        model.addAttribute("recentLeaves",
                leaveService.getTeamLeaves(managerId));

        // Notifications
        model.addAttribute("notifications",
                notificationService.getNotifications(managerId));

        // Team goals
        model.addAttribute("teamGoals",
                goalService.getTeamGoals(managerId));

        return "manager/dashboard";
    }

    @GetMapping("/manager/team")
    public String team(Authentication auth, Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("team",
                employeeService.getTeamMembers(manager.getId()));

        return "manager/team";
    }

    @GetMapping("/manager/leaves")
    public String teamLeaves(Authentication auth, Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("teamLeaves",
                leaveService.getTeamLeavesByStatus(manager.getId(), LeaveStatus.PENDING));

        return "manager/leaves";
    }

    @PostMapping("/manager/leaves/reject/{id}")
    public String rejectLeave(@PathVariable Long id,
                              @RequestParam String comment,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            leaveService.rejectLeave(id, comment, manager.getId());
            activityLogService.logActivity(
                    manager.getEmail(), "MANAGER", "REJECT_LEAVE",
                    "Rejected leave request ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Leave rejected.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/leaves";
    }

    @PostMapping("/manager/leaves/approve/{id}")
    public String approveLeave(@PathVariable Long id,
                               @RequestParam String comment,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            leaveService.approveLeave(id, comment, manager.getId());
            activityLogService.logActivity(
                    manager.getEmail(), "MANAGER", "APPROVE_LEAVE",
                    "Approved leave request ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Leave approved successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/leaves";
    }

    @GetMapping("/manager/leaves/history")
    public String leaveHistory(Authentication auth, Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("leaves",
                leaveService.getTeamLeaves(manager.getId()));

        return "manager/leave-history";
    }

    @GetMapping("/manager/goals")
    public String teamGoals(Authentication auth, Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("goals",
                goalService.getTeamGoals(manager.getId()));

        return "manager/goals";
    }

    @PostMapping("/manager/goals/update/{id}")
    public String updateGoal(@PathVariable Long id,
                             @RequestParam GoalStatus status,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            goalService.updateGoalStatus(id, status, manager.getId());
            activityLogService.logActivity(
                    manager.getEmail(), "MANAGER", "UPDATE_GOAL",
                    "Updated goal " + id + " to " + status);
            redirectAttributes.addFlashAttribute("successMessage", "Goal updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/goals";
    }

    @PostMapping("/manager/goals/update-priority/{id}")
    public String updateGoalPriority(@PathVariable Long id,
                                     @RequestParam GoalPriority priority,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            goalService.updateGoalPriority(id, priority, manager.getId());
            activityLogService.logActivity(
                    manager.getEmail(), "MANAGER", "UPDATE_GOAL_PRIORITY",
                    "Updated goal " + id + " priority to " + priority);
            redirectAttributes.addFlashAttribute("successMessage", "Goal priority updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/goals";
    }

    // ===== MANAGER PERSONAL GOALS =====

    @GetMapping("/manager/my-goals")
    public String myGoals(Authentication auth, Model model) {
        Employee manager = employeeService.getEmployeeByEmail(auth.getName());
        model.addAttribute("myGoals", goalService.getManagerOwnGoals(manager.getId()));
        model.addAttribute("goalStatuses", GoalStatus.values());
        return "manager/my-goals";
    }

    @PostMapping("/manager/my-goals/create")
    public String createMyGoal(Authentication auth, Goal goal,
                               RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            goalService.addManagerGoal(manager.getId(), goal);
            activityLogService.logActivity(manager.getEmail(), "MANAGER", "CREATE_OWN_GOAL",
                    "Created personal goal: " + goal.getDescription());
            redirectAttributes.addFlashAttribute("successMessage", "Goal created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/my-goals";
    }

    @PostMapping("/manager/my-goals/update-priority/{id}")
    public String updateMyGoalPriority(@PathVariable Long id,
                                       @RequestParam GoalPriority priority,
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            goalService.updateManagerGoalPriority(id, priority, manager.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Priority updated!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/my-goals";
    }

    @PostMapping("/manager/my-goals/update-status/{id}")
    public String updateMyGoalStatus(@PathVariable Long id,
                                     @RequestParam GoalStatus status,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            goalService.updateManagerGoalStatus(id, status, manager.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Status updated!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/my-goals";
    }

    @PostMapping("/manager/my-goals/delete/{id}")
    public String deleteMyGoal(@PathVariable Long id,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            goalService.deleteManagerGoal(id, manager.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Goal deleted.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/my-goals";
    }

    @GetMapping("/manager/reviews")
    public String reviews(Authentication auth, Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("reviews",
                reviewService.getTeamReviews(manager.getId()));

        return "manager/reviews";
    }

    @PostMapping("/manager/reviews/evaluate/{id}")
    public String evaluateReview(@PathVariable Long id,
                                 @RequestParam int managerRating,
                                 @RequestParam String feedback,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            reviewService.evaluateReview(id, managerRating, feedback, manager.getId());
            activityLogService.logActivity(
                    manager.getEmail(), "MANAGER", "EVALUATE_REVIEW",
                    "Evaluated review ID " + id + " with rating " + managerRating);
            redirectAttributes.addFlashAttribute("successMessage", "Review evaluated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/reviews";
    }

    @GetMapping("/manager/notifications")
    public String notifications(Authentication auth, Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("notifications",
                notificationService.getNotifications(manager.getId()));

        return "manager/notifications";
    }

    @PostMapping("/manager/notifications/delete/{id}")
    public String deleteNotification(@PathVariable Long id,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            notificationService.deleteNotification(id, manager.getId());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/notifications";
    }

    @PostMapping("/manager/notifications/read/{id}")
    public String markNotificationRead(@PathVariable Long id,
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            notificationService.markAsRead(id, manager.getId());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/notifications";
    }

    @GetMapping("/manager/calendar")
    public String managerCalendar(Model model) {

        model.addAttribute("holidays",
                holidayService.getAllHolidays());

        return "manager/calendar";
    }

    @GetMapping("/manager/announcements")
    public String managerAnnouncements(Model model) {

        model.addAttribute("announcements",
                announcementService.getAllAnnouncements());

        return "manager/announcements";
    }

    @GetMapping("/manager/team-leave-calendar")
    public String teamLeaveCalendar(Authentication auth, Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        List<Employee> team = employeeService.getTeamMembers(manager.getId());

        List<Leave> leaves = leaveService.getTeamLeaves(manager.getId())
                .stream()
                .filter(l -> "APPROVED".equals(l.getStatus()))
                .toList();

        int total = team.size();

        int onLeave = (int) leaves.stream()
                .filter(l -> "APPROVED".equals(l.getStatus()))
                .filter(l -> !l.getStartDate().isAfter(LocalDate.now())
                        && !l.getEndDate().isBefore(LocalDate.now()))
                .count();

        int available = total - onLeave;

        Map<LocalDate, Long> conflicts =
                leaves.stream()
                        .filter(l -> "APPROVED".equals(l.getStatus()))
                        .collect(Collectors.groupingBy(
                                Leave::getStartDate,
                                Collectors.counting()));

        Map<String, Long> heatmap =
                leaves.stream()
                        .collect(Collectors.groupingBy(
                                l -> l.getEmployee().getDepartment() != null
                                        ? l.getEmployee().getDepartment().getName()
                                        : "No Department",
                                Collectors.counting()));

        model.addAttribute("teamLeaves", leaves);
        model.addAttribute("total", total);
        model.addAttribute("onLeave", onLeave);
        model.addAttribute("available", available);
        model.addAttribute("conflicts", conflicts);
        model.addAttribute("heatmap", heatmap);

        return "manager/team-leave-calendar";
    }

    @GetMapping("/manager/team/member/{id}")
    public String viewTeamMember(@PathVariable Long id,
                                 Authentication auth,
                                 Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        Employee employee = employeeService.getEmployeeById(id);

        model.addAttribute("employee", employee);

        model.addAttribute("goals",
                goalService.getEmployeeGoals(id));

        model.addAttribute("leaves",
                leaveService.getEmployeeLeaves(id));

        model.addAttribute("reviews",
                reviewService.getEmployeeReviews(id));

        model.addAttribute("leaveBalance",
                leaveService.getLeaveBalance(id));

        return "manager/team-member-profile";
    }

    @GetMapping("/manager/profile")
    public String managerProfile(Authentication authentication, Model model) {

        String email = authentication.getName();

        Employee manager = employeeService.getEmployeeByEmail(email);

        model.addAttribute("employee", manager);

        return "manager/profile";
    }

    @PostMapping("/manager/profile/update")
    public String updateManagerProfile(Authentication auth,
                                       @RequestParam(required=false) String phoneNumber,
                                       @RequestParam(required=false) String address,
                                       @RequestParam(required=false) String emergencyContact,
                                       @RequestParam(required=false) String gender,
                                       @RequestParam(required=false)
                                       @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate dob,
                                       @RequestParam(required=false) MultipartFile profilePic)
            throws IOException {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        EmployeeProfileUpdateRequest request = new EmployeeProfileUpdateRequest();

        request.setPhoneNumber(phoneNumber);
        request.setAddress(address);
        request.setEmergencyContact(emergencyContact);
        request.setGender(gender);
        request.setDob(dob);

        if(profilePic != null && !profilePic.isEmpty()){

            String fileName = System.currentTimeMillis() + "_" +
                    profilePic.getOriginalFilename().replaceAll("\\s+","_");

            Path uploadPath = Paths.get("uploads");

            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            Files.copy(profilePic.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            request.setProfilePic(fileName);
        }

        employeeService.updateProfile(manager.getId(), request);

        return "redirect:/manager/profile?success";
    }

    @PostMapping("/manager/security/update")
    public String updateManagerSecurity(Authentication auth,
                                        @RequestParam String securityQuestion1,
                                        @RequestParam String securityAnswer1,
                                        @RequestParam String securityQuestion2,
                                        @RequestParam String securityAnswer2,
                                        @RequestParam String securityQuestion3,
                                        @RequestParam String securityAnswer3) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        manager.setSecurityQuestion1(securityQuestion1);
        manager.setSecurityAnswer1(passwordEncoder.encode(securityAnswer1));

        manager.setSecurityQuestion2(securityQuestion2);
        manager.setSecurityAnswer2(passwordEncoder.encode(securityAnswer2));

        manager.setSecurityQuestion3(securityQuestion3);
        manager.setSecurityAnswer3(passwordEncoder.encode(securityAnswer3));

        employeeService.save(manager);

        return "redirect:/manager/profile?success";
    }

    public Employee updateEmployee(Employee employee){
        return repository.save(employee);
    }

    // ==================== MANAGER APPLY LEAVE ====================

    @GetMapping("/manager/apply-leave")
    public String managerApplyLeave(Authentication auth, Model model) {

        Employee manager = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("leaveBalance",
                leaveService.getLeaveBalance(manager.getId()));

        model.addAttribute("myLeaves",
                leaveService.getEmployeeLeaves(manager.getId()));

        return "manager/apply-leave";
    }

    @PostMapping("/manager/apply-leave/submit")
    public String submitManagerLeave(Authentication auth, Leave leaveRequest,
                                     RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            leaveService.applyLeave(manager.getId(), leaveRequest);
            activityLogService.logActivity(
                    manager.getEmail(), "MANAGER", "APPLY_LEAVE",
                    "Applied for leave from " + leaveRequest.getStartDate());
            redirectAttributes.addFlashAttribute("successMessage", "Leave applied successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/apply-leave";
    }

    @PostMapping("/manager/apply-leave/cancel/{id}")
    public String cancelManagerLeave(@PathVariable Long id, Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            Employee manager = employeeService.getEmployeeByEmail(auth.getName());
            leaveService.cancelLeave(id, manager.getId());
            activityLogService.logActivity(
                    manager.getEmail(), "MANAGER", "CANCEL_LEAVE",
                    "Cancelled leave ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Leave cancelled.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/apply-leave";
    }

}
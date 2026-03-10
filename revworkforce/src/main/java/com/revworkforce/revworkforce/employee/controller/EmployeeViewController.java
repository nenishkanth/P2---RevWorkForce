package com.revworkforce.revworkforce.employee.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

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
import com.revworkforce.revworkforce.calendar.entity.Holiday;
import com.revworkforce.revworkforce.calendar.service.HolidayService;
import com.revworkforce.revworkforce.employee.dto.EmployeeProfileUpdateRequest;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.Goal;
import com.revworkforce.revworkforce.employee.entity.GoalStatus;
import com.revworkforce.revworkforce.employee.entity.Leave;
import com.revworkforce.revworkforce.employee.entity.LeaveBalance;
import com.revworkforce.revworkforce.employee.entity.LeaveStatus;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.service.EmployeeService;
import com.revworkforce.revworkforce.employee.service.GoalService;
import com.revworkforce.revworkforce.employee.service.LeaveService;
import com.revworkforce.revworkforce.manager.entity.PerformanceReview;
import com.revworkforce.revworkforce.manager.service.PerformanceReviewService;
import com.revworkforce.revworkforce.notification.entity.Notification;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@Controller
public class EmployeeViewController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final GoalService goalService;
    private final PerformanceReviewService reviewService;
    private final NotificationService notificationService;
    private final HolidayService holidayService;
    private final AnnouncementService announcementService;
    private final ActivityLogService activityLogService;
    private final EmployeeRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;


    public EmployeeViewController(EmployeeService employeeService,
                                  LeaveService leaveService,
                                  GoalService goalService,
                                  PerformanceReviewService reviewService,
                                  NotificationService notificationService,
                                  HolidayService holidayService,
                                  AnnouncementService announcementService,
                                  ActivityLogService activityLogService,
                                  EmployeeRepository repository,
                                  BCryptPasswordEncoder passwordEncoder) {

        this.employeeService = employeeService;
        this.leaveService = leaveService;
        this.goalService = goalService;
        this.reviewService = reviewService;
        this.notificationService = notificationService;
        this.holidayService = holidayService;
        this.announcementService = announcementService;
        this.activityLogService = activityLogService;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Authentication auth, Model model) {

        Employee employee = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("employee", employee);

        // Leave Balance
        LeaveBalance leaveBalance = leaveService.getLeaveBalance(employee.getId());
        model.addAttribute("leaveBalance", leaveBalance);

        // Pending Leaves Count
        int pendingLeaves = leaveService
                .getEmployeeLeavesByStatus(employee.getId(), LeaveStatus.PENDING)
                .size();

        model.addAttribute("pendingLeaves", pendingLeaves);

        // Leave History
        model.addAttribute("leaves",
                leaveService.getEmployeeLeaves(employee.getId()));

        // GOALS
        List<Goal> goals = goalService.getEmployeeGoals(employee.getId());

        long activeGoals = goals.stream()
                .filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS
                        || g.getStatus() == GoalStatus.NOT_STARTED)
                .count();

        long completedGoals = goals.stream()
                .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
                .count();

        model.addAttribute("activeGoals", activeGoals);
        model.addAttribute("completedGoals", completedGoals);

        // HOLIDAYS
        List<Holiday> holidays = holidayService.getUpcomingHolidays();

        model.addAttribute("holidays", holidays);
        model.addAttribute("holidaysCount", holidays.size());

        // NOTIFICATIONS
        List<Notification> notifications =
                notificationService.getEmployeeNotifications(employee.getId());

        model.addAttribute("notifications", notifications);

        // Unread count
        model.addAttribute("unreadNotifications",
                notificationService.getUnreadCount(employee.getId()));

        return "employee/dashboard";
    }

    @GetMapping("/employee/profile")
    public String viewProfile(Authentication auth, Model model) {

        Employee emp = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("employee", emp);

        return "employee/profile";
    }

    @PostMapping("/employee/profile/update")
    public String updateProfile(Authentication auth,
                                @RequestParam(required=false) String phoneNumber,
                                @RequestParam(required=false) String address,
                                @RequestParam(required=false) String emergencyContact,
                                @RequestParam(required=false) String gender,
                                @RequestParam(required=false)
                                @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate dob,
                                @RequestParam(required=false) MultipartFile profilePic,
                                RedirectAttributes redirectAttributes)
            throws IOException {

        Employee emp = employeeService.getEmployeeByEmail(auth.getName());

        EmployeeProfileUpdateRequest request = new EmployeeProfileUpdateRequest();

        request.setPhoneNumber(phoneNumber);
        request.setAddress(address);
        request.setEmergencyContact(emergencyContact);
        request.setGender(gender);
        request.setDob(dob);

        if(profilePic != null && !profilePic.isEmpty()){

            if(profilePic.getSize() > 5 * 1024 * 1024){
                redirectAttributes.addFlashAttribute("errorMessage", "File size exceeds 5MB limit");
                return "redirect:/employee/profile";
            }

            String contentType = profilePic.getContentType();

            if(!contentType.equals("image/jpeg") &&
                    !contentType.equals("image/png") &&
                    !contentType.equals("image/jpg")){
                redirectAttributes.addFlashAttribute("errorMessage", "Only JPG and PNG images allowed");
                return "redirect:/employee/profile";
            }

            String fileName = System.currentTimeMillis() + "_" +
                    profilePic.getOriginalFilename().replaceAll("\\s+", "_");

            Path uploadPath = Paths.get("uploads");

            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            Files.copy(profilePic.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            request.setProfilePic(fileName);
        }

        employeeService.updateProfile(emp.getId(), request);

        return "redirect:/employee/profile?success";
    }

    @PostMapping("/employee/security/update")
    public String updateSecurity(Authentication auth,
                                 @RequestParam String securityQuestion1,
                                 @RequestParam String securityAnswer1,
                                 @RequestParam String securityQuestion2,
                                 @RequestParam String securityAnswer2,
                                 @RequestParam String securityQuestion3,
                                 @RequestParam String securityAnswer3) {

        Employee emp = employeeService.getEmployeeByEmail(auth.getName());

        emp.setSecurityQuestion1(securityQuestion1);
        emp.setSecurityAnswer1(passwordEncoder.encode(securityAnswer1));

        emp.setSecurityQuestion2(securityQuestion2);
        emp.setSecurityAnswer2(passwordEncoder.encode(securityAnswer2));

        emp.setSecurityQuestion3(securityQuestion3);
        emp.setSecurityAnswer3(passwordEncoder.encode(securityAnswer3));

        employeeService.save(emp);

        return "redirect:/employee/profile?success";
    }

    @GetMapping("/employee/leaves")
    public String viewLeaves(Authentication auth,
                             @RequestParam(required = false) LeaveStatus status,
                             Model model) {

        Employee emp = employeeService.getEmployeeByEmail(auth.getName());

        if (status == null) {
            model.addAttribute("leaves",
                    leaveService.getEmployeeLeaves(emp.getId()));
        } else {
            model.addAttribute("leaves",
                    leaveService.getEmployeeLeavesByStatus(emp.getId(), status));
        }

        model.addAttribute("leaveBalance",
                leaveService.getLeaveBalance(emp.getId()));

        model.addAttribute("leaveRequest", new Leave());

        return "employee/leaves";
    }

    @PostMapping("/employee/leaves/apply")
    public String applyLeave(Authentication auth, Leave leaveRequest,
                             RedirectAttributes redirectAttributes) {
        try {
            Employee emp = employeeService.getEmployeeByEmail(auth.getName());
            leaveService.applyLeave(emp.getId(), leaveRequest);
            activityLogService.logActivity(
                    emp.getEmail(), "EMPLOYEE", "APPLY_LEAVE",
                    "Applied for leave from " + leaveRequest.getStartDate());
            redirectAttributes.addFlashAttribute("successMessage", "Leave applied successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employee/leaves";
    }

    @PostMapping("/employee/leaves/cancel/{id}")
    public String cancelLeave(@PathVariable Long id, Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            Employee emp = employeeService.getEmployeeByEmail(auth.getName());
            leaveService.cancelLeave(id, emp.getId());
            activityLogService.logActivity(
                    emp.getEmail(), "EMPLOYEE", "CANCEL_LEAVE",
                    "Cancelled leave ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Leave cancelled successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employee/leaves";
    }

    @GetMapping("/employee/notifications")
    public String notifications(Authentication auth, Model model) {

        Employee emp = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("notifications",
                notificationService.getNotifications(emp.getId()));

        model.addAttribute("unreadCount",
                notificationService.getUnreadCount(emp.getId()));

        return "employee/notifications";
    }

    @PostMapping("/employee/notifications/read/{id}")
    public String markRead(@PathVariable Long id, Authentication auth,
                           RedirectAttributes redirectAttributes) {
        try {
            Employee emp = employeeService.getEmployeeByEmail(auth.getName());
            notificationService.markAsRead(id, emp.getId());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employee/notifications";
    }

    @PostMapping("/employee/notifications/delete/{id}")
    public String deleteNotification(@PathVariable Long id, Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            Employee emp = employeeService.getEmployeeByEmail(auth.getName());
            notificationService.deleteNotification(id, emp.getId());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employee/notifications";
    }

    @PostMapping("/employee/goals/create")
    public String createGoal(Authentication auth, Goal goal,
                             RedirectAttributes redirectAttributes) {
        try {
            Employee emp = employeeService.getEmployeeByEmail(auth.getName());
            goalService.addGoal(emp.getId(), goal);
            activityLogService.logActivity(
                    emp.getEmail(), "EMPLOYEE", "CREATE_GOAL",
                    "Created new goal: " + goal.getGoalName());
            redirectAttributes.addFlashAttribute("successMessage", "Goal created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employee/goals";
    }

    @GetMapping("/employee/goals")
    public String goals(Authentication auth, Model model) {

        Employee emp = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("goals",
                goalService.getEmployeeGoals(emp.getId()));

        model.addAttribute("goal", new Goal());

        return "employee/goals";
    }

    @GetMapping("/employee/reviews")
    public String reviews(Authentication auth, Model model) {

        Employee emp = employeeService.getEmployeeByEmail(auth.getName());

        model.addAttribute("reviews",
                reviewService.getEmployeeReviews(emp.getId()));
        model.addAttribute("review", new PerformanceReview());

        return "employee/reviews";
    }

    @PostMapping("/employee/reviews/submit")
    public String submitReview(Authentication auth, PerformanceReview review,
                               RedirectAttributes redirectAttributes) {
        try {
            Employee emp = employeeService.getEmployeeByEmail(auth.getName());
            reviewService.submitReview(emp.getId(), review);
            activityLogService.logActivity(
                    emp.getEmail(), "EMPLOYEE", "SUBMIT_REVIEW",
                    "Submitted performance review");
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employee/reviews";
    }

    @GetMapping("/employee/announcements")
    public String employeeAnnouncements(Model model) {

        model.addAttribute("announcements",
                announcementService.getAllAnnouncements());

        return "employee/announcements";
    }

    @GetMapping("/employee/calendar")
    public String employeeCalendar(Model model) {

        model.addAttribute("holidays",
                holidayService.getAllHolidays());

        return "employee/calendar";
    }

    @GetMapping("/employee/manager")
    public String viewManager(Authentication authentication, Model model) {

        String email = authentication.getName();

        Employee employee = employeeService.getEmployeeByEmail(email);

        Employee manager = employee.getReportingManager();

        model.addAttribute("manager", manager);

        return "employee/manager";
    }
}
package com.revworkforce.revworkforce.admin.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.revworkforce.revworkforce.admin.entity.Announcement;
import com.revworkforce.revworkforce.admin.entity.Department;
import com.revworkforce.revworkforce.admin.entity.Designation;
import com.revworkforce.revworkforce.admin.entity.Role;
import com.revworkforce.revworkforce.admin.service.ActivityLogService;
import com.revworkforce.revworkforce.admin.service.AnalyticsService;
import com.revworkforce.revworkforce.admin.service.AnnouncementService;
import com.revworkforce.revworkforce.admin.service.DepartmentService;
import com.revworkforce.revworkforce.admin.service.DesignationService;
import com.revworkforce.revworkforce.calendar.entity.Holiday;
import com.revworkforce.revworkforce.calendar.service.HolidayService;
import com.revworkforce.revworkforce.employee.dto.EmployeeProfileUpdateRequest;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.EmployeeStatus;
import com.revworkforce.revworkforce.employee.entity.Leave;
import com.revworkforce.revworkforce.employee.entity.LeavePolicy;
import com.revworkforce.revworkforce.employee.entity.LeaveStatus;
import com.revworkforce.revworkforce.employee.entity.LeaveType;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveBalanceRepository;
import com.revworkforce.revworkforce.employee.repository.LeavePolicyRepository;
import com.revworkforce.revworkforce.employee.service.EmployeeService;
import com.revworkforce.revworkforce.employee.service.LeavePolicyService;
import com.revworkforce.revworkforce.employee.service.LeaveService;

@Controller
public class AdminViewController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final AnnouncementService announcementService;
    private final HolidayService holidayService;
    private final LeavePolicyService leavePolicyService;
    private final DepartmentService departmentService;
    private final DesignationService designationService;
    private final ActivityLogService activityLogService;
    private final AnalyticsService analyticsService;
    private final EmployeeRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public AdminViewController(EmployeeService employeeService,
                               LeaveService leaveService,
                               AnnouncementService announcementService,
                               HolidayService holidayService,
                               LeavePolicyService leavePolicyService,
                               DepartmentService departmentService,
                               DesignationService designationService,
                               ActivityLogService activityLogService,
                               AnalyticsService analyticsService,
                               EmployeeRepository repository,
                               PasswordEncoder passwordEncoder,
                               LeavePolicyRepository leavePolicyRepository,
                               LeaveBalanceRepository leaveBalanceRepository) {

        this.employeeService = employeeService;
        this.leaveService = leaveService;
        this.announcementService = announcementService;
        this.holidayService = holidayService;
        this.leavePolicyService = leavePolicyService;
        this.departmentService = departmentService;
        this.designationService = designationService;
        this.activityLogService = activityLogService;
        this.analyticsService = analyticsService;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.leavePolicyRepository = leavePolicyRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Employee loggedInEmployee = employeeService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged-in employee not found"));

        model.addAttribute("loggedInEmployee", loggedInEmployee);

        model.addAttribute("employeeCount",
                employeeService.getAllEmployees().size());

        model.addAttribute("pendingLeaveCount",
                leaveService.getPendingLeaves().size());

        model.addAttribute("announcementCount",
                announcementService.getAllAnnouncements().size());

        model.addAttribute("holidayCount",
                holidayService.getAllHolidays().size());

        // Real recent activity: last 5 leaves + last 3 announcements
        java.util.List<Leave> allLeaves = leaveService.getAllLeaves();
        java.util.List<Leave> recentLeaves = allLeaves.stream()
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("recentLeaves", recentLeaves);

        java.util.List<Announcement> recentAnnouncements =
                announcementService.getAllAnnouncements().stream()
                        .limit(3)
                        .collect(java.util.stream.Collectors.toList());
        model.addAttribute("recentAnnouncements", recentAnnouncements);

        // Real monthly leave trend for current year (long[12])
        java.util.Map<String, Object> analyticsData = analyticsService.getAdminAnalyticsData();
        model.addAttribute("dashboardMonthlyTrend", analyticsData.get("monthlyTrend"));

        return "admin/dashboard";
    }

    @GetMapping("/admin/employees")
    public String employees(@RequestParam(required=false) String keyword,
                            @RequestParam(required=false) Role role,
                            Model model) {

        List<Employee> employees;

        if (keyword != null && !keyword.isEmpty()) {
            employees = employeeService.searchEmployees(keyword);
        }
        else if (role != null) {
            employees = employeeService.getEmployeesByRole(role);
        }
        else {
            employees = employeeService.getAllEmployees();
        }

        model.addAttribute("employees", employees);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);

        model.addAttribute("managers",
                employeeService.getEmployeesByRole(Role.MANAGER));

        model.addAttribute("admins",
                employeeService.getEmployeesByRole(Role.ADMIN));

        return "admin/employees";
    }

    @GetMapping("/admin/calendar")
    public String calendar(Model model) {

        model.addAttribute("holidays",
                holidayService.getAllHolidays());

        model.addAttribute("holiday", new Holiday());

        return "admin/calendar";
    }

    @PostMapping("/admin/calendar/add")
    public String addHoliday(Holiday holiday, RedirectAttributes redirectAttributes) {
        try {
            holidayService.addHoliday(holiday);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "ADD_HOLIDAY",
                    "Added holiday: " + holiday.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Holiday added successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/calendar";
    }

    @PostMapping("/admin/calendar/delete/{id}")
    public String deleteHoliday(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            holidayService.deleteHoliday(id);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "DELETE_HOLIDAY",
                    "Deleted holiday ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Holiday deleted.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/calendar";
    }

    @GetMapping("/admin/calendar/edit/{id}")
    public String editHoliday(@PathVariable Long id, Model model) {

        Holiday holiday = holidayService.getAllHolidays()
                .stream()
                .filter(h -> h.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Holiday not found"));

        model.addAttribute("holiday", holiday);
        model.addAttribute("holidays", holidayService.getAllHolidays());

        return "admin/calendar";
    }

    @GetMapping("/admin/announcements")
    public String announcements(Model model) {

        model.addAttribute("announcements",
                announcementService.getAllAnnouncements());

        return "admin/announcements";
    }

    @PostMapping("/admin/announcements")
    public String createAnnouncement(Authentication auth,
                                     @RequestParam String title,
                                     @RequestParam String message,
                                     RedirectAttributes redirectAttributes) {
        try {
            Announcement announcement = new Announcement();
            announcement.setTitle(title);
            announcement.setContent(message);
            String createdBy = auth.getName();
            announcementService.createAnnouncement(announcement, createdBy);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement created!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/announcements";
    }

    @GetMapping("/admin/analytics")
    public String analytics(Model model) {

        model.addAttribute("employeeCount",
                employeeService.getAllEmployees().size());

        model.addAttribute("leaveCount",
                leaveService.getAllLeaves().size());

        model.addAttribute("announcementCount",
                announcementService.getAllAnnouncements().size());

        model.addAttribute("holidayCount",
                holidayService.getAllHolidays().size());

        // Real chart data from database
        java.util.Map<String, Object> analyticsData = analyticsService.getAdminAnalyticsData();
        model.addAttribute("leavesByStatus", analyticsData.get("leavesByStatus"));
        model.addAttribute("leavesByType",   analyticsData.get("leavesByType"));
        model.addAttribute("monthlyTrend",   analyticsData.get("monthlyTrend"));
        model.addAttribute("leavesByDept",   analyticsData.get("leavesByDept"));

        return "admin/analytics";
    }

    @GetMapping("/admin/policies")
    public String policies(Model model) {

        model.addAttribute("policies",
                leavePolicyService.getAllPolicies());

        return "admin/policies";
    }

    @PostMapping("/admin/policies/update")
    public String updatePolicy(@RequestParam Long id,
                               @RequestParam String role,
                               @RequestParam int casualLeave,
                               @RequestParam int sickLeave,
                               @RequestParam int paidLeave,
                               RedirectAttributes redirectAttributes) {
        try {
            LeavePolicy policyData = new LeavePolicy();
            policyData.setCasualLeave(casualLeave);
            policyData.setSickLeave(sickLeave);
            policyData.setPaidLeave(paidLeave);
            Role roleEnum = Role.valueOf(role);
            leavePolicyService.setPolicy(roleEnum, policyData);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "UPDATE_POLICY",
                    "Updated leave policy for role: " + role);
            redirectAttributes.addFlashAttribute("successMessage", "Leave policy updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/policies";
    }

    @GetMapping("/admin/register")
    public String registerPage(Model model) {

        model.addAttribute("employee", new Employee());

        model.addAttribute("departments",
                departmentService.getAllDepartments());

        model.addAttribute("designations",
                designationService.getAllDesignations());

        model.addAttribute("managers",
                employeeService.getEmployeesByRole(Role.MANAGER));

        model.addAttribute("admins",
                employeeService.getEmployeesByRole(Role.ADMIN));

        return "admin/register";
    }

    @PostMapping("/admin/register")
    public String registerEmployee(Employee employee,
                                   @RequestParam(required = false) Long managerId,
                                   @RequestParam(required = false) Long adminId,
                                   RedirectAttributes redirectAttributes) {
        try {
            if(employee.getRole() == Role.EMPLOYEE && managerId != null){
                Employee manager = repository.findById(managerId)
                        .orElseThrow(() -> new RuntimeException("Manager not found"));
                employee.setReportingManager(manager);
            }

            if(employee.getRole() == Role.MANAGER && adminId != null){
                Employee admin = repository.findById(adminId)
                        .orElseThrow(() -> new RuntimeException("Admin not found"));
                employee.setAssignedAdmin(admin);
            }

            if(repository.existsByEmail(employee.getEmail())){
                redirectAttributes.addFlashAttribute("errorMessage", "Email already exists. Please use a different email.");
                return "redirect:/admin/register";
            }

            // Generate Employee ID based on role
            String prefix = "";
            if(employee.getRole() == Role.EMPLOYEE){ prefix = "EMP"; }
            else if(employee.getRole() == Role.MANAGER){ prefix = "MGR"; }
            else if(employee.getRole() == Role.ADMIN){ prefix = "ADMIN"; }

            Employee lastEmployee = repository.findTopByRoleOrderByEmployeeIdDesc(employee.getRole());

            int nextNumber = 1;
            if(lastEmployee != null){
                String lastId = lastEmployee.getEmployeeId();
                String numberPart = lastId.replace(prefix, "");
                nextNumber = Integer.parseInt(numberPart) + 1;
            }

            String newEmployeeId = prefix + String.format("%03d", nextNumber);
            if(prefix.equals("EMP")){
                newEmployeeId = prefix + String.format("%04d", nextNumber);
            }

            employee.setEmployeeId(newEmployeeId);
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
            employee.setStatus(EmployeeStatus.ACTIVE);

            Employee savedEmployee = repository.save(employee);

            redirectAttributes.addFlashAttribute("successMessage", "Employee registered successfully!");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/register";
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/admin/employees/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam EmployeeStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            employeeService.changeEmployeeStatus(id, status);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "CHANGE_EMPLOYEE_STATUS",
                    "Changed employee ID " + id + " to " + status);
            redirectAttributes.addFlashAttribute("successMessage", "Employee status updated.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/admin/leaves")
    public String viewLeaves(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) LeaveType type,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        List<Leave> leaves;

        if (status == null && type == null && departmentId == null
                && employeeId == null && startDate == null && endDate == null) {
            leaves = leaveService.getAllLeaves();
        } else {
            leaves = leaveService.filterLeaves(
                    status, type, departmentId, employeeId, startDate, endDate);
        }

        model.addAttribute("leaves", leaves);
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("departments", departmentService.getAllDepartments());

        return "admin/leaves";
    }

    @GetMapping("/admin/reports/leaves")
    public String leaveReports(
            @RequestParam(required = false) Long employeeId,
            Model model) {

        model.addAttribute("employees", employeeService.getAllEmployees());

        model.addAttribute("departmentReport",
                leaveService.getDepartmentLeaveReport());

        model.addAttribute("monthlyReport",
                leaveService.getMonthlyLeaveReport());

        if(employeeId != null) {
            model.addAttribute("employeeLeaves",
                    leaveService.getEmployeeLeaveReport(employeeId));
        }

        return "admin/leave-reports";
    }

    @GetMapping("/admin/departments")
    public String departments(Model model) {

        model.addAttribute("departments",
                departmentService.getAllDepartments());

        model.addAttribute("department", new Department());

        return "admin/departments";
    }

    @PostMapping("/admin/departments/save")
    public String saveDepartment(Department department, RedirectAttributes redirectAttributes) {
        try {
            departmentService.saveDepartment(department);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "SAVE_DEPARTMENT",
                    "Saved department " + department.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Department saved successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/admin/departments/edit/{id}")
    public String editDepartment(@PathVariable Long id, Model model) {

        model.addAttribute("department",
                departmentService.getDepartmentById(id));

        model.addAttribute("departments",
                departmentService.getAllDepartments());

        return "admin/departments";
    }

    @PostMapping("/admin/departments/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteDepartment(id);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "DELETE_DEPARTMENT",
                    "Deleted department ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Department deleted.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/admin/designations")
    public String designations(Model model) {

        model.addAttribute("designation", new Designation());

        model.addAttribute("designations",
                designationService.getAllDesignations());

        model.addAttribute("departments",
                departmentService.getAllDepartments());

        return "admin/designations";
    }

    @PostMapping("/admin/designations/save")
    public String saveDesignation(Designation designation, RedirectAttributes redirectAttributes) {
        try {
            designationService.saveDesignation(designation);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "SAVE_DESIGNATION",
                    "Saved designation " + designation.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Designation saved successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/designations";
    }

    @GetMapping("/admin/designations/edit/{id}")
    public String editDesignation(@PathVariable Long id, Model model) {

        model.addAttribute("designation",
                designationService.getDesignation(id));

        model.addAttribute("designations",
                designationService.getAllDesignations());

        model.addAttribute("departments",
                departmentService.getAllDepartments());

        return "admin/designations";
    }

    @PostMapping("/admin/designations/delete/{id}")
    public String deleteDesignation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            designationService.deleteDesignation(id);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "DELETE_DESIGNATION",
                    "Deleted designation ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Designation deleted.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/designations";
    }

    @GetMapping("/admin/activity-logs")
    public String viewLogs(Model model) {

        model.addAttribute("logs",
                activityLogService.getAllLogs());

        return "admin/activity-logs";
    }

    @GetMapping("/admin/employees/edit/{id}")
    public String editEmployee(@PathVariable Long id, Model model) {

        model.addAttribute("employee",
                employeeService.getEmployeeById(id));

        model.addAttribute("departments",
                departmentService.getAllDepartments());

        model.addAttribute("designations",
                designationService.getAllDesignations());

        model.addAttribute("managers",
                employeeService.getManagers());

        return "admin/edit-employee";
    }

    @PostMapping("/admin/employees/update")
    public String updateEmployee(Employee employee, RedirectAttributes redirectAttributes) {
        try {
            employeeService.updateEmployee(employee);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "UPDATE_EMPLOYEE",
                    "Updated employee " + employee.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Employee updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/admin/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(id);
            activityLogService.logActivity(
                    "ADMIN", "ADMIN", "DELETE_EMPLOYEE",
                    "Deleted employee ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Employee deleted.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/admin/employees/assign-manager/{id}")
    public String assignManager(@PathVariable Long id,
                                @RequestParam Long managerId,
                                RedirectAttributes redirectAttributes) {
        try {
            employeeService.assignManager(id, managerId);
            redirectAttributes.addFlashAttribute("successMessage", "Manager assigned successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/admin/employees/assign-admin/{id}")
    public String assignAdmin(@PathVariable Long id,
                              @RequestParam Long adminId,
                              RedirectAttributes redirectAttributes) {
        try {
            employeeService.assignAdmin(id, adminId);
            redirectAttributes.addFlashAttribute("successMessage", "Admin assigned successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/admin/profile")
    public String adminProfile(Authentication authentication, Model model) {

        String email = authentication.getName();

        Employee admin = employeeService.getEmployeeByEmail(email);

        model.addAttribute("employee", admin);

        return "admin/profile";
    }

    @PostMapping("/admin/profile/update")
    public String updateAdminProfile(Authentication auth,
                                     @RequestParam(required=false) String phoneNumber,
                                     @RequestParam(required=false) String address,
                                     @RequestParam(required=false) String emergencyContact,
                                     @RequestParam(required=false) String gender,
                                     @RequestParam(required=false)
                                     @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate dob,
                                     @RequestParam(required=false) MultipartFile profilePic)
            throws IOException {

        Employee admin = employeeService.getEmployeeByEmail(auth.getName());

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

        employeeService.updateProfile(admin.getId(), request);

        return "redirect:/admin/profile?success";
    }

    @PostMapping("/admin/security/update")
    public String updateAdminSecurity(Authentication auth,
                                      @RequestParam String securityQuestion1,
                                      @RequestParam String securityAnswer1,
                                      @RequestParam String securityQuestion2,
                                      @RequestParam String securityAnswer2,
                                      @RequestParam String securityQuestion3,
                                      @RequestParam String securityAnswer3) {

        Employee admin = employeeService.getEmployeeByEmail(auth.getName());

        admin.setSecurityQuestion1(securityQuestion1);
        admin.setSecurityAnswer1(passwordEncoder.encode(securityAnswer1));

        admin.setSecurityQuestion2(securityQuestion2);
        admin.setSecurityAnswer2(passwordEncoder.encode(securityAnswer2));

        admin.setSecurityQuestion3(securityQuestion3);
        admin.setSecurityAnswer3(passwordEncoder.encode(securityAnswer3));

        employeeService.save(admin);

        return "redirect:/admin/profile?success";
    }

    // ==================== ADMIN: MANAGER LEAVE APPROVAL ====================

    @GetMapping("/admin/manager-leaves")
    public String managerLeavesPending(Model model) {

        List<Leave> all = leaveService.getAllLeaves();
        List<Leave> managerLeaves = all.stream()
                .filter(l -> l.getEmployee().getRole() == Role.MANAGER
                        && l.getStatus() == LeaveStatus.PENDING)
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("managerLeaves", managerLeaves);
        return "admin/manager-leaves";
    }

    @GetMapping("/admin/manager-leaves/all")
    public String managerLeavesAll(Model model) {

        List<Leave> all = leaveService.getAllLeaves();
        List<Leave> managerLeaves = all.stream()
                .filter(l -> l.getEmployee().getRole() == Role.MANAGER)
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("managerLeaves", managerLeaves);
        return "admin/manager-leaves";
    }

    @PostMapping("/admin/manager-leaves/approve/{id}")
    public String approveManagerLeave(@PathVariable Long id,
                                      @RequestParam(required = false) String comment,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            String adminEmail = authentication.getName();
            leaveService.approveLeaveByAdmin(id, comment != null ? comment : "Approved by Admin");
            activityLogService.logActivity(
                    adminEmail, "ADMIN", "APPROVE_MANAGER_LEAVE",
                    "Approved manager leave ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Manager leave approved!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/manager-leaves";
    }

    @PostMapping("/admin/manager-leaves/reject/{id}")
    public String rejectManagerLeave(@PathVariable Long id,
                                     @RequestParam(required = false) String comment,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        try {
            String adminEmail = authentication.getName();
            leaveService.rejectLeaveByAdmin(id, comment != null ? comment : "Rejected by Admin");
            activityLogService.logActivity(
                    adminEmail, "ADMIN", "REJECT_MANAGER_LEAVE",
                    "Rejected manager leave ID " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Manager leave rejected.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/manager-leaves";
    }

}
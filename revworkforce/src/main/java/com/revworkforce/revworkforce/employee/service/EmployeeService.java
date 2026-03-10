package com.revworkforce.revworkforce.employee.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.admin.entity.Role;
import com.revworkforce.revworkforce.admin.service.ActivityLogService;
import com.revworkforce.revworkforce.employee.dto.EmployeeProfileUpdateRequest;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.EmployeeStatus;
import com.revworkforce.revworkforce.employee.entity.LeaveBalance;
import com.revworkforce.revworkforce.employee.entity.LeavePolicy;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveBalanceRepository;
import com.revworkforce.revworkforce.employee.repository.LeavePolicyRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final ActivityLogService activityLogService;
    private final EmployeeRepository employeeRepository;
    
    private static final int DEFAULT_PAID_LEAVE = 6;
    private static final int DEFAULT_SICK_LEAVE = 8;
    private static final int DEFAULT_CASUAL_LEAVE = 12;

    public EmployeeService(EmployeeRepository repository,
                           BCryptPasswordEncoder passwordEncoder,
                           LeaveBalanceRepository leaveBalanceRepository,
                           LeavePolicyRepository leavePolicyRepository,
                           ActivityLogService activityLogService,
                           EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leavePolicyRepository = leavePolicyRepository;
        this.activityLogService = activityLogService;
        this.employeeRepository = employeeRepository;
    }


        // Get all employees
        public List<Employee> getAllEmployees() {
            return repository.findAll();
        }

        // Get employee by email (throws exception if not found)
        public Employee getEmployeeByEmail(String email) {
            return repository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
        }

        // Get employee by email returning Optional (for controller use)
        public Optional<Employee> findByEmail(String email) {
            return repository.findByEmail(email);
        }

        // Get employee by id
        public Employee getEmployeeById(Long id) {
            return repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
        }

        // Get team members under a manager
        public List<Employee> getTeamMembers(Long managerId) {
            return repository.findByReportingManagerId(managerId);
        }

        public Employee registerEmployee(Employee employee, Long managerId, Long adminId) {

            // Assign Reporting Manager (for Employees)
            if (managerId != null) {

                Employee manager = repository.findById(managerId)
                        .orElseThrow(() -> new RuntimeException("Manager not found"));

                employee.setReportingManager(manager);
            }

            // Assign Admin (for Managers)
            if (adminId != null) {

                Employee admin = repository.findById(adminId)
                        .orElseThrow(() -> new RuntimeException("Admin not found"));

                employee.setAssignedAdmin(admin);
            }

            // Generate Employee ID
            String employeeId = generateEmployeeId(employee.getRole());
            employee.setEmployeeId(employeeId);

            // Encrypt password
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));

            employee.setStatus(EmployeeStatus.ACTIVE);

            Employee savedEmployee = repository.save(employee);

            // Get Leave Policy based on role
            LeavePolicy policy = leavePolicyRepository
                    .findByRole(employee.getRole())
                    .orElseThrow(() -> new RuntimeException("Leave policy not configured for role"));

            // Create Leave Balance using policy
            LeaveBalance balance = new LeaveBalance();

            balance.setEmployee(savedEmployee);
            balance.setCasualLeave(policy.getCasualLeave());
            balance.setSickLeave(policy.getSickLeave());
            balance.setPaidLeave(policy.getPaidLeave());

            leaveBalanceRepository.save(balance);

            return savedEmployee;
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

        // Update employee (Admin)
        public Employee updateEmployee(Long id, Employee updatedEmployee) {

            Employee existing = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            if (updatedEmployee.getEmail() != null) {
                existing.setEmail(updatedEmployee.getEmail());
            }

            if (updatedEmployee.getRole() != null) {
                existing.setRole(updatedEmployee.getRole());
            }

            if (updatedEmployee.getStatus() != null) {
                existing.setStatus(updatedEmployee.getStatus());
            }

            return repository.save(existing);
        }

        // Delete employee
        public void deleteEmployee(Long id) {
            repository.deleteById(id);
        }

        // Change employee status
        public Employee changeEmployeeStatus(Long id, EmployeeStatus status) {

            Employee employee = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            employee.setStatus(status);

            return repository.save(employee);
        }

       

        // Update employee profile
        public Employee updateProfile(Long employeeId,
                EmployeeProfileUpdateRequest request) {

        		Employee employee = repository.findById(employeeId)
        				.orElseThrow(() -> new RuntimeException("Employee not found"));

        		if (request.getPhoneNumber() != null) {
        			employee.setPhoneNumber(request.getPhoneNumber());
        		}

        		if (request.getAddress() != null) {
        			employee.setAddress(request.getAddress());
        		}

        		if (request.getEmergencyContact() != null) {
        			employee.setEmergencyContact(request.getEmergencyContact());
        		}

        		if (request.getGender() != null) {
        			employee.setGender(request.getGender());
        		}

        		if (request.getDob() != null) {
        			employee.setDob(request.getDob());
        		}


        		if (request.getProfilePic() != null) {
        			employee.setProfilePic(request.getProfilePic());
        		}

        		return repository.save(employee);
        }

        // Get all managers
        public List<Employee> getManagers() {
            return repository.findByRole(Role.MANAGER);
        }

        // Generate Employee ID
        public String generateEmployeeId(Role role) {

            String prefix;

            switch (role) {
                case ADMIN:
                    prefix = "ADMIN";
                    break;

                case MANAGER:
                    prefix = "MGR";
                    break;

                case EMPLOYEE:
                    prefix = "EMP";
                    break;

                default:
                    throw new RuntimeException("Invalid role");
            }

            List<Employee> employees = repository.findAll();

            int maxNumber = 0;

            for (Employee emp : employees) {

                if (emp.getEmployeeId() != null &&
                    emp.getEmployeeId().startsWith(prefix)) {

                    String numberPart = emp.getEmployeeId().replace(prefix, "");

                    int number = Integer.parseInt(numberPart);

                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                }
            }

            int nextNumber = maxNumber + 1;

            if (role == Role.EMPLOYEE) {
                return String.format("%s%04d", prefix, nextNumber);
            } else {
                return String.format("%s%03d", prefix, nextNumber);
            }
        }
        
        public List<Employee> searchEmployees(String keyword) {

            return repository.searchEmployees(keyword);

        }
        
        public List<Employee> getEmployeesByRole(Role role) {
            return repository.findByRole(role);
        }
        
        public void updateEmployee(Employee employee) {
            // Fetch the existing record so we never overwrite critical fields
            // (status, password, role, createdAt) with null values coming from a partial form submit
            Employee existing = repository.findById(employee.getId())
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employee.getId()));

            // Apply only the editable fields sent by the admin form
            if (employee.getFirstName() != null) existing.setFirstName(employee.getFirstName());
            if (employee.getLastName()  != null) existing.setLastName(employee.getLastName());
            if (employee.getEmail()     != null) existing.setEmail(employee.getEmail());
            if (employee.getPhoneNumber() != null) existing.setPhoneNumber(employee.getPhoneNumber());
            if (employee.getSalary()    != null) existing.setSalary(employee.getSalary());

            // Preserve status / password / role / createdAt — never overwrite with null
            // status is changed separately via changeEmployeeStatus()

            repository.save(existing);
        }
        
        
        
        public void assignAdmin(Long managerId, Long adminId){

            Employee manager = repository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));

            Employee admin = repository.findById(adminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            manager.setAssignedAdmin(admin);

            repository.save(manager);
        }
        
        // Assign manager
        public Employee assignManager(Long employeeId, Long managerId) {

            Employee employee = repository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            Employee manager = repository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));

            if (manager.getRole() != Role.MANAGER) {
                throw new RuntimeException("Assigned person is not a manager");
            }

            employee.setReportingManager(manager);

            return repository.save(employee);
        }
        
        public LeaveBalance getLeaveBalance(Long employeeId) {
        	return leaveBalanceRepository.findByEmployeeId(employeeId)
        	        .orElseGet(() -> createLeaveBalance(employeeId));
        }
        
        public Employee save(Employee employee) {
            return repository.save(employee);
        }
    }
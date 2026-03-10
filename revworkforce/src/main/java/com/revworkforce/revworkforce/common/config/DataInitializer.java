package com.revworkforce.revworkforce.common.config;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.revworkforce.revworkforce.admin.entity.Role;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.EmployeeStatus;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initAdmin(EmployeeRepository employeeRepository,
	                            PasswordEncoder passwordEncoder) {
	    return args -> {

	        if (employeeRepository.findByEmail("superadmin@rev.com").isEmpty()) {

	            Employee admin = new Employee();
	            
	            admin.setEmployeeId("ADMIN001"); 
	            admin.setFirstName("System");
	            admin.setLastName("Admin");
	            admin.setEmail("superadmin@rev.com");
	            admin.setPassword(passwordEncoder.encode("admin123"));
	            admin.setRole(Role.ADMIN);
	            admin.setStatus(EmployeeStatus.ACTIVE);
	            admin.setPhoneNumber("9999999999");
	            admin.setGender("OTHER");
	            admin.setSalary(Double.valueOf(0));
	            admin.setJoiningDate(LocalDate.now());

	            employeeRepository.save(admin);

	            System.out.println("Default Admin Created");

	            employeeRepository.save(admin);
	        }
	    };
	}
}
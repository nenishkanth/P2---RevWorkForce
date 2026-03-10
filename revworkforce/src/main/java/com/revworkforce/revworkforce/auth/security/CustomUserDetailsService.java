package com.revworkforce.revworkforce.auth.security;

import java.util.Collections;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.EmployeeStatus;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository repository;

    public CustomUserDetailsService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Employee employee = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(employee.getStatus() != EmployeeStatus.ACTIVE){
            throw new DisabledException("Account is inactive");
        }

        return new User(
                employee.getEmail(),
                employee.getPassword(),
                Collections.singleton(() -> "ROLE_" + employee.getRole().name())
        );
    }
}
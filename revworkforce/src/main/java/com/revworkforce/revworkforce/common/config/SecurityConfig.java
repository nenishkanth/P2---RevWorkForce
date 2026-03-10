package com.revworkforce.revworkforce.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.revworkforce.revworkforce.auth.security.LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;

    public SecurityConfig(UserDetailsService userDetailsService,
                          LoginSuccessHandler loginSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/",
                            "/login",
                            "/forgot-password",
                            "/verify-security",
                            "/reset-password",
                            "/css/**",
                            "/js/**"
                    ).permitAll()

                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/manager/**").hasRole("MANAGER")
                    .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                    .requestMatchers(
                            "/login",
                            "/auth/**",
                            "/css/**",
                            "/js/**"
                    ).permitAll()

                    .anyRequest().authenticated()
            )

            .formLogin(form -> form
                    .loginPage("/login")
                    .successHandler(loginSuccessHandler)
                    .permitAll()
            )

            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            )
            
            

            .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
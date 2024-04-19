package com.example.lettucemoddemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.lettucemoddemo.controller.Controller;
import com.example.lettucemoddemo.utils.JwtAuthFilter;
import com.example.lettucemoddemo.utils.UserInfoService;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        JwtAuthFilter jwtAuthFilter;

        @Autowired
        UserInfoService userInfoService;

        @SuppressWarnings("deprecation")
        @Bean
        public NoOpPasswordEncoder passwordEncoder() {
                return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable())

                                // .exceptionHandling(handling -> handling
                                //                 .authenticationEntryPoint(
                                //                                 (request, response, ex) -> {
                                //                                         response.sendError(
                                //                                                         HttpServletResponse.SC_UNAUTHORIZED,
                                //                                                         ex.getMessage());
                                //                                 }))
                                .authorizeHttpRequests(requests -> requests
                                                .requestMatchers("/loginUser").permitAll())
                                .authorizeHttpRequests(requests -> requests
                                                .requestMatchers("/**").authenticated())
                                .sessionManagement(management -> management
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                System.out.println("checking security filter chain config : " );
                return http.build();

        }


        // protected void configure(AuthenticationManagerBuilder auth, NoOpPasswordEncoder noOpPasswordEncoder)
        //                 throws Exception {

        //         System.out.println("auth manager builder called : __> " + auth.userDetailsService(username -> userInfoService.loadUserByUsername(username))
        //         .passwordEncoder(noOpPasswordEncoder));
        //         auth.userDetailsService(username -> userInfoService.loadUserByUsername(username))
        //                         .passwordEncoder(noOpPasswordEncoder);

        // }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

}

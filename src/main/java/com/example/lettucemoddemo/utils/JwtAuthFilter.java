package com.example.lettucemoddemo.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserInfoService userInfoService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("checking dofilter : ");
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        //System.out.println("checking authhead in dofilter : " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            //System.out.println("token reached here --> " + token);
            Boolean valid = jwtUtil.validateToken(token);
            System.out.println("\nvalidity --> " + valid);
            if (valid == false) {
                System.out.println("\ntoken is expired . go to /refreshToken\n");
            }
            else {
                username = jwtUtil.extractUsername(token);
                System.out.println("username in filter : " + username);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userInfoService.loadUserByUsername(username);
            List<String> authority = jwtUtil.extractAuthority(token);
            final Collection<? extends GrantedAuthority> authorities =
                authority.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            if (username.equals(userDetails.getUsername())) {
                System.out.println("\n using extract auth --> " + jwtUtil.extractAuthority(token) + "\n");
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                        userDetails.getPassword(),
                        authorities);
                System.out.println("authtoken (authenticated object) in filter --> " +  authToken);
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }
        filterChain.doFilter(request, response);
    }

}

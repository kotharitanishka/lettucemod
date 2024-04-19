package com.example.lettucemoddemo.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        if (username.equals("user") == false) {
            throw new UsernameNotFoundException(username);
        }
        UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username("user")
                        .password("54321")
                        .roles(roles.toArray(new String[0]))
                        .build();
        System.out.println("user details --> username : " + userDetails.getUsername());
        return userDetails;
    }
    
}

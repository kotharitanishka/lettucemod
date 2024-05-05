package com.example.lettucemoddemo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.lettucemoddemo.model.UserAuth;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.search.SearchResults;

import io.lettuce.core.RedisURI;

@Service
public class UserInfoService implements UserDetailsService {

    @Autowired 
    RSA rsa;

    // create modules client
    RedisModulesClient client = RedisModulesClient.create(RedisURI.create("localhost", 6379));

    // connect to redis server
    StatefulRedisModulesConnection<String, String> connection = client.connect();

    // Obtain the command API for synchronous execution.
    RedisModulesCommands<String, String> commands = connection.sync();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuth userAuth = new UserAuth();
        String query = "@username:" + username;
        SearchResults<String, String> ans = commands.ftSearch("uidx", query);
        
        String answer = ans.get(0).values().toArray()[0].toString();
        try {
            userAuth = new ObjectMapper().readValue(answer, UserAuth.class);
            System.out.println("userauth --> "+ userAuth.getPassword());

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        if (username.equals(userAuth.getUsername()) == false) {
            throw new UsernameNotFoundException(username);
        }
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(userAuth.getUsername())
                .password(userAuth.getPassword())
                .roles(userAuth.getAuthority())
                .build();
        System.out.println("user details --> username : " + userDetails.getUsername());
        return userDetails;
    }

}

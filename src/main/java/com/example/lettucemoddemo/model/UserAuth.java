package com.example.lettucemoddemo.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "username" , "password" , "authority"})
public class UserAuth {

    private String id ;

    private String username ;

    private String password ;

    private String authority ;

    public UserAuth() {
    }

    public UserAuth(String id , String username, String password, String authority) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authority = authority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}

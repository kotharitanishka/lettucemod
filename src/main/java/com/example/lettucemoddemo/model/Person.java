package com.example.lettucemoddemo.model;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.NonNull;

public class Person {

    @NonNull
    private String id; 

    private String name ;

    private Integer age ;

    private String createdBy ; 

    private String createdOn ;

    private String updatedBy ; 

    private String updatedOn ;  

    Map<String, Object> detail = new HashMap<>();

    public Person() {
    }

    public Person(String id, String name, Integer age , String createdBy , String createdOn , String updatedBy , String updatedOn ) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
        // this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    @JsonAnySetter
    public void setDetail(String key, Object value) {
        detail.put(key, value);
    }

    public Map<String, Object> getDetail() {
        return detail;
    }

    // public JsonNode getDetails() {
    //     return details;
    // }

    // public void setDetails(JsonNode details) {
    //     this.details = details;
    // }
    
}

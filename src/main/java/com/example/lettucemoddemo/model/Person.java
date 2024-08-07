package com.example.lettucemoddemo.model;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@JsonPropertyOrder({"id" , "name" , "age" , "mobNo", "dob", "detail" , "createdBy0" , "createdOn0" , "updatedBy0" , "updatedOn0" , "active0"})
public class Person {

    
    private String id; 

    private String name ;

    @Min (value = 18 , message = "age must be above 18")
    private Integer age ;

    @Size (min= 10 , max= 10 , message = "mob no length should be 10")
    private String mobNo;

    private String dob;

    Map<String, Object> detail = new HashMap<>();
    
    private String createdBy0 ; 

    private String createdOn0 ;

    private String updatedBy0 ; 

    private String updatedOn0 ;  

    private boolean active0;

    public Person() {
    }

    public Person(String id, String name, Integer age ,String mobNo , String dob, String createdBy0 , String createdOn0 , String updatedBy0 , String updatedOn0 , boolean active0) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.mobNo = mobNo;
        this.dob = dob;
        this.createdBy0 = createdBy0;
        this.createdOn0 = createdOn0;
        this.updatedBy0 = updatedBy0;
        this.updatedOn0 = updatedOn0;
        this.active0 = active0;
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

    public String getMobNo() {
        return mobNo;
    }

    public void setMobNo(String mobNo) {
        this.mobNo = mobNo;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getcreatedBy0() {
        return createdBy0;
    }

    public void setcreatedBy0(String createdBy0) {
        this.createdBy0 = createdBy0;
    }

    public String getcreatedOn0() {
        return createdOn0;
    }

    public void setcreatedOn0(String createdOn0) {
        this.createdOn0 = createdOn0;
    }

    public String getupdatedBy0() {
        return updatedBy0;
    }

    public void setupdatedBy0(String updatedBy0) {
        this.updatedBy0 = updatedBy0;
    }

    public String getupdatedOn0() {
        return updatedOn0;
    }

    public void setupdatedOn0(String updatedOn0) {
        this.updatedOn0 = updatedOn0;
    }

    @JsonAnySetter
    public void setDetail(String key, Object value) {
        detail.put(key, value);
    }

    public Map<String, Object> getDetail() {
        return detail;
    }

    public boolean isactive0() {
        return active0;
    }

    public void setactive0(boolean active0) {
        this.active0 = active0;
    }


    // public JsonNode getDetails() {
    //     return details;
    // }

    // public void setDetails(JsonNode details) {
    //     this.details = details;
    // }
    
}

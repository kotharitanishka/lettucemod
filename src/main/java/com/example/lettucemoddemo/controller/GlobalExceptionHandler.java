package com.example.lettucemoddemo.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        // Create a map of field errors
        // Return appropriate error response
        
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("error", HttpStatus.BAD_REQUEST);
        map.put("message", ex.getFieldError().getDefaultMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(map);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleException(HttpMessageNotReadableException exception, HttpServletRequest request) {
        //System.out.println(exception.getMessage());
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("error", HttpStatus.BAD_REQUEST);
        System.out.println("\n");
        map.put("message", exception.getMessage());
        //exception.getLocalizedMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(map);
        
    }

    
        //return new ResponseEntity<String>("error", HttpStatus.INTERNAL_SERVER_ERROR);
    }


package com.example.lettucemoddemo.utils;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


public class JwtUtil {

    private String SECRET = "sa134902eeowdksape24sa134902eeowdksape24sa134902eeowdksape24sa134902eeowdksape24sa134902eeowdksape24sa134902eeowdksape24sa134902eeowdksape24sa134902eeowdksape24sa134902eeowdksape24";
    private long EXPIRATION_TIME = 864_000_000; // 10 days

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}

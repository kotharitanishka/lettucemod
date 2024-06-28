package com.example.lettucemoddemo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    long accessExpTime = 1000 * 60 * 1;
    long refreshExpTime = 1000 * 60 * 5;


    // @Value("${secret.aes-key}")
    // private String SECRET ;

    // private Key getSignKey() {
    // byte[] keyBytes = Decoders.BASE64.decode(SECRET);
    // return Keys.hmacShaKeyFor(keyBytes);
    // }

    // public String generateToken(String username) {
    // return Jwts.builder()
    // .setSubject(username)
    // .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 20))
    // .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    // }

    // public String extractUsername(String token) {
    // return Jwts.parserBuilder()
    // .setSigningKey(getSignKey())
    // .build()
    // .parseClaimsJws(token)
    // .getBody()
    // .getSubject();
    // }

    // private String SECRET =
    // "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    @Value("${secret.private-key}")
    private String secretPrivateKey ;

    @Value("${secret.public-key}")
    private String secretPublicKey ;

    public PublicKey generateJwtKeyDecryption() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Decoders.BASE64.decode(secretPublicKey);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }

    public PrivateKey generateJwtKeyEncryption() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Decoders.BASE64.decode(secretPrivateKey);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }

    public String generateToken(String username, Long time , Map<String, Object> claims) {
        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setExpiration(new Date(System.currentTimeMillis() + time))
                    .signWith(generateJwtKeyEncryption(), SignatureAlgorithm.RS256).compact();
            
            System.out.println(" token gen time ---> " + new Date(System.currentTimeMillis()));
            System.out.println(" token valid till time ---> " + new Date(System.currentTimeMillis() + time));

            //System.out.println(extractAuthority(token));
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> claims(String username , Boolean isJwt) {
        Map<String, Object> claims = new HashMap<>() ;
        if (isJwt == true) {
            Collection<? extends GrantedAuthority> role;
            UserInfoService userInfoService = new UserInfoService();
            UserDetails userDetails ;
            userDetails = userInfoService.loadUserByUsername(username);
            role = userDetails.getAuthorities();
            String auth = role.stream().toArray()[0].toString();
            claims.put("Roles", auth);
        }
        return claims;
    }

    public String generateAccessToken(String username) {
        System.out.println("genarating access token --> nothing with print dw");
        System.out.println(claims(username , true));
        Map<String, Object> claims = claims(username , true);
        return generateToken(username, accessExpTime , claims);
    }

    public String generateRefreshToken(String username) {
        System.out.println("genarating refresh token --> ");
        System.out.println(claims(username , false));
        Map<String, Object> claims = claims(username , false);
        return generateToken(username, refreshExpTime , claims);
    }

    public String extractUsername(String token) {
        try {
            String username = Jwts.parserBuilder()
                    .setSigningKey(generateJwtKeyDecryption())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return username;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> extractAuthority(String token) {
        //Collection<? extends GrantedAuthority> r;
        List<String> answer = new ArrayList<>();
        try {
            Claims o = Jwts.parserBuilder()
                    .setSigningKey(generateJwtKeyDecryption())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object r =  o.get("Roles");
            System.out.println("\n\n"+ r + "\n\n");
            if (r != null) {
                answer.add(r.toString());
                return answer;
            }
            return answer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean validateToken(String token) {

        long exptime;
        long currentTime;
        try {
            exptime = Jwts.parserBuilder()
                    .setSigningKey(generateJwtKeyDecryption())
                    .build()
                    .parseClaimsJws(token)
                    .getBody().getExpiration().getTime();
            System.out.println("exp time = --> " + exptime);
            currentTime = new Date(System.currentTimeMillis()).getTime();
            System.out.println("check expiry validate token ---> " + currentTime);
            if (currentTime < exptime) {
                System.out.println("\ntoken is valid\n");
                return true;
            } else {
                System.out.println("\nfalse : token is valid\n");
                return false;
            }
        } catch (Exception e) {
            System.out.println("\ntoken is not valid\n");
            //e.printStackTrace();
            
            return false;
        }
    }

}

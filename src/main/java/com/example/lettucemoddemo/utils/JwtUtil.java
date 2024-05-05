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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {



    private String secretPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtEmrrVOiFNiYbl/B2MopjEOfO8sxE7KboQsxnuYLcg6zoSWRtrxI3+Z2Tzltjx7ULeaSp5lG59LgPOQmQZ5S2hQdX0OIUHCP3MLPHB4a6pO2b7qjUXRqVjT1N8jZZIqXJBb1HdhvDSxvAkIY3Rohsfwzk25r9AlmqamffUndg6wsDPLuQAJdgiA9lgsumlhZdM+YBaGcKzHip6WOmYttXyjuD+1Z7z3GzAkuXtdlpvoC/D8p+VndB22sMQ8ZJOqsNfmCk7jIeCzS7daVfDh+lHtBSgi+Nb4NTh8oZwl3SW05hpL043VPXE79R+XTAv9TdtN8Do3Gri4q9Ax8IcphGQIDAQAB";
    private String secretPrivateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC0SautU6IU2JhuX8HYyimMQ587yzETspuhCzGe5gtyDrOhJZG2vEjf5nZPOW2PHtQt5pKnmUbn0uA85CZBnlLaFB1fQ4hQcI/cws8cHhrqk7ZvuqNRdGpWNPU3yNlkipckFvUd2G8NLG8CQhjdGiGx/DOTbmv0CWapqZ99Sd2DrCwM8u5AAl2CID2WCy6aWFl0z5gFoZwrMeKnpY6Zi21fKO4P7VnvPcbMCS5e12Wm+gL8Pyn5Wd0HbawxDxkk6qw1+YKTuMh4LNLt1pV8OH6Ue0FKCL41vg1OHyhnCXdJbTmGkvTjdU9cTv1H5dMC/1N203wOjcauLir0DHwhymEZAgMBAAECggEAFrEHE05pd2eOQqYkHIh6vkAXSfat0KWJrQq0TiRKNdWKil0o1UUm2aIvgJKL1ZhdwFaQXYPknB0QmC68b0SMImKnEmx05cWlIHYXN0TFbSz2U2MRrJjodr08+APRnjP2CKwoYdScoEiCwwKqunl62uS8C8cO01ezkjsv6xxhmHGGWUNKNAzb6klv8AEMzxSZKvY1Qhslzz/c8mlaNsleKUvJ74c3QIPvGVnjntuElL1MfyZTG7IFQbGUl0lKUR5CMVEHnWBMFRzmR3QPjrhbnCKlSNEIdSNuRcwPJFVfwqm57Fo74CEAVcuzeLZ336IVjU4C0o5Q6nPC6tlwPqAheQKBgQD9lI7pL8gjCjtWyZSc0OY+QOVwJcnlIuyG/Vk1HaaONjqFLP5g+BRUFQGJQgL/lZ3gt6/fUFojVSDMXh2ECBmbhoLBFPz2eFi918kexilI6yjBBZPFsSmamUXHMrLDDEg8XKl93k9qwwvDwNJSAkrN1lC4CEsjIgmHoGDOwt0J4wKBgQC2AhMYotjCBAdC00051Ck0+ctnPR9I9/913wc+2fGrlEtacfh3PwYJ1ngQ7UTYxDBrH/Q14vVkYQDO6fRzNZXu6VZ92J1cRQcFFOdx8ul13lfmBJONgLbafegFGaHeYjdydD89Tcjq3SAYRAAZV3XltesFDpApKWNugadm20jJ0wKBgQCFRuhZ3Rg2enE3qxkl1pG81IUCjYnwmYtxY4TYau4YPp5BUqWcN6cPjj7RclV7GZrx+flAyM2nVl23rBudQlibKnZcnvhZXmVd2iVFvGcEgTxZPuIs/HFeZQKMsLPN2g25VAZtNkPNXBg52YVxPu0sBGFGaG19WK1jkEJxSW+9ewKBgQCFSQWk//ROOFb85/Jgy+6VPexnoQodDIfGUNpke4QQC1wXUukKoluQrr9kXe0R9LIkcNUM9TSRGrxVylxaVj6DI9tT82JhZKdcYE+RUbsHtxiVeHp5oG3HmXZk7b1tNm22YkzBXwoofGB5hKqx5CuRWeRO4rhNfGK1VTs7BpDKQwKBgGJdeRNfJLgRGvLaHqeB8qV1oFrNZdVLwuw9WGxe4e/Z3Lx+P9NL/SN7hPI9VDS8eGkuf6UF+6XZs/Z/xKjpJrhXl7te5xI4oijjGUel0P4F4Z/9gN7JUy9AfJ6rQfJPD5Nk3eIPN5ObKr2GR53hju/sc8Fac//kBorUPToZGwUQ";
    //private String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    long accessExpTime = 1000 * 60 * 1;
    long refreshExpTime = 1000 * 60 * 5;

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
            //System.out.println("token ---> " + token);
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
        System.out.println("genarating access token --> ");
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
            e.printStackTrace();
            
            return false;
        }
    }

}

package com.example.lettucemoddemo.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.context.annotation.Configuration;
import io.jsonwebtoken.io.Decoders;

@Configuration
public class RSA {

    private KeyFactory keyFactory;
    private Cipher encryptCipher;
    Cipher decryptCipher;


    public RSA() {
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            encryptCipher = Cipher.getInstance("RSA");
            decryptCipher = Cipher.getInstance("RSA");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String secretPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtEmrrVOiFNiYbl/B2MopjEOfO8sxE7KboQsxnuYLcg6zoSWRtrxI3+Z2Tzltjx7ULeaSp5lG59LgPOQmQZ5S2hQdX0OIUHCP3MLPHB4a6pO2b7qjUXRqVjT1N8jZZIqXJBb1HdhvDSxvAkIY3Rohsfwzk25r9AlmqamffUndg6wsDPLuQAJdgiA9lgsumlhZdM+YBaGcKzHip6WOmYttXyjuD+1Z7z3GzAkuXtdlpvoC/D8p+VndB22sMQ8ZJOqsNfmCk7jIeCzS7daVfDh+lHtBSgi+Nb4NTh8oZwl3SW05hpL043VPXE79R+XTAv9TdtN8Do3Gri4q9Ax8IcphGQIDAQAB";
    private String secretPrivateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC0SautU6IU2JhuX8HYyimMQ587yzETspuhCzGe5gtyDrOhJZG2vEjf5nZPOW2PHtQt5pKnmUbn0uA85CZBnlLaFB1fQ4hQcI/cws8cHhrqk7ZvuqNRdGpWNPU3yNlkipckFvUd2G8NLG8CQhjdGiGx/DOTbmv0CWapqZ99Sd2DrCwM8u5AAl2CID2WCy6aWFl0z5gFoZwrMeKnpY6Zi21fKO4P7VnvPcbMCS5e12Wm+gL8Pyn5Wd0HbawxDxkk6qw1+YKTuMh4LNLt1pV8OH6Ue0FKCL41vg1OHyhnCXdJbTmGkvTjdU9cTv1H5dMC/1N203wOjcauLir0DHwhymEZAgMBAAECggEAFrEHE05pd2eOQqYkHIh6vkAXSfat0KWJrQq0TiRKNdWKil0o1UUm2aIvgJKL1ZhdwFaQXYPknB0QmC68b0SMImKnEmx05cWlIHYXN0TFbSz2U2MRrJjodr08+APRnjP2CKwoYdScoEiCwwKqunl62uS8C8cO01ezkjsv6xxhmHGGWUNKNAzb6klv8AEMzxSZKvY1Qhslzz/c8mlaNsleKUvJ74c3QIPvGVnjntuElL1MfyZTG7IFQbGUl0lKUR5CMVEHnWBMFRzmR3QPjrhbnCKlSNEIdSNuRcwPJFVfwqm57Fo74CEAVcuzeLZ336IVjU4C0o5Q6nPC6tlwPqAheQKBgQD9lI7pL8gjCjtWyZSc0OY+QOVwJcnlIuyG/Vk1HaaONjqFLP5g+BRUFQGJQgL/lZ3gt6/fUFojVSDMXh2ECBmbhoLBFPz2eFi918kexilI6yjBBZPFsSmamUXHMrLDDEg8XKl93k9qwwvDwNJSAkrN1lC4CEsjIgmHoGDOwt0J4wKBgQC2AhMYotjCBAdC00051Ck0+ctnPR9I9/913wc+2fGrlEtacfh3PwYJ1ngQ7UTYxDBrH/Q14vVkYQDO6fRzNZXu6VZ92J1cRQcFFOdx8ul13lfmBJONgLbafegFGaHeYjdydD89Tcjq3SAYRAAZV3XltesFDpApKWNugadm20jJ0wKBgQCFRuhZ3Rg2enE3qxkl1pG81IUCjYnwmYtxY4TYau4YPp5BUqWcN6cPjj7RclV7GZrx+flAyM2nVl23rBudQlibKnZcnvhZXmVd2iVFvGcEgTxZPuIs/HFeZQKMsLPN2g25VAZtNkPNXBg52YVxPu0sBGFGaG19WK1jkEJxSW+9ewKBgQCFSQWk//ROOFb85/Jgy+6VPexnoQodDIfGUNpke4QQC1wXUukKoluQrr9kXe0R9LIkcNUM9TSRGrxVylxaVj6DI9tT82JhZKdcYE+RUbsHtxiVeHp5oG3HmXZk7b1tNm22YkzBXwoofGB5hKqx5CuRWeRO4rhNfGK1VTs7BpDKQwKBgGJdeRNfJLgRGvLaHqeB8qV1oFrNZdVLwuw9WGxe4e/Z3Lx+P9NL/SN7hPI9VDS8eGkuf6UF+6XZs/Z/xKjpJrhXl7te5xI4oijjGUel0P4F4Z/9gN7JUy9AfJ6rQfJPD5Nk3eIPN5ObKr2GR53hju/sc8Fac//kBorUPToZGwUQ";

    public PublicKey generatePublicKeyEncryption() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Decoders.BASE64.decode(secretPublicKey);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }

    public PrivateKey generatePrivateKeyDecryption() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Decoders.BASE64.decode(secretPrivateKey);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }

    public String encrypt(String normalPassword)
            throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, InvalidKeySpecException {
        encryptCipher.init(Cipher.ENCRYPT_MODE, generatePublicKeyEncryption());
        byte[] secretMessageBytes = normalPassword.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
        System.out.println(Base64.getDecoder().decode(encodedMessage) == encryptedMessageBytes);
        System.out.println("encrypt in rsa is working --->" + encodedMessage);
        return encodedMessage;
    }

    public String decrypt(String encodedPassword)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException,
            BadPaddingException {
        System.out.println("encoded pass in RSA file is --> " + encodedPassword);
        decryptCipher.init(Cipher.DECRYPT_MODE, generatePrivateKeyDecryption());
        byte[] b = Base64.getDecoder().decode(encodedPassword);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(b);
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        System.out.println("is the password same ?? --> " + decryptedMessage);
        return decryptedMessage;

    }

}

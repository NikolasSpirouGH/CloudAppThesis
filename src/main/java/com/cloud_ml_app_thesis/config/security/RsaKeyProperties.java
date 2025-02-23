package com.cloud_ml_app_thesis.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@PropertySource("classpath:application.yaml")
public class RsaKeyProperties {

    @Value("${rsa.public.key-location")
    private String publicKeyPath;

    @Value("${rsa.private.key-location}")
    private String privateKeyPath;

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    public RSAPublicKey getPublicKey() {
        if(publicKey == null){
            publicKey = loadPublicKey();
        }
        return publicKey;
    }
    public RSAPrivateKey getPrivateKey(){
        if(privateKey == null){
            privateKey = loadPrivateKey();
        }
        return privateKey;
    }

    private RSAPublicKey loadPublicKey(){
        try{
            String key = Files.readString(Paths.get(publicKeyPath.replace("classpath:", "src/main/resources/")));
            String publicKeyContent = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(publicKeyContent);

            X509EncodedKeySpec  keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey)  keyFactory.generatePublic(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new RuntimeException("Failed to load RSA public key", e);
        }
    }

    private RSAPrivateKey loadPrivateKey(){
        try{
            String key = Files.readString(Paths.get("classpath:", "src/main/resources/"));
            String privateKeyContent = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(privateKeyContent);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch(IOException | NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new RuntimeException("Failed to load RSA private key", e);
        }
    }
}

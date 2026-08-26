package org.kamal.userservice.security;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.kamal.userservice.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("classpath:keys/private_key.pem")
    private Resource privateKeyResource;

    private PrivateKey privateKey;

    @PostConstruct
    public void init() throws Exception{
        String key = new String(privateKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s","");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(keySpec);
    }

    private final long expirationMs = 1000 * 60 * 60;

    public String generateToken(String email, Role role, Long userId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() +  expirationMs))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }
}

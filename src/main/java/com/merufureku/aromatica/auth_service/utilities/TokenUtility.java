package com.merufureku.aromatica.auth_service.utilities;

import com.merufureku.aromatica.auth_service.config.KeyConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenUtility {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final KeyConfig keyConfig;

    public TokenUtility(KeyConfig keyConfig) {
        this.keyConfig = keyConfig;
    }

    public String generateToken(String jti, Integer userId, String role){

        logger.info("Generating token for {}", userId);

        var token = Jwts.builder()
                .setId(jti)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 mins
                .signWith(SignatureAlgorithm.HS256, keyConfig.getJwtSecretKey())
                .compact();

        logger.info("Generate Token Success!: {}", token);

        return token;
    }

    public String getUsername(String token){
        return Jwts.parser()
                .setSigningKey(keyConfig.getJwtSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(keyConfig.getJwtSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

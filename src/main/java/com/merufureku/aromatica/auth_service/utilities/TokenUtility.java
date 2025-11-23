package com.merufureku.aromatica.auth_service.utilities;

import com.merufureku.aromatica.auth_service.config.KeyConfig;
import com.merufureku.aromatica.auth_service.dto.token.ParsedTokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static com.merufureku.aromatica.auth_service.constants.AuthConstants.REFRESH_TOKEN;

@Component
public class TokenUtility {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final KeyConfig keyConfig;

    public TokenUtility(KeyConfig keyConfig) {
        this.keyConfig = keyConfig;
    }

    public String generateToken(String jti, Integer userId, String type, String role){

        logger.info("Generating token for {}", userId);

        var secretKeyString = type.equals(REFRESH_TOKEN) ?
                keyConfig.getJwtRefreshSecretKey() :
                keyConfig.getJwtAccessSecretKey();

        var keyBytes = Base64.getDecoder().decode(secretKeyString);
        var secretKey = Keys.hmacShaKeyFor(keyBytes);

        var expirationMillis = type.equals(REFRESH_TOKEN)
                ? 7L * 24 * 60 * 60 * 1000  // 7 days
                : 15L * 60 * 1000;          // 15 minutes

        var token = Jwts.builder()
                .setId(jti)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();


        logger.info("Generate Token Success!: {}", token);

        return token;
    }

    public Claims parseToken(String token) {

        var secretKey = Keys.hmacShaKeyFor(Base64.getDecoder()
                .decode(keyConfig.getJwtAccessSecretKey()));

        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public ParsedTokenInfo parseAndValidateToken(String token) {
        var claims = parseToken(token);
        var jti = claims.getId();
        var userId = claims.get("userId", Integer.class);

        return new ParsedTokenInfo(userId, jti);
    }
}

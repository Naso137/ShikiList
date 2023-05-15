package com.naso.restapi.security.jwt;

import java.util.Date;

import com.naso.restapi.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;

@Component
public class JwtUtils {
    @Value("${jwtAccessSecret}")
    private String jwtAccessSecret;
    @Value("${jwtRefreshSecret}")
    private String jwtRefreshSecret;
    @Value("${jwtAccessExpirationMs}")
    private int jwtAccessExpirationMs;
    @Value("${jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;

    public String generateAccessToken(User user) {
        return generateTokenFromMail(user.getMail(), jwtAccessSecret, jwtAccessExpirationMs);
    }

    public String generateRefreshToken(User user) {
        return generateTokenFromMail(user.getMail(), jwtRefreshSecret, jwtRefreshExpirationMs);
    }

    public boolean validateAccessJwtToken(String authToken) throws SignatureException, MalformedJwtException, ExpiredJwtException,
            UnsupportedJwtException, IllegalArgumentException {
        Jwts.parser().setSigningKey(jwtAccessSecret).parseClaimsJws(authToken);
        return true;
    }

    public boolean validateRefreshJwtToken(String authToken) throws SignatureException, MalformedJwtException, ExpiredJwtException,
            UnsupportedJwtException, IllegalArgumentException {
        Jwts.parser().setSigningKey(jwtRefreshSecret).parseClaimsJws(authToken);
        return true;
    }

    public String getProfileMailFromAccessJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtAccessSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public Authentication getJwt() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    private String generateTokenFromMail(String mail, String secret, int expiration) {
        return Jwts.builder().setSubject(mail).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration)).signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}

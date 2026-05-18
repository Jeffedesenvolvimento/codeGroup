package br.com.codegroup.service;

import br.com.codegroup.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Getter
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withClaim("userId", user.getId())
                .withSubject(user.getUsuario())
                .withExpiresAt(Instant.now().plusSeconds(expiration))
                .withIssuedAt(Instant.now())
                .sign(algorithm);
    }

    public String extractUsername(String token) {
        return verifyAndDecode(token).getSubject();
    }

    public Long extractUserId(String token) {
        return verifyAndDecode(token).getClaim("userId").asLong();
    }

    public boolean isTokenValid(String token) {
        try {
            verifyAndDecode(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    // método privado central — valida assinatura e expiração
    private DecodedJWT verifyAndDecode(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .build()
                .verify(token); // lança JWTVerificationException se inválido ou expirado
    }
}
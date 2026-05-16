package br.com.codegroup.service;

import br.com.codegroup.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
        return JWT.create().withClaim("userId", user.getId())
                .withSubject(user.getUsuario())
                .withExpiresAt(Instant.now().plusSeconds(expiration))
                .withIssuedAt(Instant.now())
                .sign(algorithm);
    }

}

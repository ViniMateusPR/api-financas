package br.com.viniciusfinancas.financas.infra.security;

import br.com.viniciusfinancas.financas.domain.user.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;
    public String generateToken(User user) {
        try {
            Algorithm algorith = Algorithm.HMAC256(secret);

            String token = JWT.create()
                    .withIssuer("financas")
                    .withSubject(user.getEmail())
                    .withExpiresAt()
                    .sign(algorith);
            return token;
        }catch (JWTCreationException exception) {
            throw new RuntimeException("Error while authenticating");
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}

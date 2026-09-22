package com.uade.ecommerce.security;

import com.uade.ecommerce.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

    @Value("${JWT_SECRET:}")
    private String secretoConfigurado;

    private SecretKey clave;

    @PostConstruct
    void inicializarClave() {
        if (secretoConfigurado.isBlank()) {
            clave = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            LOGGER.warn("JWT_SECRET no está configurado: los tokens dejarán de ser válidos al reiniciar la aplicación");
        } else {
            clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoConfigurado));
        }
    }

    public String generarToken(Usuario usuario) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(usuario.getMail())
                .setIssuedAt(new Date(ahora))
                .setExpiration(new Date(ahora + 3_600_000))
                .signWith(clave, SignatureAlgorithm.HS256)
                .compact();
    }

    public String obtenerMail(String token) {
        Claims datos = Jwts.parserBuilder()
                .setSigningKey(clave)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return datos.getSubject();
    }
}

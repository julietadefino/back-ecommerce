package com.uade.ecommerce.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {

    public static final String COOKIE_NAME = "trama_access_token";

    @Value("${app.auth.cookie-secure:true}")
    private boolean secure;

    public ResponseCookie crear(String token) {
        return construir(token, Duration.ofHours(1));
    }

    public ResponseCookie eliminar() {
        return construir("", Duration.ZERO);
    }

    private ResponseCookie construir(String valor, Duration duracion) {
        return ResponseCookie.from(COOKIE_NAME, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/api")
                .maxAge(duracion)
                .build();
    }
}
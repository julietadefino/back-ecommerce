package com.uade.ecommerce.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class CsrfController {

    public record CsrfRespuestaDTO(
            String token,
            String headerName
    ) {}

    @GetMapping("/csrf")
    public ResponseEntity<CsrfRespuestaDTO> obtenerToken(CsrfToken csrfToken) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new CsrfRespuestaDTO(
                        csrfToken.getToken(),
                        csrfToken.getHeaderName()
                ));
    }
}
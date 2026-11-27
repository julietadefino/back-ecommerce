package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.LoginDTO;
import com.uade.ecommerce.dto.LoginRespuestaDTO;
import com.uade.ecommerce.dto.UsuarioRegistroDTO;
import com.uade.ecommerce.dto.UsuarioRespuestaDTO;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.security.AuthCookieService;
import com.uade.ecommerce.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthCookieService authCookieService;

    public record SesionRespuestaDTO(
            UsuarioRespuestaDTO usuario,
            String access
    ) {}

    @GetMapping
    public ResponseEntity<List<UsuarioRespuestaDTO>> getAll() {
        return ResponseEntity.ok(usuarioService.listarRespuestas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRespuestaDTO> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(usuarioService.buscarRespuesta(id));
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioRespuestaDTO> registrar(
            @Valid @RequestBody UsuarioRegistroDTO datos
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.registrar(datos));
    }

    @PostMapping("/login")
    public ResponseEntity<SesionRespuestaDTO> login(
            @Valid @RequestBody LoginDTO datos
    ) {
        LoginRespuestaDTO resultado = usuarioService.login(datos);

        // El JWT viaja solamente en la cookie HttpOnly.
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        authCookieService.crear(resultado.token()).toString()
                )
                .body(new SesionRespuestaDTO(
                        resultado.usuario(),
                        null
                ));
    }

    @GetMapping("/me")
    public ResponseEntity<SesionRespuestaDTO> sesionActual(
            @AuthenticationPrincipal Usuario usuario
    ) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(new SesionRespuestaDTO(
                UsuarioRespuestaDTO.fromEntity(usuario),
                usuario.getRol() == null
                        ? "USUARIO"
                        : usuario.getRol().name()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        authCookieService.eliminar().toString()
                )
                .build();
    }
}
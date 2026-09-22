package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.LoginDTO;
import com.uade.ecommerce.dto.LoginRespuestaDTO;
import com.uade.ecommerce.dto.UsuarioRegistroDTO;
import com.uade.ecommerce.dto.UsuarioRespuestaDTO;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.service.UsuarioService;
import com.uade.ecommerce.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uade.ecommerce.exception.ApiException;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    public List<UsuarioRespuestaDTO> getAll() {
        return usuarioService.getAll()
                .stream()
                .map(UsuarioRespuestaDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioRespuestaDTO getById(
            @PathVariable Long id
    ) {
        Usuario usuario = usuarioService.getById(id)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Usuario no encontrado"
                        )
                );

        return UsuarioRespuestaDTO.fromEntity(usuario);
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioRespuestaDTO> registrar(
            @Valid @RequestBody UsuarioRegistroDTO datos
    ) {
        Usuario usuario = Usuario.builder()
                .nombreUsuario(datos.getNombreUsuario())
                .mail(datos.getMail())
                .contrasenia(datos.getContrasenia())
                .nombre(datos.getNombre())
                .apellido(datos.getApellido())
                .build();

        Usuario registrado =
                usuarioService.registrar(usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UsuarioRespuestaDTO.fromEntity(registrado));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespuestaDTO> login(
            @Valid @RequestBody LoginDTO datos
    ) {
        Usuario usuario = usuarioService.login(
                datos.getMail(),
                datos.getContrasenia()
        );

        return ResponseEntity.ok(new LoginRespuestaDTO(
                jwtService.generarToken(usuario),
                "Bearer",
                UsuarioRespuestaDTO.fromEntity(usuario)
        ));
    }
}

package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.LoginDTO;
import com.uade.ecommerce.dto.LoginRespuestaDTO;
import com.uade.ecommerce.dto.UsuarioRegistroDTO;
import com.uade.ecommerce.dto.UsuarioRespuestaDTO;
import com.uade.ecommerce.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

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
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.registrar(datos));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespuestaDTO> login(
            @Valid @RequestBody LoginDTO datos
    ) {
        return ResponseEntity.ok(usuarioService.login(datos));
    }
}

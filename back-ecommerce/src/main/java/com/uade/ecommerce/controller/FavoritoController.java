package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.FavoritoDTO;
import com.uade.ecommerce.service.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<FavoritoDTO>> listarPorUsuario(
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(
                favoritoService.listarFavoritosPorUsuario(usuarioId)
        );
    }

    @PostMapping
    public ResponseEntity<FavoritoDTO> agregarFavorito(
            @RequestParam Long usuarioId,
            @RequestParam Long productoId
    ) {
        FavoritoDTO nuevoFavorito =
                favoritoService.agregarFavorito(usuarioId, productoId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevoFavorito);
    }

    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<Void> vaciarFavoritos(
            @PathVariable Long usuarioId
    ) {
        favoritoService.vaciarFavoritos(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
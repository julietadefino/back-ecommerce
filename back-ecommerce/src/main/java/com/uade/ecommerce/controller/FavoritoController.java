package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.FavoritoDTO;
import com.uade.ecommerce.service.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<FavoritoDTO>> listarFavoritos(@PathVariable Long usuarioId) {
        List<FavoritoDTO> favoritos = favoritoService.listarFavoritosPorUsuario(usuarioId);
        return ResponseEntity.ok(favoritos); // favoritos del usuario
    }

    @PostMapping
    public ResponseEntity<FavoritoDTO> agregarFavorito(
            @RequestParam Long usuarioId, 
            @RequestParam Long productoId
    ) {
        FavoritoDTO nuevoFavorito = favoritoService.agregarFavorito(usuarioId, productoId);
        return new ResponseEntity<>(nuevoFavorito, HttpStatus.CREATED);
    }

    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<Void> vaciarFavoritos(@PathVariable Long usuarioId) {
        favoritoService.vaciarFavoritos(usuarioId);
        return ResponseEntity.noContent().build();
    }
}

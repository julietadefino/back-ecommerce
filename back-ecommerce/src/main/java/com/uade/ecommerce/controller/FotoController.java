package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.AgregarFotosDTO;
import com.uade.ecommerce.dto.FotoRespuestaDTO;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.service.FotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión de las fotos de un producto, aislada del controlador de
 * productos. Las rutas cuelgan del producto porque una foto no existe
 * por fuera de él.
 */
@RestController
@RequestMapping("/api/productos/{productoId}/fotos")
public class FotoController {

    @Autowired
    private FotoService fotoService;

    @GetMapping
    public ResponseEntity<List<FotoRespuestaDTO>> listar(
            @PathVariable Long productoId
    ) {
        return ResponseEntity.ok(
                FotoRespuestaDTO.fromEntities(
                        fotoService.listar(productoId)
                )
        );
    }

    @PostMapping
    public ResponseEntity<List<FotoRespuestaDTO>> agregar(
            @PathVariable Long productoId,
            @Valid @RequestBody AgregarFotosDTO datos
    ) {
        Producto producto = fotoService.agregarFotos(
                productoId,
                datos.getUsuarioId(),
                datos.getFotos()
        );

        return ResponseEntity.ok(
                FotoRespuestaDTO.fromEntities(producto.getFotos())
        );
    }

    @PatchMapping("/{fotoId}/portada")
    public ResponseEntity<List<FotoRespuestaDTO>> marcarPortada(
            @PathVariable Long productoId,
            @PathVariable Long fotoId,
            @RequestParam Long usuarioId
    ) {
        Producto producto = fotoService.marcarPortada(
                productoId,
                fotoId,
                usuarioId
        );

        return ResponseEntity.ok(
                FotoRespuestaDTO.fromEntities(producto.getFotos())
        );
    }

    @DeleteMapping("/{fotoId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long productoId,
            @PathVariable Long fotoId,
            @RequestParam Long usuarioId
    ) {
        fotoService.eliminarFoto(productoId, fotoId, usuarioId);

        return ResponseEntity.noContent().build();
    }
}

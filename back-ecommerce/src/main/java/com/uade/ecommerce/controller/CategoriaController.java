package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.CategoriaDTO;
import com.uade.ecommerce.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> getAll() {
        return ResponseEntity.ok(
                categoriaService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                categoriaService.getById(id)
        );
    }

    @GetMapping("/buscar")
    public ResponseEntity<CategoriaDTO> buscarPorNombre(
            @RequestParam String nombre,
            @RequestParam(
                    defaultValue = "true"
            ) boolean ignorarMayusculas
    ) {
        return ResponseEntity.ok(
                categoriaService.buscarPorNombre(
                        nombre,
                        ignorarMayusculas
                )
        );
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> crear(
            @Valid @RequestBody CategoriaDTO datos
    ) {
        CategoriaDTO guardada =
                categoriaService.crear(datos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDTO datos
    ) {
        CategoriaDTO actualizada =
                categoriaService.actualizar(id, datos);

        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        categoriaService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
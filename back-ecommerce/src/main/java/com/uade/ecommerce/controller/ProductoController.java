package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.ActualizarStockDTO;
import com.uade.ecommerce.dto.ProductoCrearDTO;
import com.uade.ecommerce.dto.ProductoRespuestaDTO;
import com.uade.ecommerce.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;


import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoRespuestaDTO>> getAll(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean conStock
    ) {
        return ResponseEntity.ok(productoService.buscarRespuestas(
                categoriaId, nombre, conStock
        ));
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<ProductoRespuestaDTO>> buscarPaginado(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean conStock,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable
    ) {
        return ResponseEntity.ok(productoService.buscarPaginadoRespuestas(
                categoriaId, nombre, conStock, precioMin, precioMax, pageable
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoRespuestaDTO> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(productoService.buscarRespuesta(id));
    }

    @PostMapping
    public ResponseEntity<ProductoRespuestaDTO> crear(
            @Valid @RequestBody ProductoCrearDTO datos
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoService.crear(datos));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoRespuestaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoCrearDTO datos
    ) {
        return ResponseEntity.ok(productoService.actualizar(id, datos));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductoRespuestaDTO> actualizarStock(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarStockDTO datos
    ) {
        return ResponseEntity.ok(productoService.actualizarStock(id, datos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam Long usuarioId
    ) {
        productoService.eliminar(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

}

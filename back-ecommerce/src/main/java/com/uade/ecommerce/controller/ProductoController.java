package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.ActualizarStockDTO;
import com.uade.ecommerce.dto.ProductoCrearDTO;
import com.uade.ecommerce.dto.ProductoRespuestaDTO;
import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Producto;
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
        List<ProductoRespuestaDTO> productos =
                productoService.buscar(categoriaId, nombre, conStock)
                        .stream()
                        .map(ProductoRespuestaDTO::fromEntity)
                        .toList();

        return ResponseEntity.ok(productos);
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
        Page<ProductoRespuestaDTO> resultado =
                productoService.buscarPaginado(
                        categoriaId,
                        nombre,
                        conStock,
                        precioMin,
                        precioMax,
                        pageable
                ).map(ProductoRespuestaDTO::fromEntity);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoRespuestaDTO> getById(
            @PathVariable Long id
    ) {
        Producto producto = productoService.getById(id)
                .orElseThrow(() ->
                        ApiException.notFound("Producto no encontrado")
                );

        return ResponseEntity.ok(
                ProductoRespuestaDTO.fromEntity(producto)
        );
    }

    @PostMapping
    public ResponseEntity<ProductoRespuestaDTO> crear(
            @Valid @RequestBody ProductoCrearDTO datos
    ) {
        Producto producto = crearProductoDesdeDTO(datos);

        Producto guardado = productoService.crear(
                producto,
                datos.getCategoriaId(),
                datos.getUsuarioId(),
                datos.getFotos()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductoRespuestaDTO.fromEntity(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoRespuestaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoCrearDTO datos
    ) {
        Producto nuevosDatos = crearProductoDesdeDTO(datos);

        Producto actualizado = productoService.actualizar(
                id,
                datos.getUsuarioId(),
                nuevosDatos,
                datos.getCategoriaId(),
                datos.getFotos()
        );

        return ResponseEntity.ok(
                ProductoRespuestaDTO.fromEntity(actualizado)
        );
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductoRespuestaDTO> actualizarStock(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarStockDTO datos
    ) {
        Producto actualizado = productoService.actualizarStock(
                id,
                datos.getUsuarioId(),
                datos.getStock()
        );

        return ResponseEntity.ok(
                ProductoRespuestaDTO.fromEntity(actualizado)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam Long usuarioId
    ) {
        productoService.eliminar(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    private Producto crearProductoDesdeDTO(ProductoCrearDTO datos) {
        Producto producto = new Producto();
        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        return producto;
    }
}
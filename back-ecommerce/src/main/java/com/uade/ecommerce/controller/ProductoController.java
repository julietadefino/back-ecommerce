package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.ActualizarStockDTO;
import com.uade.ecommerce.dto.ProductoCrearDTO;
import com.uade.ecommerce.dto.ProductoRespuestaDTO;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uade.ecommerce.exception.ApiException;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<ProductoRespuestaDTO> getAll(
            @RequestParam(required = false)
            Long categoriaId,
            @RequestParam(required = false)
            String nombre,
            @RequestParam(required = false)
            Boolean conStock
    ) {
        List<Producto> productos =
                productoService.buscar(categoriaId, nombre, conStock);

        return productos.stream()
                .map(ProductoRespuestaDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductoRespuestaDTO getById(
            @PathVariable Long id
    ) {
        Producto producto = productoService.getById(id)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Producto no encontrado"
                            )
                                
                );

        return ProductoRespuestaDTO.fromEntity(producto);
    }

    @PostMapping
    public ResponseEntity<ProductoRespuestaDTO> crear(
            @Valid @RequestBody ProductoCrearDTO datos
    ) {
        Producto producto = new Producto();
        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());

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

    @PatchMapping("/{id}/stock")
    public ProductoRespuestaDTO actualizarStock(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarStockDTO datos
    ) {
        Producto actualizado =
                productoService.actualizarStock(
                        id,
                        datos.getUsuarioId(),
                        datos.getStock()
                );

        return ProductoRespuestaDTO.fromEntity(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam Long usuarioId
    ) {
        productoService.eliminar(id, usuarioId);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
public ProductoRespuestaDTO actualizar(
        @PathVariable Long id,
        @Valid @RequestBody ProductoCrearDTO datos
) {
    Producto nuevosDatos = new Producto();
    nuevosDatos.setNombre(datos.getNombre());
    nuevosDatos.setDescripcion(datos.getDescripcion());
    nuevosDatos.setPrecio(datos.getPrecio());
    nuevosDatos.setStock(datos.getStock());

    Producto actualizado = productoService.actualizar(
            id,
            datos.getUsuarioId(),
            nuevosDatos,
            datos.getCategoriaId(),
            datos.getFotos()
    );

    return ProductoRespuestaDTO.fromEntity(actualizado);
    }

        }

package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.ActualizarCantidadItemDTO;
import com.uade.ecommerce.dto.AgregarItemCarritoDTO;
import com.uade.ecommerce.dto.CarritoRespuestaDTO;
import com.uade.ecommerce.dto.CheckoutRespuestaDTO;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/{usuarioId}")
    public CarritoRespuestaDTO getByUsuario(
            @PathVariable Long usuarioId
    ) {
        Carrito carrito =
                carritoService.getByUsuario(usuarioId);

        return CarritoRespuestaDTO.fromEntity(carrito);
    }

    @PostMapping("/items")
    public CarritoRespuestaDTO agregarProducto(
            @Valid @RequestBody AgregarItemCarritoDTO datos
    ) {
        Carrito carrito = carritoService.agregarProducto(
                datos.getUsuarioId(),
                datos.getProductoId(),
                datos.getCantidad()
        );

        return CarritoRespuestaDTO.fromEntity(carrito);
    }

    @PatchMapping("/{usuarioId}/items/{itemId}")
    public CarritoRespuestaDTO actualizarCantidad(
            @PathVariable Long usuarioId,
            @PathVariable Long itemId,
            @RequestBody ActualizarCantidadItemDTO datos
    ) {
        Carrito carrito = carritoService.actualizarCantidadItem(
                usuarioId,
                itemId,
                datos.getCantidad()
        );

        return CarritoRespuestaDTO.fromEntity(carrito);
    }

    @DeleteMapping("/{usuarioId}/items/{itemId}")
    public CarritoRespuestaDTO eliminarItem(
            @PathVariable Long usuarioId,
            @PathVariable Long itemId
    ) {
        Carrito carrito = carritoService.eliminarItem(
                usuarioId,
                itemId
        );

        return CarritoRespuestaDTO.fromEntity(carrito);
    }

    @DeleteMapping("/{usuarioId}/items")
    public CarritoRespuestaDTO vaciar(
            @PathVariable Long usuarioId
    ) {
        Carrito carrito =
                carritoService.vaciar(usuarioId);

        return CarritoRespuestaDTO.fromEntity(carrito);
    }

    @PostMapping("/{usuarioId}/checkout")
    public ResponseEntity<CheckoutRespuestaDTO> checkout(
            @PathVariable Long usuarioId
    ) {
        BigDecimal total =
                carritoService.checkout(usuarioId);

        CheckoutRespuestaDTO respuesta =
                new CheckoutRespuestaDTO(
                        "Checkout realizado correctamente",
                        total
                );

        return ResponseEntity.ok(respuesta);
    }
}

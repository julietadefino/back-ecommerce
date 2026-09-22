package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.CheckoutRequestDTO;
import com.uade.ecommerce.dto.OrdenRespuestaDTO;
import com.uade.ecommerce.model.OrdenCompra;
import com.uade.ecommerce.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrdenRespuestaDTO> checkout(
            @Valid @RequestBody CheckoutRequestDTO request
    ) {
        OrdenCompra orden = ordenService.checkout(request.getUsuarioId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrdenRespuestaDTO.fromEntity(orden));
    }

    @GetMapping("/historial/{usuarioId}")
    public ResponseEntity<List<OrdenRespuestaDTO>> obtenerHistorial(
            @PathVariable Long usuarioId
    ) {
        List<OrdenRespuestaDTO> historial = ordenService.obtenerHistorial(usuarioId)
                .stream()
                .map(OrdenRespuestaDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(historial);
    }
}

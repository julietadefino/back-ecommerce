package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.ReseniaDTO;
import com.uade.ecommerce.service.ReseniaService;
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
@RequestMapping("/api/resenias")
public class ReseniaController {

    private final ReseniaService reseniaService;

    public ReseniaController(ReseniaService reseniaService) {
        this.reseniaService = reseniaService;
    }

    @PostMapping
    public ResponseEntity<ReseniaDTO> crearResenia(
            @Valid @RequestBody ReseniaDTO reseniaDTO
    ) {
        ReseniaDTO nuevaResenia =
                reseniaService.crearResenia(reseniaDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevaResenia);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ReseniaDTO>> obtenerResenias(
            @PathVariable Long productoId
    ) {
        return ResponseEntity.ok(
                reseniaService.obtenerReseniasPorProducto(productoId)
        );
    }

    @GetMapping("/producto/{productoId}/promedio")
    public ResponseEntity<Double> obtenerPromedio(
            @PathVariable Long productoId
    ) {
        return ResponseEntity.ok(
                reseniaService.obtenerPromedioProducto(productoId)
        );
    }
}
package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.ReseniaDTO;
import com.uade.ecommerce.service.ReseniaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resenias")
public class ReseniaController {

    @Autowired
    private ReseniaService reseniaService;

    @PostMapping
    public ResponseEntity<ReseniaDTO> crearResenia(@RequestBody ReseniaDTO reseniaDTO) {
        ReseniaDTO nuevaResenia = reseniaService.crearResenia(reseniaDTO);
        return new ResponseEntity<>(nuevaResenia, HttpStatus.CREATED);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ReseniaDTO>> obtenerResenias(@PathVariable Long productoId) {
        List<ReseniaDTO> resenias = reseniaService.obtenerReseniasPorProducto(productoId);
        return new ResponseEntity<>(resenias, HttpStatus.OK);
    }

    @GetMapping("/producto/{productoId}/promedio")
    public ResponseEntity<Double> obtenerPromedio(@PathVariable Long productoId) {
        Double promedio = reseniaService.obtenerPromedioProducto(productoId);
        return new ResponseEntity<>(promedio, HttpStatus.OK);
    }
}
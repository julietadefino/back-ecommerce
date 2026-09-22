package com.uade.ecommerce.service;

import com.uade.ecommerce.dto.ReseniaDTO;
import com.uade.ecommerce.exception.CalificacionInvalidaException;
import com.uade.ecommerce.exception.ReseniaDuplicadaException;
import com.uade.ecommerce.model.Resenia;
import com.uade.ecommerce.repository.ReseniaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReseniaService {

    @Autowired
    private ReseniaRepository reseniaRepository;

    private static final List<String> PALABRAS_PROHIBIDAS = Arrays.asList("estafa", "basura", "horrible");

    public ReseniaDTO crearResenia(ReseniaDTO dto) {
        if (dto.getCalificacion() < 1 || dto.getCalificacion() > 5) {
            throw new CalificacionInvalidaException("La calificación debe estar entre 1 y 5 estrellas.");
        }

        if (reseniaRepository.existsByUsuarioIdAndProductoId(dto.getUsuarioId(), dto.getProductoId())) {
            throw new ReseniaDuplicadaException("El usuario ya dejó una reseña para este producto.");
        }

        Resenia resenia = new Resenia();
        resenia.setCalificacion(dto.getCalificacion());
        resenia.setComentario(filtrarMalasPalabras(dto.getComentario()));
        resenia.setProductoId(dto.getProductoId());
        resenia.setUsuarioId(dto.getUsuarioId());

        Resenia reseniaGuardada = reseniaRepository.save(resenia);
        dto.setId(reseniaGuardada.getId());
        dto.setComentario(reseniaGuardada.getComentario());

        return dto;
    }

    public List<ReseniaDTO> obtenerReseniasPorProducto(Long productoId) {
        return reseniaRepository.findByProductoId(productoId).stream().map(resenia -> {
            ReseniaDTO dto = new ReseniaDTO();
            dto.setId(resenia.getId());
            dto.setCalificacion(resenia.getCalificacion());
            dto.setComentario(resenia.getComentario());
            dto.setProductoId(resenia.getProductoId());
            dto.setUsuarioId(resenia.getUsuarioId());
            return dto;
        }).collect(Collectors.toList());
    }

    public Double obtenerPromedioProducto(Long productoId) {
        return reseniaRepository.obtenerPromedioEstrellasPorProducto(productoId);
    }

    private String filtrarMalasPalabras(String comentario) {
        if (comentario == null || comentario.trim().isEmpty()) return comentario;
        String comentarioFiltrado = comentario;
        for (String palabra : PALABRAS_PROHIBIDAS) {
            comentarioFiltrado = comentarioFiltrado.replaceAll("(?i)" + palabra, "***");
        }
        return comentarioFiltrado;
    }
}
package com.uade.ecommerce.service;

import com.uade.ecommerce.dto.ReseniaDTO;
import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.exception.CalificacionInvalidaException;
import com.uade.ecommerce.exception.ReseniaDuplicadaException;
import com.uade.ecommerce.model.Resenia;
import com.uade.ecommerce.repository.ProductoRepository;
import com.uade.ecommerce.repository.ReseniaRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class ReseniaService {

    private static final List<String> PALABRAS_PROHIBIDAS =
            List.of("estafa", "basura", "horrible");

    private final ReseniaRepository reseniaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReseniaService(
            ReseniaRepository reseniaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.reseniaRepository = reseniaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ReseniaDTO crearResenia(ReseniaDTO dto) {
        validarCalificacion(dto.getCalificacion());
        validarProducto(dto.getProductoId());
        validarUsuario(dto.getUsuarioId());

        if (reseniaRepository.existsByUsuarioIdAndProductoId(
                dto.getUsuarioId(),
                dto.getProductoId()
        )) {
            throw new ReseniaDuplicadaException(
                    "El usuario ya dejó una reseña para este producto"
            );
        }

        Resenia resenia = new Resenia();
        resenia.setCalificacion(dto.getCalificacion());
        resenia.setComentario(
                filtrarMalasPalabras(dto.getComentario())
        );
        resenia.setProductoId(dto.getProductoId());
        resenia.setUsuarioId(dto.getUsuarioId());

        Resenia guardada = reseniaRepository.save(resenia);
        return convertirADTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<ReseniaDTO> obtenerReseniasPorProducto(Long productoId) {
        validarProducto(productoId);

        return reseniaRepository.findByProductoId(productoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Double obtenerPromedioProducto(Long productoId) {
        validarProducto(productoId);

        return reseniaRepository
                .obtenerPromedioEstrellasPorProducto(productoId);
    }

    private void validarCalificacion(Integer calificacion) {
        if (calificacion == null ||
                calificacion < 1 ||
                calificacion > 5) {
            throw new CalificacionInvalidaException(
                    "La calificación debe estar entre 1 y 5 estrellas"
            );
        }
    }

    private void validarProducto(Long productoId) {
        if (productoId == null || productoId <= 0) {
            throw ApiException.badRequest(
                    "El ID del producto debe ser un número positivo"
            );
        }

        if (!productoRepository.existsById(productoId)) {
            throw ApiException.notFound("Producto no encontrado");
        }
    }

    private void validarUsuario(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw ApiException.badRequest(
                    "El ID del usuario debe ser un número positivo"
            );
        }

        if (!usuarioRepository.existsById(usuarioId)) {
            throw ApiException.notFound("Usuario no encontrado");
        }
    }

    private ReseniaDTO convertirADTO(Resenia resenia) {
        ReseniaDTO dto = new ReseniaDTO();
        dto.setId(resenia.getId());
        dto.setCalificacion(resenia.getCalificacion());
        dto.setComentario(resenia.getComentario());
        dto.setProductoId(resenia.getProductoId());
        dto.setUsuarioId(resenia.getUsuarioId());
        return dto;
    }

    private String filtrarMalasPalabras(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            return comentario;
        }

        String comentarioFiltrado = comentario;

        for (String palabra : PALABRAS_PROHIBIDAS) {
            comentarioFiltrado = comentarioFiltrado.replaceAll(
                    "(?i)" + Pattern.quote(palabra),
                    "***"
            );
        }

        return comentarioFiltrado;
    }
}
package com.uade.ecommerce.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Cuerpo de POST /api/productos/{productoId}/fotos.
 * Las fotos son URLs de imágenes ya alojadas, no archivos binarios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgregarFotosDTO {

    @NotNull(message = "El usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Long usuarioId;

    @NotEmpty(message = "Debe indicar al menos una foto")
    private List<String> fotos;
}

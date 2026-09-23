package com.uade.ecommerce.dto;

import com.uade.ecommerce.model.Foto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Foto tal como se devuelve al cliente. Expone el id para que el
 * front pueda borrar o destacar una foto puntual.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FotoRespuestaDTO {

    private Long id;
    private String url;
    private boolean esPortada;

    public static FotoRespuestaDTO fromEntity(Foto foto) {
        return new FotoRespuestaDTO(
                foto.getId(),
                foto.getUrl(),
                foto.isEsPortada()
        );
    }

    public static List<FotoRespuestaDTO> fromEntities(
            List<Foto> fotos
    ) {
        return fotos == null
                ? List.of()
                : fotos.stream()
                        .map(FotoRespuestaDTO::fromEntity)
                        .toList();
    }
}

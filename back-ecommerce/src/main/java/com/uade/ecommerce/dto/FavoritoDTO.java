package com.uade.ecommerce.dto;

import com.uade.ecommerce.model.Favorito;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoDTO {

    private Long id;
    private Long usuarioId;
    private Long productoId;
    private String nombreProducto;
    private BigDecimal precioProducto;
    private Boolean stockDisponible;

    public static FavoritoDTO fromEntity(Favorito favorito) {
        return new FavoritoDTO(
                favorito.getId(),
                favorito.getUsuario().getId(),
                favorito.getProducto().getId(),
                favorito.getProducto().getNombre(),
                favorito.getProducto().getPrecio(),
                favorito.getProducto().getStock() > 0
        );
    }
}
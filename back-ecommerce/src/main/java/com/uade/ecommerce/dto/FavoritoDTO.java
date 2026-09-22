package com.uade.ecommerce.dto;

import com.uade.ecommerce.entity.Favorito; // O com.uade.ecommerce.model.Favorito según el paquete de tus entidades
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoDTO {

    private Long id;
    private Long usuarioId;
    private Long productoId;
    
    private String nombreProducto;
    private Double precioProducto;
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

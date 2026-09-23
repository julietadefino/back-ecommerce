package com.uade.ecommerce.dto;

import com.uade.ecommerce.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRespuestaDTO {

    private Long id;
    private String nombre;
    private String descripcion;
        private BigDecimal precio;
    private Integer stock;

    private Long categoriaId;
    private String categoria;

    private Long usuarioId;
    private String nombreUsuario;

    private List<FotoRespuestaDTO> fotos;

    public static ProductoRespuestaDTO fromEntity(
            Producto producto
    ) {
        List<FotoRespuestaDTO> fotos = producto.getFotos() == null
                ? List.of()
                : producto.getFotos()
                        .stream()
                        .map(FotoRespuestaDTO::fromEntity)
                        .toList();

        return new ProductoRespuestaDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getUsuario().getId(),
                producto.getUsuario().getNombreUsuario(),
                fotos
        );
    }
}

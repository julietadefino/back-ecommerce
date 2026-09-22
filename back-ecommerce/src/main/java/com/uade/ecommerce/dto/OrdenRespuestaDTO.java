package com.uade.ecommerce.dto;

import com.uade.ecommerce.model.OrdenCompra;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrdenRespuestaDTO {

    private Long id;
    private Long usuarioId;
    private BigDecimal total;
    private LocalDateTime fechaCreacion;
    private String codigoSeguimiento;
    private List<DetallePedidoRespuestaDTO> detalles;

    public static OrdenRespuestaDTO fromEntity(OrdenCompra orden) {
        List<DetallePedidoRespuestaDTO> detalles = orden.getDetalles() == null
                ? List.of()
                : orden.getDetalles().stream()
                        .map(DetallePedidoRespuestaDTO::fromEntity)
                        .toList();

        return new OrdenRespuestaDTO(
                orden.getId(),
                orden.getUsuario().getId(),
                orden.getTotal(),
                orden.getFechaCreacion(),
                orden.getCodigoSeguimiento(),
                detalles
        );
    }
}

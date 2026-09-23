package com.uade.ecommerce.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class ErrorRespuestaDTO {

    private LocalDateTime fecha;
    private Integer estado;
    private String error;
    private String mensaje;
    private String ruta;
    private Map<String, String> errores;

    public ErrorRespuestaDTO(
            LocalDateTime fecha,
            Integer estado,
            String error,
            String mensaje,
            String ruta
    ) {
        this(fecha, estado, error, mensaje, ruta, null);
    }

    public ErrorRespuestaDTO(
            LocalDateTime fecha,
            Integer estado,
            String error,
            String mensaje,
            String ruta,
            Map<String, String> errores
    ) {
        this.fecha = fecha;
        this.estado = estado;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.errores = errores;
    }
}

package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

/**
 * El producto llegó al máximo de fotos permitidas.
 *
 * Extiende ApiException para que la responda el manejador global ya
 * existente, sin necesidad de agregarle un @ExceptionHandler nuevo.
 */
public class LimiteFotosExcedidoException
        extends ApiException {

    public LimiteFotosExcedidoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}

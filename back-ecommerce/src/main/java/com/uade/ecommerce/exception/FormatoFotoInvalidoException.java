package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

/**
 * La URL recibida no corresponde a una imagen válida: no respeta el
 * formato http/https, supera el largo permitido o no termina en una
 * extensión de imagen aceptada.
 *
 * Extiende ApiException para que la responda el manejador global ya
 * existente, sin necesidad de agregarle un @ExceptionHandler nuevo.
 */
public class FormatoFotoInvalidoException
        extends ApiException {

    public FormatoFotoInvalidoException(String mensaje) {
        super(HttpStatus.BAD_REQUEST, mensaje);
    }
}

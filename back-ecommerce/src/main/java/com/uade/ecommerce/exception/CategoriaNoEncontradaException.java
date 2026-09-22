package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class CategoriaNoEncontradaException
        extends ApiException {

    public CategoriaNoEncontradaException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }
}
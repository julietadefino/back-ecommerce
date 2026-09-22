package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class ItemCarritoNoEncontradoException
        extends ApiException {

    public ItemCarritoNoEncontradoException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }
}
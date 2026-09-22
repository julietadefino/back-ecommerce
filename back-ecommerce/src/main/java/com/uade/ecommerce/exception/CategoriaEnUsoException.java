package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class CategoriaEnUsoException
        extends ApiException {

    public CategoriaEnUsoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
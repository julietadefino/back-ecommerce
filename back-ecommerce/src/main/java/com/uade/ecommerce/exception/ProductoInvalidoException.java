package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class ProductoInvalidoException extends ApiException {

    public ProductoInvalidoException(String mensaje) {
        super(HttpStatus.BAD_REQUEST, mensaje);
    }
}
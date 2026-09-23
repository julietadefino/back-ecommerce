package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class CalificacionInvalidaException extends ApiException {

    public CalificacionInvalidaException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
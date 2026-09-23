package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class ReseniaDuplicadaException extends ApiException {

    public ReseniaDuplicadaException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
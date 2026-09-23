package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class CarritoVacioException extends ApiException {

    public CarritoVacioException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
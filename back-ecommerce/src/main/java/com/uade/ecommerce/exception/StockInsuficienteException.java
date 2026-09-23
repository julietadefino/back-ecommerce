package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class StockInsuficienteException extends ApiException {

    public StockInsuficienteException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class ProductoYaEnFavoritosException extends ApiException {

    public ProductoYaEnFavoritosException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
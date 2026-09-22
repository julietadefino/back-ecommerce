package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) 
public class ProductoEnFavoritosException extends RuntimeException {
    public ProductoEnFavoritosException(String msg) {
        super(msg);
    }
}

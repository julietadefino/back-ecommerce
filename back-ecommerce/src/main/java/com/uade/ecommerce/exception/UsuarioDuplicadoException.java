package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;

public class UsuarioDuplicadoException extends ApiException {

    public UsuarioDuplicadoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}

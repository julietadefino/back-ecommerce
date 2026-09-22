package com.uade.ecommerce.exception;

@ExceptionHandler(UsuarioDuplicadoException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleUsuarioDuplicado(UsuarioDuplicadoException ex) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO("USUARIO_DUPLICADO", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}

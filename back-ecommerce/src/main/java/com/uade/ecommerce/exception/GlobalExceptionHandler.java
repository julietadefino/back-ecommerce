package com.uade.ecommerce.exception;

import com.uade.ecommerce.dto.ErrorRespuestaDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarValidacion(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errores = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errores.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        HttpStatus estado = HttpStatus.BAD_REQUEST;
        ErrorRespuestaDTO respuesta = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                "La solicitud contiene datos inválidos",
                request.getRequestURI(),
                errores
        );

        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(UsuarioDuplicadoException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleUsuarioDuplicado(UsuarioDuplicadoException ex) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO("USUARIO_DUPLICADO", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarApiException(
            ApiException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                exception.getStatus(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud es inválido o está mal formado",
                request
        );
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorRespuestaDTO> manejarParametroInvalido(
            Exception exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "Los parámetros de la solicitud son inválidos",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarErrorInesperado(
            Exception exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado",
                request
        );
    }

    private ResponseEntity<ErrorRespuestaDTO> construirRespuesta(
            HttpStatus estado,
            String mensaje,
            HttpServletRequest request
    ) {
        ErrorRespuestaDTO respuesta = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity.status(estado).body(respuesta);
    }
}

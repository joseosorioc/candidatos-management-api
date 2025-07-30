package com.seek.candidatosmanagementapi.exception;

import com.seek.candidatosmanagementapi.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de campos anotados con @Valid.
     * @param ex excepción lanzada con los detalles de validación.
     * @param request contexto de la petición HTTP.
     * @return respuesta 400 con lista de errores y mensaje descriptivo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Error de validación: {}", ex.getMessage());
        String messages = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse resp = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Errores de validación: " + messages)
                .build();
        return ResponseEntity.badRequest().body(resp);
    }

    /**
     * Maneja violaciones de restricciones de validación manual (ConstraintViolationException).
     * @param ex excepción con los detalles de las restricciones incumplidas.
     * @param request contexto de la petición HTTP.
     * @return respuesta 400 con resumen de los mensajes de violación.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        log.warn("Violación de restricción: {}", ex.getMessage());
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse resp = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Violación de restricciones: " + msg)
                .build();
        return ResponseEntity.badRequest().body(resp);
    }

    /**
     * Captura JSON malformado en el cuerpo de la petición.
     * @param ex excepción que indica formato inválido de JSON.
     * @param request contexto de la petición HTTP.
     * @return respuesta 400 con mensaje genérico de formato inválido.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("JSON inválido: {}", ex.getMessage());
        ErrorResponse resp = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Formato de JSON inválido")
                .build();
        return ResponseEntity.badRequest().body(resp);
    }

    /**
     * Gestiona desajustes de tipo en los parámetros de ruta o query.
     * @param ex excepción que contiene el nombre y tipo requerido del parámetro.
     * @param request contexto de la petición HTTP.
     * @return respuesta 400 indicando el tipo correcto esperado.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Tipo incorrecto: {}", ex.getMessage());
        ErrorResponse resp = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(String.format(
                        "El parámetro '%s' debe ser de tipo %s",
                        ex.getName(), ex.getRequiredType().getSimpleName()))
                .build();
        return ResponseEntity.badRequest().body(resp);
    }

    /**
     * Se invoca cuando no hay manejador registrado para la URL solicitada.
     * @param ex excepción que contiene la URL no encontrada.
     * @param request contexto de la petición HTTP.
     * @return respuesta 404 indicando que el endpoint no existe.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {
        log.warn("Endpoint no encontrado: {}", ex.getRequestURL());
        ErrorResponse resp = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("Endpoint no existe")
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
    }

    /**
     * Maneja métodos HTTP no soportados por un endpoint.
     * @param ex excepción que indica el método no permitido.
     * @param request contexto de la petición HTTP.
     * @return respuesta 405 con el método no permitido.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("Método no permitido: {}", ex.getMessage());
        ErrorResponse resp = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message(String.format("Método %s no permitido", ex.getMethod()))
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(resp);
    }

    /**
     * Captura excepciones de lógica de negocio personalizadas.
     * @param ex excepción BusinessException con mensaje de conflicto.
     * @param request contexto de la petición HTTP.
     * @return respuesta 409 con el mensaje de negocio.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.warn("Error de negocio: {}", ex.getMessage());
        ErrorResponse resp = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
    }

    /**
     * Gestiona violaciones de integridad de datos en la capa de persistencia.
     * @param ex excepción DataIntegrityException con detalles de la violación.
     * @param request contexto de la petición HTTP.
     * @return respuesta 422 con mensaje de integridad.
     */
    @ExceptionHandler(DataIntegrityException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityException(
            DataIntegrityException ex, WebRequest request) {
        log.warn("Integridad de datos violada: {}", ex.getMessage());
        ErrorResponse resp = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Unprocessable Entity")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(resp);
    }

    /**
     * Captura cualquier excepción no prevista y devuelve un 500 genérico.
     * @param ex excepción genérica ocurrida en el servidor.
     * @param request contexto de la petición HTTP.
     * @return respuesta 500 indicando error interno del servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Error inesperado", ex);
        ErrorResponse resp = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Error interno del servidor. Contacte al administrador.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}

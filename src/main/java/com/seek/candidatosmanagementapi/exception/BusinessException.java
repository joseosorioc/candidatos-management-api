package com.seek.candidatosmanagementapi.exception;

/**
 * Excepción personalizada para errores de lógica de negocio.
 */
public class BusinessException extends RuntimeException {
    /**
     * Crea una excepción de negocio con un mensaje específico.
     *
     * @param message Descripción del error de negocio.
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Crea una excepción de negocio con mensaje y causa original.
     *
     * @param message Descripción del error de negocio.
     * @param cause   Excepción causante original.
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

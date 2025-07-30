package com.seek.candidatosmanagementapi.exception;

/**
 * Excepción que indica violaciones de integridad de datos en la aplicación.
 */
public class DataIntegrityException extends RuntimeException {

    /**
     * Crea una nueva excepción con un mensaje descriptivo.
     *
     * @param message Mensaje que describe el error de integridad.
     */
    public DataIntegrityException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción con un mensaje descriptivo y causa raíz.
     *
     * @param message Mensaje que describe el error de integridad.
     * @param cause   Causa original del error.
     */
    public DataIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }
}

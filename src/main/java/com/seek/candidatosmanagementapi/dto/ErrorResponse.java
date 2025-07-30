package com.seek.candidatosmanagementapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de error de la API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    /** Fecha y hora del error (formato yyyy-MM-dd HH:mm:ss) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /** Código HTTP del error */
    private int status;

    /** Descripción del error */
    private String error;

    /** Mensaje detallado para el cliente */
    private String message;
}

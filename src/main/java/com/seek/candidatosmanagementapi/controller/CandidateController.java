package com.seek.candidatosmanagementapi.controller;

import com.seek.candidatosmanagementapi.dto.CandidateResponse;
import com.seek.candidatosmanagementapi.dto.CreateCandidateRequest;
import com.seek.candidatosmanagementapi.dto.ErrorResponse;
import com.seek.candidatosmanagementapi.dto.MetricsResponse;
import com.seek.candidatosmanagementapi.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de candidatos.
 *
 * @author Jose Osorio Catalan
 */
@RestController
@RequestMapping("api/v1/candidatos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Candidatos", description = "Gestión de candidatos para procesos de reclutamiento")
@SecurityRequirement(name = "basicAuth")
public class CandidateController {

    private final CandidateService service;

    /**
     * Crea un nuevo candidato en el sistema.
     */
    @Operation(summary = "Crear candidato", description = "Registra un nuevo candidato con validación de edad")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Candidato creado",
                    content = @Content(schema = @Schema(implementation = CandidateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Error de negocio",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CandidateResponse> create(@Valid @RequestBody CreateCandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCandidate(request));
    }

    /**
     * Obtiene todos los candidatos registrados.
     */
    @Operation(summary = "Listar candidatos", description = "Retorna todos los candidatos con información calculada")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getAll() {
        return ResponseEntity.ok(service.getAllCandidates());
    }

    /**
     * Calcula métricas de edad de todos los candidatos.
     */
    @Operation(summary = "Métricas de edad", description = "Promedio y desviación estándar de edades")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas calculadas",
                    content = @Content(schema = @Schema(implementation = MetricsResponse.class))),
            @ApiResponse(responseCode = "409", description = "No hay candidatos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        return ResponseEntity.ok(service.getMetrics());
    }
}
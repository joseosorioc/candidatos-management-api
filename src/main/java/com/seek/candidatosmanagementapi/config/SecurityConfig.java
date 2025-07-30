package com.seek.candidatosmanagementapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seek.candidatosmanagementapi.dto.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * Configuración de seguridad para la API de gestión de candidatos.
 *
 * Esta clase configura Spring Security para:
 * - Implementar autenticación HTTP Basic
 * - Manejar excepciones de seguridad con respuestas en JSON
 * - Permitir acceso público a documentación Swagger
 * - Trabajar de forma stateless
 *
 * @author Jose Osorio Catalan
 * @version 1.0
 * @since 2025-01-01
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Usuario configurado en application.properties para autenticación básica.
     */
    @Value("${security.user.name}")
    private String securityUser;

    /**
     * Contraseña configurada en application.properties para autenticación básica.
     */
    @Value("${security.user.password}")
    private String securityPassword;

    /**
     * ObjectMapper para serializar respuestas de error a JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param objectMapper para convertir objetos a JSON en respuestas de error
     */
    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Configura la cadena de filtros de seguridad de Spring Security.
     *
     * Define las reglas de seguridad:
     * - Desactiva CSRF (no necesario para APIs stateless)
     * - Permite acceso público a documentación y endpoints de monitoreo
     * - Requiere autenticación para el resto de endpoints
     * - Configura gestión de sesiones como stateless
     * - Establece autenticación HTTP Basic
     * - Define manejadores personalizados para errores de seguridad
     *
     * @param http configurador de seguridad HTTP
     * @return SecurityFilterChain configurado
     * @throws Exception si hay errores en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // Sesiones Staless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler())
                );
        return http.build();
    }

    /**
     * Servicio de detalles de usuario para autenticación.
     *
     * Crea un usuario en memoria con las credenciales configuradas.
     * En un entorno de producción esto debería conectarse a una base de datos
     * o servicio de autenticación externo.
     *
     * @param encoder encoder de contraseñas BCrypt
     * @return UserDetailsService con usuario configurado
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.withUsername(securityUser)
                .password(encoder.encode(securityPassword))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Bean para codificación de contraseñas usando BCrypt.
     *
     * BCrypt es un algoritmo de hash seguro que incluye sal automática
     * y es resistente a ataques de fuerza bruta.
     *
     * @return PasswordEncoder configurado con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Manejador personalizado para errores de autenticación (401 Unauthorized).
     *
     * Se ejecuta cuando las credenciales están ausentes o son inválidas.
     * Retorna una respuesta JSON estructurada en lugar del error HTML por defecto.
     *
     * @return AuthenticationEntryPoint que retorna respuestas JSON
     */
    @Bean
    public AuthenticationEntryPoint restAuthEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message("Credenciales inválidas o faltantes. Se requiere autenticación HTTP Basic.")
                    .build();

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());

            response.getWriter().write(objectMapper.writeValueAsString(error));
        };
    }

    /**
     * Manejador personalizado para errores de autorización (403 Forbidden).
     *
     * Se ejecuta cuando el usuario está autenticado pero no tiene permisos
     * para acceder al recurso solicitado.
     *
     * @return AccessDeniedHandler que retorna respuestas JSON
     */
    @Bean
    public AccessDeniedHandler restAccessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException ex) -> {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.FORBIDDEN.value())
                    .error("Forbidden")
                    .message("Acceso denegado. No tienes permisos para acceder a este recurso.")
                    .build();

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        };
    }
}
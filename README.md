# Candidatos Management API

Sistema de gestión de candidatos desarrollado como microservicio RESTful para procesos de selección y contratación. 

**Autor:** Jose Osorio Catalan  
**Tecnologías:** Spring Boot 3.5.4, MySQL 8.0, AWS Elastic Beanstalk

## 📋 Descripción del Proyecto

Este microservicio permite gestionar candidatos durante procesos de reclutamiento, proporcionando funcionalidades para registro, consulta y análisis estadístico de datos. La aplicación está diseñada siguiendo principios SOLID y patrones de arquitectura enterprise.

### Funcionalidades Principales

- **Registro de candidatos** con validación completa de datos
- **Consulta de métricas estadísticas** (promedio de edad, desviación estándar)
- **Listado completo** con cálculos derivados automáticos
- **Autenticación HTTP Basic** para seguridad
- **Documentación interactiva** con Swagger UI
- **Monitoreo** con Spring Boot Actuator

## 🏗️ Arquitectura del Sistema

### Capas de la Aplicación

```
├── Controller     # Capa de presentación REST
├── Service        # Lógica de negocio
├── Repository     # Acceso a datos con Spring Data JPA
├── Entity         # Entidades de dominio
├── DTO            # Objetos de transferencia de datos
├── Config         # Configuraciones de seguridad y Swagger
└── Exception      # Manejo centralizado de excepciones
```

### Patrones Implementados

- **Repository Pattern** para abstracción de datos
- **Builder Pattern** en DTOs y entidades
- **Controller-Service-Repository** para separación de responsabilidades
- **Exception Handler** global para manejo de errores
- **DTO Pattern** para transferencia de datos segura

## 🌐 Endpoints de la API

### Gestión de Candidatos

```http
# Crear candidato
POST /api/v1/candidatos
Content-Type: application/json
Authorization: Basic USUARIO:PASSWORD

{
  "firstName": "Juan Carlos",
  "lastName": "Pérez González",
  "age": 28,
  "birthDate": "1995-06-15"
}
```

```http
# Listar todos los candidatos
GET /api/v1/candidatos
Authorization: Basic USUARIO:PASSWORD
```

```http
# Obtener métricas estadísticas
GET /api/v1/candidatos/metrics
Authorization: Basic USUARIO:PASSWORD
```

### Documentación y Monitoreo

```http
# Swagger UI (público)
GET /swagger-ui.html


# Health check (público)
GET /actuator/health
```

## 📊 Cálculos Automáticos

La API realiza cálculos derivados automáticamente para cada candidato, fue un excelente punto poder proporcionar otros calculos:

- **Edad actual** Sale desde la fecha de nacimiento
- **Próximo cumpleaños** y días restantes
- **Edad en meses** total
- **Fecha estimada de evento** (75 años desde nacimiento)

## 🔒 Seguridad Implementada

### Autenticación HTTP Basic
- Usuario: `admin`
- Password: `admin123`


### Endpoints Públicos
- Documentación Swagger (`/swagger-ui/**`)
- Métricas de aplicación (`/actuator/**`)
- OpenAPI docs (`/v3/api-docs/**`)

### Configuración de Seguridad
- Sesiones stateless para escalabilidad
- CSRF deshabilitado (API REST)
- Respuestas de error en formato JSON estructurado

## 🚨 Manejo de Errores

Implementé un sistema robusto de manejo de excepciones con códigos HTTP apropiados:

- **400 Bad Request** - Errores de validación o JSON malformado
- **401 Unauthorized** - Credenciales inválidas o faltantes
- **403 Forbidden** - Acceso denegado
- **409 Conflict** - Errores de lógica de negocio
- **422 Unprocessable Entity** - Violaciones de integridad de datos
- **500 Internal Server Error** - Errores inesperados del sistema

Todas las respuestas de error siguen el formato estructurado:

```json
{
  "timestamp": "2025-01-29 14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El nombre es obligatorio"
}
```

## 🧪 Testing

### Pruebas Implementadas

**Tests Unitarios:**
- `CandidateControllerTest` - Validación de endpoints y respuestas
- `SecurityConfigTest` - Verificación de configuración de seguridad

**Cobertura de Testing:**
- Casos exitosos (200, 201)
- Manejo de errores (400, 401, 409, 422, 500)
- Validación de autenticación y autorización
- Comportamiento stateless de sesiones

### Ejecutar Pruebas
Importante: Tener configuradas las variables ambientes que puede encontrarlas en ElasticBeanStalk > Environments > Api-candidatos

<img width="1846" height="311" alt="image" src="https://github.com/user-attachments/assets/6879c0ba-12ae-48b1-b48b-744b5e7246d9" />

<img width="1514" height="361" alt="image" src="https://github.com/user-attachments/assets/7f5b5ee0-b22c-4af0-b04e-93b555c30afd" />



```bash
# Ejecutar todos los tests - 


Luego que en local tenga las variables con los valores, puede ejecutar

mvn test 

# Ejecutar con reporte de cobertura
mvn test jacoco:report
```

## ☁️ Infraestructura AWS

### Arquitectura Desplegada

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Internet      │────│  Elastic         │────│     RDS         │
│   Gateway       │    │  Beanstalk       │    │    MySQL        │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                       ┌──────────────────┐
                       │   CloudWatch     │
                       │     Logs         │
                       └──────────────────┘
```

### Servicios AWS Utilizados

- **Elastic Beanstalk** - Despliegue y gestión de aplicación
- **RDS MySQL 8.0** - Base de datos managed
- **CloudWatch** - Logs y métricas
- **EC2** - Instancias gestionadas por EB

### Variables de Ambiente

La aplicación usa variables de ambiente para configuración:

```properties
# Base de datos
DB_URL=[Este valor esta en los parametros]
DB_USERNAME=[Este valor esta en los parametros]
DB_PASSWORD=[Este valor esta en los parametros]

# Seguridad
SECURITY_USERNAME=[Este valor esta en los parametros]
SECURITY_PASSWORD=[Este valor esta en los parametros]

# Aplicación
APP_NAME=candidatos-management-api
SERVER_PORT=8080
```

## 🚀 Configuración y Ejecución

### Prerrequisitos

- Java 17 o superior
- Maven 3.6+
- MySQL 8.0
- AWS CLI (para despliegue)

### Ejecución Local

1. **Clonar el repositorio:**
```bash
git clone https://github.com/josekarllos/candidatos-management-api.git
cd candidatos-management-api
```

2. **Configurar base de datos:**
```sql
CREATE DATABASE candidatos_db;
```

3. **Configurar variables de ambiente:**
```bash
export DB_URL=jdbc:mysql://localhost:3306/candidatos_db
export DB_USERNAME=root
export DB_PASSWORD=tu_password
export SECURITY_USERNAME=admin
export SECURITY_PASSWORD=admin123
```

4. **Compilar y ejecutar:**
```bash
mvn clean install
mvn spring-boot:run
```

5. **Acceder a la aplicación en local:**
- API: `http://localhost:8080/api/v1/candidatos`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Actuator: `http://localhost:8080/actuator`

### Construcción para Producción

```bash
# Generar JAR optimizado
mvn clean package -Dmaven.test.skip=true

# El JAR se genera en target/candidatos-management-api-1.0-SNAPSHOT.jar
```

## 📈 Monitoreo y Observabilidad

### Métricas Disponibles

La aplicación expone métricas a través de Spring Boot Actuator:

- **Métricas JVM** - Memoria, threads, garbage collection
- **Métricas HTTP** - Requests, responses, latencia
- **Métricas de base de datos** - Conexiones, queries
- **Métricas custom** - Candidatos creados, métricas calculadas

### Endpoints de Monitoreo

```http
GET /actuator/health          # Estado de la aplicación
GET /actuator/metrics         # Métricas detalladas
GET /actuator/info           # Información de la aplicación
```

## 🔧 Validaciones de Negocio

Implementé validaciones robustas para garantizar integridad de datos:

- **Nombres obligatorios** y no vacíos
- **Edad entre 0 y 150 años**
- **Fecha de nacimiento en el pasado**
- **Coherencia entre edad y fecha de nacimiento** (tolerancia ±1 año)
- **Límite de antigüedad** (máximo 150 años)

## 📚 Documentación Técnica

### Swagger/OpenAPI

La API está completamente documentada con Swagger 3:

- **Esquemas de request/response** detallados
- **Códigos de estado** explicados
- **Ejemplos de uso** para cada endpoint
- **Autenticación** configurada en la interfaz

Acceso: `http://localhost:8080/swagger-ui.html`

### Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/seek/candidatosmanagementapi/
│   │   ├── config/          # Configuraciones
│   │   ├── controller/      # Controllers REST
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # Entidades JPA
│   │   ├── exception/      # Manejo de excepciones
│   │   ├── repository/     # Repositorios
│   │   └── service/        # Lógica de negocio
│   └── resources/
│       └── application.properties
└── test/
    └── java/              # Pruebas unitarias
```



### Autenticación HTTP Basic
Para simplicidad del reto, aunque en producción recomendaría JWT o OAuth2.

## Escalabilidad

El diseño considera escalabilidad horizontal:

- **Stateless** - Sin sesiones en memoria
- **Configuración externa** - Variables de ambiente
- **Base de datos externa** - RDS separado de la aplicación
- **Métricas** - Monitoreo para identificar cuellos de botella
- **Auto Scaling** - Configurado en Elastic Beanstalk

## 📞 API en Producción

**URL:** `http://api-candidatos-env.eba-svqmjzpm.us-east-1.elasticbeanstalk.com`

**Acceso colaborador AWS:**
- Usuario: `seek-reviewers`
- Console: AWS Elastic Beanstalk, RDS, CloudWatch

**Swagger:** `http://api-candidatos-env.eba-svqmjzpm.us-east-1.elasticbeanstalk.com/swagger-ui.html`

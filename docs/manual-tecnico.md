# Manual Tecnico - Microservicio de Reportes

## 1. Objetivo
Este microservicio expone reportes del sistema de congresos. Consume informacion de otros microservicios y entrega resultados agregados para administracion.

## 2. Stack tecnico
- Java 21 (definido en pom)
- Spring Boot 3.3.4
- Spring Security (JWT stateless)
- Spring Cloud Netflix Eureka Client
- RestTemplate con @LoadBalanced
- OpenAPI/Swagger UI
- Docker + Docker Compose
- GitHub Actions para despliegue a Azure VM

## 3. Arquitectura interna
### 3.1 Componentes principales
- ReportsApplication: inicia la app y habilita discovery client.
- ReportController: expone endpoints REST bajo /api/v1/reports.
- ReportService: contiene la logica de agregacion y calculo.
- CongresoClient: consume datos de congresos, instituciones y registros.
- AsistenciasClient: consume asistencias por actividad.
- JwtAuthenticationFilter + JwtService: validan token y cargan rol en SecurityContext.

### 3.2 Flujo general de una solicitud
1. Llega request HTTP al controlador.
2. JwtAuthenticationFilter lee Authorization Bearer token.
3. JwtService valida firma, expiracion y tipo ACCESS.
4. SecurityConfig valida roles por endpoint.
5. ReportService consulta clientes remotos y arma DTO de salida.
6. Controller devuelve ResponseEntity 200 con el reporte.

## 4. Seguridad
- Autenticacion: JWT Bearer
- Sesion: stateless
- Endpoints publicos:
  - /actuator/health
  - /swagger-ui/**
  - /v3/api-docs/**
  - /swagger-ui.html
  - /api-docs/**
- Endpoints protegidos por rol:
  - ADMIN_SISTEMA:
    - GET /api/v1/reports/earnings
    - GET /api/v1/reports/congress-by-institution
  - ADMIN_CONGRESO:
    - GET /api/v1/reports/participants?conferenceId={id}
    - GET /api/v1/reports/attendance-by-activity?activityId={id}
    - GET /api/v1/reports/workshop-reservations?activityId={id}
    - GET /api/v1/reports/earnings-by-congress?conferenceId={id}

## 5. Endpoints y contratos
Base path: /api/v1/reports

### 5.1 GET /earnings
Retorna:
- totalEarnings (BigDecimal)
- totalConferences (long)
- totalRegistrations (long)

### 5.2 GET /congress-by-institution
Retorna lista por institucion:
- institutionId
- institutionName
- totalConferences
- activeConferences
- conferences[]

### 5.3 GET /participants?conferenceId={id}
Retorna:
- conferenceId
- conferenceName
- totalParticipants
- totalEarnings
- participants[]: userId, amountPaid, registeredAt

### 5.4 GET /attendance-by-activity?activityId={id}
Retorna:
- activityId
- totalAttendances
- attendances[]

### 5.5 GET /workshop-reservations?activityId={id}
Retorna:
- activityId
- totalReservations
- reservationsByParticipationType (mapa tipo -> cantidad)

### 5.6 GET /earnings-by-congress?conferenceId={id}
Retorna:
- conferenceId
- conferenceName
- conferencePrice
- totalEarnings
- totalRegistrations

## 6. Configuracion
Valores en application.yml (sobrescribibles por env vars):
- SERVER_PORT (default 8087)
- JWT_SECRET
- EUREKA_SERVER_URL=https://eureka-service-a6b0enhtggeqgwd7.eastus2-01.azurewebsites.net/eureka/
- CONGRESO_SERVICE_URL
- AUTH_SERVICE_URL
- ACTIVITIES_SERVICE_URL
- ASISTENCIAS_SERVICE_URL

Nombre del servicio para Eureka:
- spring.application.name = reports-service

## 7. Integraciones externas
- Servicio de congresos:
  - GET {CONGRESO_SERVICE_URL}/api/v1/conferences
  - GET {CONGRESO_SERVICE_URL}/api/v1/institutions
  - GET {CONGRESO_SERVICE_URL}/api/v1/registrations
- Servicio de asistencias:
  - GET {ASISTENCIAS_SERVICE_URL}/api/v1/asistencias/actividad/{activityId}

Manejo de fallos en clientes:
- Si la llamada falla, se retorna lista vacia.
- Esto evita caidas del servicio, pero puede producir reportes con totales en cero.

## 8. Ejecucion local
### 8.1 Con Maven
1. Configurar variables de entorno o archivo .env.
2. Ejecutar:

```bash
mvn spring-boot:run
```

### 8.2 Con Docker Compose
```bash
docker compose up -d --build
```

Puerto expuesto: 8087.

## 9. CI/CD
Workflow: .github/workflows/main_microservice-auth.yml

Caracteristicas actuales:
- Trigger por push a ramas main y hotfix.
- Copia proyecto por SCP a Azure VM.
- Ruta remota aislada para reportes: /opt/ms-reports/back_reports.
- Compose project aislado: reports.
- Despliegue remoto:
  - docker compose -p reports down
  - docker compose -p reports up -d --build

## 10. Pruebas
Pruebas unitarias implementadas:
- src/test/java/com/example/reports/service/ReportServiceTest.java
- src/test/java/com/example/reports/controller/ReportControllerTest.java

Ejecucion:
```bash
mvn test
```

Nota de compatibilidad JVM:
- Si se usa Java 26, Mockito inline puede fallar con Byte Buddy.
- Ya se agrego configuracion para mock-maker-subclass en:
  - src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker

## 11. Problemas comunes y solucion
### 11.1 401/403 en endpoints
- Verificar token Bearer.
- Verificar claim role y tipo ACCESS.
- Confirmar rol requerido por endpoint.

### 11.2 Reportes vacios
- Revisar disponibilidad de congreso-service y ms-asistencias.
- Validar URLs de servicios en variables de entorno.

### 11.3 Servicio no aparece en Eureka
- Revisar EUREKA_SERVER_URL y confirmar que apunte a https://eureka-service-a6b0enhtggeqgwd7.eastus2-01.azurewebsites.net/eureka/.
- Verificar conectividad de red y DNS.

### 11.4 Error en tests por Byte Buddy
- Verificar archivo MockMaker en src/test/resources/mockito-extensions.
- Reintentar con mvn test.

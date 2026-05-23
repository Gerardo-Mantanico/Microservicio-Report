# Manual de Usuario - Microservicio de Reportes

## 1. Que hace este servicio
Este microservicio permite consultar reportes del sistema de congresos para analisis operativo y financiero.

Tipos de reporte disponibles:
- Ganancias totales del sistema.
- Congresos por institucion.
- Participantes por congreso.
- Asistencias por actividad.
- Reservas de taller por tipo de participacion.
- Ganancias por congreso.

## 2. Requisitos para usarlo
- Tener URL base del servicio (ejemplo: http://localhost:8087).
- Tener token JWT valido.
- Tener rol correcto segun el reporte.

## 3. Roles requeridos
- ADMIN_SISTEMA:
  - /api/v1/reports/earnings
  - /api/v1/reports/congress-by-institution
- ADMIN_CONGRESO:
  - /api/v1/reports/participants
  - /api/v1/reports/attendance-by-activity
  - /api/v1/reports/workshop-reservations
  - /api/v1/reports/earnings-by-congress

Si el rol no coincide, el sistema puede responder 403 Forbidden.

## 4. Como autenticarte
Enviar header Authorization en cada request protegido:

```http
Authorization: Bearer TU_TOKEN_JWT
```

## 5. Uso rapido con Swagger
1. Abrir navegador en:
   - http://localhost:8087/swagger-ui.html
2. Seleccionar endpoint.
3. Presionar Try it out.
4. Completar parametros requeridos.
5. Ejecutar y revisar respuesta JSON.

## 6. Endpoints de uso
Base URL ejemplo: http://localhost:8087

### 6.1 Ganancias totales
- Metodo: GET
- URL: /api/v1/reports/earnings
- Rol: ADMIN_SISTEMA

Ejemplo curl:
```bash
curl -H "Authorization: Bearer TU_TOKEN" \
  http://localhost:8087/api/v1/reports/earnings
```

### 6.2 Congresos por institucion
- Metodo: GET
- URL: /api/v1/reports/congress-by-institution
- Rol: ADMIN_SISTEMA

Ejemplo curl:
```bash
curl -H "Authorization: Bearer TU_TOKEN" \
  http://localhost:8087/api/v1/reports/congress-by-institution
```

### 6.3 Participantes por congreso
- Metodo: GET
- URL: /api/v1/reports/participants?conferenceId=1
- Rol: ADMIN_CONGRESO

Ejemplo curl:
```bash
curl -H "Authorization: Bearer TU_TOKEN" \
  "http://localhost:8087/api/v1/reports/participants?conferenceId=1"
```

### 6.4 Asistencias por actividad
- Metodo: GET
- URL: /api/v1/reports/attendance-by-activity?activityId=10
- Rol: ADMIN_CONGRESO

Ejemplo curl:
```bash
curl -H "Authorization: Bearer TU_TOKEN" \
  "http://localhost:8087/api/v1/reports/attendance-by-activity?activityId=10"
```

### 6.5 Reservas de taller
- Metodo: GET
- URL: /api/v1/reports/workshop-reservations?activityId=10
- Rol: ADMIN_CONGRESO

Ejemplo curl:
```bash
curl -H "Authorization: Bearer TU_TOKEN" \
  "http://localhost:8087/api/v1/reports/workshop-reservations?activityId=10"
```

### 6.6 Ganancias por congreso
- Metodo: GET
- URL: /api/v1/reports/earnings-by-congress?conferenceId=1
- Rol: ADMIN_CONGRESO

Ejemplo curl:
```bash
curl -H "Authorization: Bearer TU_TOKEN" \
  "http://localhost:8087/api/v1/reports/earnings-by-congress?conferenceId=1"
```

## 7. Ejemplos de respuesta
### 7.1 Respuesta de /earnings
```json
{
  "totalEarnings": 1500.00,
  "totalConferences": 3,
  "totalRegistrations": 25
}
```

### 7.2 Respuesta de /workshop-reservations
```json
{
  "activityId": 10,
  "totalReservations": 40,
  "reservationsByParticipationType": {
    "Ponente": 5,
    "Asistente": 30,
    "Sin tipo": 5
  }
}
```

## 8. Errores frecuentes para usuario
- 401 Unauthorized:
  - Token ausente, invalido o expirado.
- 403 Forbidden:
  - El token es valido, pero el rol no tiene permiso para ese endpoint.
- 500/Internal error:
  - Servicio dependiente caido o error interno temporal.

## 9. Recomendaciones de uso
- Guardar el token en herramienta segura (Postman environment, no hardcode).
- Validar parametros conferenceId/activityId antes de ejecutar.
- Usar Swagger para explorar contratos de respuesta.
- Si un reporte viene en cero, confirmar que hay datos en los microservicios fuente.

## 10. Soporte
Para soporte tecnico, revisar primero:
- Estado del servicio: /actuator/health
- Disponibilidad del gateway y servicios dependientes
- Logs del microservicio de reportes

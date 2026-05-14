# Microservicio de Asignaciones y Trazabilidad (FleetMaster)

Este microservicio gestiona la asignación operativa de vehículos a conductores, el historial de trazabilidad y la exportación de datos.

## Tecnologías
- Spring Boot 3
- MySQL (TiDB)
- Spring Data JPA
- MapStruct & Lombok
- Apache POI (Excel) & OpenCSV
- Flyway (Migraciones)

## Requisitos
- JDK 17
- Maven 3.8+
- MySQL/TiDB

## Configuración (Variables de Entorno)
El servicio requiere las siguientes variables de entorno para conectarse a la base de datos y a Eureka:

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `TIDB_HOST` | Host de la base de datos | `localhost` |
| `TIDB_PORT` | Puerto de la base de datos | `4000` |
| `TIDB_DB` | Nombre de la base de datos | `fleetmaster` |
| `TIDB_USER` | Usuario BD | `root` |
| `TIDB_PASSWORD` | Contraseña BD | (vacio) |
| `PORT` | Puerto del microservicio | `8080` |
| `EUREKA_URL` | URL del servidor Eureka | `http://localhost:8761/eureka/` |

## Ejecución
```bash
mvn clean package
java -jar target/assignement-0.0.1-SNAPSHOT.jar
```

## Endpoints Principales

### 1. Crear Asignación
**POST** `/api/asignaciones`
```json
{
  "vehicleId": "uuid-del-vehiculo",
  "driverId": "uuid-del-conductor",
  "userId": "uuid-del-usuario"
}
```

### 2. Cerrar Asignación (Recepción)
**PATCH** `/api/asignaciones/{id}/cerrar`
```json
{
  "finalKm": 1500.5,
  "userId": "uuid-del-usuario"
}
```

### 3. Historial de Vehículo
**GET** `/api/vehiculos/{id}/historial`

### 4. Exportar Datos
**GET** `/api/asignaciones/export?formato=XLSX` (O `CSV`)

## Pruebas
Para ejecutar los tests unitarios e integración:
```bash
mvn test
```

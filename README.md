# 🏦 SPF MSA Client Core Service

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-blue.svg)](https://gradle.org/)
[![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-yellow.svg)](https://mapstruct.org/)

## 📋 Descripción

Microservicio core para gestión de clientes y transacciones bancarias desarrollado con Spring Boot. Implementa operaciones CRUD completas para clientes, cuentas y transacciones, utilizando MapStruct para mapeo automático de datos y JPA para persistencia en base de datos.

## 🛠️ Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 3.5.6 | Framework principal |
| **Spring Data JPA** | - | Persistencia de datos |
| **MapStruct** | 1.6.3 | Mapeo DTO ↔ Entity |
| **Gradle** | 8.x | Gestión de dependencias |
| **H2 Database** | - | Base de datos en memoria (testing) |
| **PostgreSQL** | 42.7.3 | Base de datos producción |
| **JUnit 5** | - | Framework de testing |
| **Mockito** | - | Mocking para pruebas |
| **JasperReports** | 6.21.0 | Generación de reportes PDF |

## 🏗️ Arquitectura del Proyecto

```
src/main/java/com/pichincha/spfmsaclientecoreservice/
├── 📱 OptimusApplication.java           # Clase principal Spring Boot
├── 📂 api/                              # Controladores REST
├── 📂 domain/                           # Entidades JPA
│   ├── Account.java                     # Entidad Cuenta
│   ├── Client.java                      # Entidad Cliente  
│   ├── Person.java                      # Entidad Persona (herencia)
│   └── Transaction.java                 # Entidad Transacción
├── 📂 model/                            # DTOs generados por OpenAPI
│   ├── AccountDTO.java
│   ├── ClientDTO.java
│   ├── TransactionDTO.java
│   └── ReportDTO.java
├── 📂 repository/                       # Repositorios JPA
│   ├── AccountRepository.java
│   ├── ClientRepository.java
│   └── TransactionRepository.java
├── 📂 service/
│   ├── 📁 mapper/                       # MapStruct Mappers
│   │   ├── AccountMapper.java
│   │   ├── ClientMapper.java
│   │   ├── TransactionMapper.java
│   │   └── ReportMapper.java
│   └── 📁 impl/                         # Implementaciones de servicios
│       ├── AccountServiceImpl.java
│       ├── ClientServiceImpl.java
│       ├── ReportServiceImpl.java
│       └── PdfReportServiceImpl.java
└── 📂 configuration/                    # Configuraciones Spring
```

## 🗺️ Mappers MapStruct

El proyecto utiliza **MapStruct** para conversión automática entre DTOs y Entidades:

### 🔄 TransactionMapper
```java
@Mapper(componentModel = "spring", uses = {AccountMapper.class})
public interface TransactionMapper {
    @Mapping(source = "account.accountId", target = "accountId")
    TransactionDTO toDto(Transaction transaction);
    
    @Mapping(source = "accountId", target = "account.accountId")
    Transaction toEntity(TransactionDTO transactionDTO);
    
    // Conversión automática LocalDateTime ↔ OffsetDateTime
    default OffsetDateTime map(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }
}
```

### 📋 Mappers Disponibles

| Mapper | Conversión | Características |
|--------|------------|-----------------|
| **AccountMapper** | `Account` ↔ `AccountDTO` | Usa `ClientMapper`, mapea `client.personId` → `clientId` |
| **ClientMapper** | `Client` ↔ `ClientDTO` | Mapea `personId` → `clientId` |
| **TransactionMapper** | `Transaction` ↔ `TransactionDTO` | Conversión de fechas, usa `AccountMapper` |
| **ReportMapper** | `Transaction` → `ReportDTO` | Para generación de reportes |

## 🚀 Instalación y Ejecución

### 📋 Prerrequisitos
- ☕ **Java 21** o superior
- 🐘 **Gradle 8.x** o superior
- 🐘 **PostgreSQL** (para producción)

### 📥 Instalación

1. **Clonar el repositorio:**
```bash
git clone <url-del-repositorio>
cd spf-msa-client-core-service-test
```

2. **Compilar el proyecto:**
```bash
./gradlew build
```

3. **Ejecutar la aplicación:**
```bash
# Modo desarrollo (H2)
./gradlew bootRun

# Con perfil específico
./gradlew bootRun --args='--spring.profiles.active=docker'
```

4. **Usando Docker:**
```bash
# Construir imagen
docker build -t optimus-app .

# Ejecutar con docker-compose
docker-compose up -d
```

## 🧪 Testing

### 🔬 Estructura de Tests

```
src/test/java/com/pichincha/spfmsaclientecoreservice/
├── OptimusApplicationTest.java          # Tests aplicación principal
├── SpringContextIntegrationTest.java   # Tests integración Spring
├── 📂 integration/                      # Tests de integración
├── 📂 repository/                       # Tests repositorios
└── 📂 service/impl/                     # Tests servicios
```

### 🏃‍♂️ Ejecutar Tests

```bash
# Todos los tests
./gradlew test

# Tests específicos
./gradlew test --tests OptimusApplicationTest
./gradlew test --tests "*Integration*"

# Con reporte de cobertura
./gradlew test jacocoTestReport
```

### 📊 Reporte de Cobertura
Los reportes de cobertura se generan en: `build/reports/jacoco/test/html/index.html`

## 🔧 Configuración

### 🌍 Perfiles de Spring

| Perfil | Base de Datos | Propósito |
|--------|---------------|-----------|
| **default** | H2 | Desarrollo local |
| **test** | H2 | Ejecución de tests |
| **docker** | PostgreSQL | Contenedores Docker |

### ⚙️ Configuraciones Clave

```yaml
# application.yml
spring:
  profiles:
    active: default
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

## 📊 Funcionalidades Principales

### 👥 Gestión de Clientes
- ✅ **CRUD completo** de clientes
- ✅ **Herencia** de datos de Person
- ✅ **Validaciones** de negocio
- ✅ **Relación** con cuentas

### 💰 Gestión de Cuentas
- ✅ **Administración** de cuentas bancarias
- ✅ **Tipos de cuenta** (Ahorro, Corriente)
- ✅ **Control de saldos** y estados
- ✅ **Relación** con transacciones

### 💸 Gestión de Transacciones
- ✅ **Registro** de movimientos
- ✅ **Cálculo automático** de saldos
- ✅ **Validaciones** de negocio
- ✅ **Historial** completo

### 📈 Reportes y PDF
- ✅ **Reportes** por cliente y fecha
- ✅ **Generación PDF** con JasperReports
- ✅ **Filtros avanzados**
- ✅ **Exportación** de datos

## 🌐 API REST

### 📋 Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/clients` | Listar clientes |
| `POST` | `/api/clients` | Crear cliente |
| `GET` | `/api/accounts` | Listar cuentas |
| `POST` | `/api/accounts` | Crear cuenta |
| `GET` | `/api/transactions` | Listar transacciones |
| `POST` | `/api/transactions` | Crear transacción |
| `GET` | `/api/reports` | Generar reportes |

### 📖 Documentación API
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `src/main/resources/openapi.yaml`

## 🐳 Docker

### 🔨 Construcción

```bash
# Construir imagen
docker build -t spf-msa-client-core-service .

# Ejecutar contenedor
docker run -p 8080:8080 spf-msa-client-core-service
```

### 🐙 Docker Compose

```bash
# Levantar servicios (app + PostgreSQL)
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

## 🔧 Scripts Útiles

```bash
# Limpiar y compilar
./gradlew clean build

# Ejecutar sin tests
./gradlew build -x test

# Generar clases de OpenAPI
./gradlew openApiGenerate

# Reporte de dependencias
./gradlew dependencies

# Análisis de código
./gradlew check
```

## 🏛️ Patrones de Diseño

### 🎯 Arquitectura en Capas
```
📱 Controller Layer  →  📊 Service Layer  →  🗄️ Repository Layer  →  💾 Database
     ↓                       ↓
   📄 DTO            →    🏗️ Entity
```

### 🔄 Mapeo de Datos
- **DTOs**: Transferencia de datos en API REST
- **Entities**: Mapeo con base de datos (JPA)
- **MapStruct**: Conversión automática entre capas

### 🎨 Principios SOLID
- **Single Responsibility**: Cada clase tiene una responsabilidad
- **Dependency Injection**: Inyección de dependencias con Spring
- **Interface Segregation**: Interfaces específicas para cada funcionalidad

## 📝 Contribución

### 🔀 Flujo de Trabajo

1. **Fork** del repositorio
2. **Crear rama** para nueva funcionalidad
   ```bash
   git checkout -b feature/nueva-funcionalidad
   ```
3. **Realizar cambios** con tests
4. **Ejecutar validaciones**
   ```bash
   ./gradlew test check
   ```
5. **Commit y Push**
6. **Crear Pull Request**

### 📏 Estándares de Código
- **Java Code Style**: Google Java Style Guide
- **Cobertura mínima**: 80%
- **Tests obligatorios** para nuevas funcionalidades
- **Documentación** de APIs con OpenAPI

## 📈 Monitoreo y Observabilidad

### 🔍 Actuator Endpoints
- **Health**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

### 📊 Métricas Disponibles
- Tiempo de respuesta de endpoints
- Contadores de transacciones
- Estado de base de datos
- Uso de memoria

## 🔒 Seguridad

### 🛡️ Implementaciones
- **Validación** de entrada con Bean Validation
- **Manejo de excepciones** centralizado
- **Logging** de operaciones críticas
- **Configuración** segura de base de datos

## 📚 Documentación Adicional

- [🐳 Guía Docker](README-DOCKER.md)
- [🧪 Guía de Testing](docs/testing-guide.md)
- [🗄️ Esquema de Base de Datos](docs/database-schema.md)
- [📖 API Documentation](docs/api-documentation.md)

## 📄 Licencia

Este proyecto está bajo la **Licencia MIT** - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👥 Equipo de Desarrollo

- **Desarrollador Principal**: Jefferson1106
- **Proyecto**: SPF MSA Client Core Service
- **Organización**: Banco Pichincha

---

⭐ **¡Si este proyecto te es útil, no olvides darle una estrella!** ⭐


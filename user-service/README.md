# User Service

Microservicio de gestión de usuarios del proyecto **duoc-music-hub**. Expone una API REST completa con documentación Swagger, respuestas HATEOAS, logs centralizados en Loki y pruebas unitarias sobre la capa de servicio.

---

- [User Service](#user-service)
  - [Estructura del módulo](#estructura-del-módulo)
  - [Dependencias](#dependencias)
  - [Configuración](#configuración)
    - [`application.yaml` completo](#applicationyaml-completo)
  - [El Punto de Partida: La clase `UserServiceApplication`](#el-punto-de-partida-la-clase-userserviceapplication)
  - [Trazabilidad y Monitoreo: La Capa de Logs con `SLF4J`](#trazabilidad-y-monitoreo-la-capa-de-logs-con-slf4j)
    - [Instalación de la dependencia](#instalación-de-la-dependencia)
    - [Configuración del logger](#configuración-del-logger)
    - [Escribir Logs en el código](#escribir-logs-en-el-código)
  - [Contrato y Documentación Viva: Configuración de Swagger/OpenAPi](#contrato-y-documentación-viva-configuración-de-swaggeropenapi)
    - [Configuración de Swagger](#configuración-de-swagger)
    - [URLs disponibles](#urls-disponibles)
    - [Anotaciones útiles en el Controller](#anotaciones-útiles-en-el-controller)
  - [El Núcleo del Negocio: La entidad `User`](#el-núcleo-del-negocio-la-entidad-user)
  - [Evolución de la Base de Datos: Las migraciones con Flyway](#evolución-de-la-base-de-datos-las-migraciones-con-flyway)
  - [Acceso Limpio a los Datos: El repositorio `UserRepository`](#acceso-limpio-a-los-datos-el-repositorio-userrepository)
  - [La Capa de Transferencia de Datos: Los DTOs](#la-capa-de-transferencia-de-datos-los-dtos)
    - [`ApiErrorResponse.java`](#apierrorresponsejava)
    - [`UserRequest`](#userrequest)
    - [`UserResponse`](#userresponse)
  - [Capa de transformación: El Mapper de Usuarios (`UserMapper`)](#capa-de-transformación-el-mapper-de-usuarios-usermapper)
    - [Configuración y Componentes del Mapper](#configuración-y-componentes-del-mapper)
  - [Control Centralizado de Errores: Manejo Global de Excepciones](#control-centralizado-de-errores-manejo-global-de-excepciones)
    - [Las Excepciones Personalizadas (La lógica de negocio)](#las-excepciones-personalizadas-la-lógica-de-negocio)
    - [El Interceptor Global (`GlobalExceptionHandler`)](#el-interceptor-global-globalexceptionhandler)
  - [La Capa de Negocio: Interfaz y Lógica del Servicio (`UserService`)](#la-capa-de-negocio-interfaz-y-lógica-del-servicio-userservice)
    - [La Interfaz Contractual (`UserService`)](#la-interfaz-contractual-userservice)
    - [La Implementación Concreta (`UserServiceImpl`)](#la-implementación-concreta-userserviceimpl)
      - [Análisis de las Reglas de Negocio Implementadas](#análisis-de-las-reglas-de-negocio-implementadas)
  - [La Puerta de Entrada de la API: El Controlador de Usuarios (`UserController`)](#la-puerta-de-entrada-de-la-api-el-controlador-de-usuarios-usercontroller)
    - [Configuración y Decoradores del Controlador](#configuración-y-decoradores-del-controlador)
    - [Enriquecimiento de Respuestas con HATEOAS](#enriquecimiento-de-respuestas-con-hateoas)
    - [Análisis de los Endpoints e Interacción de Capas](#análisis-de-los-endpoints-e-interacción-de-capas)
    - [Documentación Rigurosa del Contrato con OpenAPI (`@ApiResponses`)](#documentación-rigurosa-del-contrato-con-openapi-apiresponses)
    - [Documentación de Pruebas Unitarias: `UserService`](#documentación-de-pruebas-unitarias-userservice)
      - [Estrategia de Testing](#estrategia-de-testing)
      - [Casos de Prueba Cubiertos](#casos-de-prueba-cubiertos)
      - [Estándares de Validación de Código](#estándares-de-validación-de-código)
  - [Ejecutar en local](#ejecutar-en-local)
  - [Verificar el servicio](#verificar-el-servicio)
  - [Dockerización](#dockerización)
    - [Dockerfile](#dockerfile)
    - [Fragmento de `compose.yaml`](#fragmento-de-composeyaml)
  - [Preguntas frecuentes](#preguntas-frecuentes)

## Estructura del módulo

```plaintext
```

## Dependencias

| Dependencia               | Propósito                                                         |
|:--------------------------|:------------------------------------------------------------------|
| `Spring Web`              | API REST con Spring MVC.                                          |
| `Eureka Discovery Client` | Registro en Eureka.                                               |
| `Spring Data JPA`         | Persistencia con JPA e Hibernate.                                 |
| `MySQL Driver`            | Driver JDBC para MySQL.                                           |
| `H2 Database`             | Base de datos en memoria para pruebas.                            |
| `Flyway Migration`        | Migraciones de base de datos versionadas.                         |
| `SpringDoc OpenAPI`       | Documentación Swagger / OpenAPI 3.                                |
| `Spring HATEOAS`          | Soporte para enlaces hipermedia en las respuestas.                |
| `Spring Boot Actuator`    | Endpoints de salud para Docker.                                   |
| `Lombok`                  | Reducción de código repetitivo (getters, setters, constructores). |
| `Loki4j Logback`          | Envío de logs a Loki sin agente intermedio.                       |

> [!IMPORTANT]
> **Flyway** gestiona las migraciones de base de datos mediante scripts SQL versionados. Al arrancar, compara los scripts disponibles contra los ya aplicados y ejecuta solo los pendientes. Esto garantiza que el esquema de la base de datos evolucione de forma controlada y reproducible.
>
> La dependencia de tests `spring-boot-starter-test` ya viene incluida por defecto al generar el proyecto. Incluye `JUnit 5`, `Mockito`, `MockMvc` y `AssertJ`.

## Configuración

### `application.yaml` completo

```yaml
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:mysql://localhost:3306/user_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    database-platform: org.hibernate.dialect.MySQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
server:
  port: 8081
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

> [!IMPORTANT]
> Al aplicar `ddl-auto: validate`, Hibernate valida que el esquema de la base de datos coincida con las entidades JPA, pero no lo modifica. Flyway es el responsable de crear y evolucionar el esquema. Usar `create` o `update` junto con Flyway generaría conflictos.

## El Punto de Partida: La clase `UserServiceApplication`

La clase principal no tiene ningún cambio.

```java
package cl.duoc.music_hub.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

## Trazabilidad y Monitoreo: La Capa de Logs con `SLF4J`

### Instalación de la dependencia

Para el uso de logs avanzados vamos a usar la dependencia [`Loki4j Logback`](https://loki4j.github.io/loki-logback-appender/). Para incluirla en el proyecto, se deben agregar las siguientes líneas en el archivo `pom.xml`:

```xml
<dependency>
    <groupId>com.github.loki4j</groupId>
    <artifactId>loki-logback-appender</artifactId>
    <version>2.0.3</version>
</dependency>
```

### Configuración del logger

Una vez que hayamos agregado la dependencia, debemos crear al archivo de configuración `src/main/resources/logback.xml` con el siguiente contenido:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml" />

    <springProperty scope="context" name="appName" source="spring.application.name" />

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p)
                %clr([${appName},%X{traceId:-},%X{spanId:-}]){yellow} %clr(---){faint}
                %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} : %m%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>

    <logger name="cl.duoc.music_hub" level="DEBUG" />
</configuration>
```

El archivo `logback.xml` sirve principalmente para dos cosas: organizar cómo se ven los mensajes de diagnóstico (logs) en la consola durante el desarrollo y redirigir esos mismos logs hacia un servidor externo (en nuestro caso, Loki) cuando la aplicación corre en producción o dentro de contenedores Docker.

En términos sencillos, actúa como un "enrutador y formateador de mensajes":

- **Estandariza el formato:** Asegura que todos los microservicios muestren la misma estructura de datos en pantalla (Fecha, Hora, Nombre del Servicio, ID de la transacción y Mensaje).
- **Separa canales de salida:** Permite que un mismo log se pinte en la pantalla del programador y, al mismo tiempo, se envíe de forma invisible por la red hacia el stack de monitoreo.
- **Filtra el ruido:** Controla el nivel de detalle de lo que se escribe. Permite silenciar los mensajes internos de Spring Boot que no nos interesan, manteniendo el foco únicamente en los errores o flujos del código que escribieron los alumnos.

### Escribir Logs en el código

Para escribir los logs en nuestro código, tenemos dos posibilidades:

1. Usar la abstracción `SLF4J`:

   ```java
   package cl.duoc.music_hub.user_service.exception;

   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import org.springframework.web.bind.annotation.ExceptionHandler;
   import org.springframework.web.bind.annotation.RestControllerAdvice;

   import cl.duoc.music_hub.user_service.mapper.UserMapper;

   @RestControllerAdvice
   public class GlobalHandlerException {

       // Instancia estática del Logger recomendada para Spring Boot
       private static final Logger log = LoggerFactory.getLogger(UserMapper.class);

       @ExceptionHandler(Exception.class)
       public String handleException(Exception e) {
           log.error("Error en el controlador: ", e);
           return "Ocurrió un error en el servidor. Por favor, inténtelo de nuevo más tarde.";
       }
   }
   ```

2. Usar la anotación `@Slf4j` de Lombok:

   ```java
   package cl.duoc.music_hub.user_service.exception;

   import org.springframework.web.bind.annotation.ExceptionHandler;
   import org.springframework.web.bind.annotation.RestControllerAdvice;

   import lombok.extern.slf4j.Slf4j;

   @RestControllerAdvice
   @Slf4j
   public class GlobalHandlerException {

       @ExceptionHandler(Exception.class)
       public String handleException(Exception e) {
           log.error("Error en el controlador: ", e);
           return "Ocurrió un error en el servidor. Por favor, inténtelo de nuevo más tarde.";
       }
   }
   ```

Como se puede apreciar, la única diferencia es que se reemplazan las importaciones de `org.slf4j.Logger` y `org.slf4j.LoggerFactory` por `lombok.extern.slf4j.Slf4j`, y se elimina la instrucción `private static final Logger log = LoggerFactory.getLogger(UserMapper.class);`.

## Contrato y Documentación Viva: Configuración de Swagger/OpenAPi

Con la dependencia `SpringDoc OpenAPI`, Spring Boot genera automáticamente la documentación de la API.

### Configuración de Swagger

Para usar Swagger en nuestro servicio, vamos a agregar el archivo `src/main/cl/duoc/music_hub/user_service/config/SwagerConfig.java`:

```java
package cl.duoc.music_hub.user_service.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Usuarios")
                        .version("1.0")
                        .description("API para la gestión de usuarios"))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Servidor local"),
                        new Server().url("http://localhost:8080").description("Vía API Gateway")));
    }
}
```

### URLs disponibles

| URL                                     | Descripción                            |
|:----------------------------------------|:---------------------------------------|
| `http://localhost:8081/swagger-ui.html` | Interfaz gráfica interactiva           |
| `http://localhost:8081/v3/api-docs`     | Especificación OpenAPI en formato JSON |

### Anotaciones útiles en el Controller

```java
@Operation(summary = "Obtiene un autor por ID")
@ApiResponse(responseCode = "200", description = "Recurso encontrado")
@ApiResponse(responseCode = "404", description = "Recurso no encontrado")
@GetMapping("/{id}")
public ResponseEntity<EntityModel<AutorDTO>> buscarPorId(@PathVariable Long id) { ... }
```

## El Núcleo del Negocio: La entidad `User`

| Campo | Tipo SQL | Tipo Java | Descripción | Reglas y Anotaciones sugeridas |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | `Long` | Identificador único | Llave primaria (`@Id`), estrategia de generación `IDENTITY` |
| `username` | `VARCHAR(50)` | `String` | Nombre de usuario | Obligatorio (`nullable = false`), único (`unique = true`), largo máximo 50 |
| `email` | `VARCHAR(150)` | `String` | Correo electrónico | Obligatorio (`nullable = false`), único (`unique = true`), largo máximo 150 |
| `fullName` | `VARCHAR(200)` | `String` | Nombre completo del usuario | Obligatorio (`nullable = false`), largo máximo 200, mapeado a `full_name` |
| `createdAt` | `TIMESTAMP` | `LocalDateTime` | Fecha de creación | Obligatorio, no modificable (`updatable = false`), mapeado a `created_at`. Se puede usar `@CreationTimestamp` |
| `updatedAt` | `TIMESTAMP` | `LocalDateTime` | Fecha de actualización | Obligatorio, mapeado a `updated_at`. Se puede usar `@UpdateTimestamp` |

## Evolución de la Base de Datos: Las migraciones con Flyway

- `src/main/resources/db/migration/V1__create_users_table.sql`:

  ```sql
  CREATE TABLE users (
      id BIGINT GENERATED BY DEFAULT AS IDENTITY,
      username VARCHAR(50) NOT NULL,
      email VARCHAR(150) NOT NULL,
      full_name VARCHAR(200) NOT NULL,
      created_at TIMESTAMP NOT NULL,
      updated_at TIMESTAMP NOT NULL,
      CONSTRAINT pk_users PRIMARY KEY (id),
      CONSTRAINT uq_users_username UNIQUE (username)
  );
  ```

- `src/main/resources/db/migration/V2__insert_initial_users.sql`:

  ```sql
  INSERT INTO users (username, email, full_name, created_at, updated_at) VALUES 
  ('bats', 'bruce.wayne@duocuc.cl', 'Bruce Wayne', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('smallville', 'clark.kent@duocuc.cl', 'Clark Kent', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('diana', 'diana.prince@duocuc.cl', 'Diana Prince', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
  ```

## Acceso Limpio a los Datos: El repositorio `UserRepository`

Ahora que la base de datos ya tiene estructura y datos listos con Flyway, nos toca crear la interfaz para conectar nuestro código con esa tabla. Esto va dentro del paquete cl.duoc.music_hub.user_service.repository.

Para resolver esto, puedes elegir el camino que más te acomode según cómo quieras estructurar tu código. Tienes dos opciones válidas:

- **Opción A - `ListCrudRepository`:** Es la alternativa moderna. Si solo quieres hacer las operaciones básicas (guardar, buscar, borrar) de la forma más limpia posible, esta es tu opción. Lo mejor que tiene es que su método para listar todo te devuelve un `List<User>` directo de Java, sin tener que andar lidiando con transformaciones raras.
- **Opción B - `JpaRepository:`** Es el clásico de siempre. Elígelo si estás pensando a futuro y crees que la aplicación va a necesitar ordenar los datos o meter paginación (*Pageable*) porque la lista de usuarios se nos puede ir de las manos.

Sin importar cuál de las dos opciones, vamos a definir dos búsquedas personalizadas usando **Query Methods**:

- **Buscar por `username` exacto:** Necesitamos encontrar a un usuario por su nombre de cuenta. Ojo aquí: como el usuario puede existir o no, la regla de oro es proteger el código contra caídas de puntero nulo (`NullPointerException`). Así que este método debe retornar un contenedor seguro del tipo `Optional`.
- **Buscar por `email` exacto:** Otra forma de buscar un usuario único es por su cuenta de correo. Al igual que con el `username` el usuario puedo o no existir, por lo que debemos protegernos de caídas de puntero nulo (`NullPointerException`) y retornar un contenedor del tipo `Optional`.
- **Buscar por coincidencia en el `email`:** Necesitamos crear un método que haga una búsqueda parcial y que nos devuelva un `List<User>` con todos los que calcen.

> [!TIP]
> Tenemos que poner especial atención a las palabras clave que usamos en el nombre de los métodos `findBy...`. Un solo error de tipeo y Spring Boot no va a levantar cuando intentemos correr el proyecto.

## La Capa de Transferencia de Datos: Los DTOs

Para que nuestra API sea segura y ordenada, nunca debemos exponer la entidad de la base de datos (`User`) directamente en los controladores. En su lugar, usaremos **DTOs** (*Data Transfer Objects*) bien definidos en el paquete `cl.duoc.music_hub.user_service.dto`.

> [!TIP]
> **Documentación con OpenAPI / Swagger:** Cada componente puede ser enriquecido con la anotación `@Schema` tanto a nivel de clase como en sus atributos. Esto permite definir descripciones técnicas y valores de ejemplo (mediante la propiedad `example`), logrando que la interfaz de **Swagger UI** actúe como una documentación viva y autoexplicativa del microservicio.

A continuación, se detalla la estructura y propósito de los tres DTOs del sistema:

### `ApiErrorResponse.java`

Este objeto es el encargado de unificar y formatear cualquier excepción que ocurra dentro del microservicio. Su objetivo es evitar que el cliente reciba un volcado de memoria interno (stack trace), entregando en su lugar una respuesta JSON clara y predecible.

- **Campos incluidos:**
  - `timestamp`: Fecha y hora exacta en la que se gatilló el error.
  - `status`: Código de estado HTTP numérico (por ejemplo, 404).
  - `error`: Nombre del error HTTP asociado (por ejemplo, "Not Found").
  - `message`: Mensaje amigable que explica la causa del fallo (por ejemplo, "El usuario consultado no existe").
  - `path`: La URL exacta de la API que intentó consumir el cliente.
  - `errors`: Una lista de strings opcional que detalla múltiples fallas en paralelo, diseñado principalmente para acumular errores de validación de campos.
- **Comportamiento de serialización:** La clase cuenta con la regla de exclusión de nulos (`@JsonInclude(JsonInclude.Include.NON_NULL)`). Si el campo errors no contiene datos, simplemente se omite del JSON final para mantener la respuesta limpia.

### `UserRequest`

Este DTO está diseñado exclusivamente para capturar la información que viaja desde el cliente hacia la API durante los flujos de creación (`POST`) o actualización (`PUT`).

- **Campos incluidos:** Únicamente los datos que el cliente tiene permitido manipular: `username`, `email` y `fullName`.
- **Regla de diseño:** Excluye deliberadamente campos internos como el `id` (manejado por la estrategia autoincremental de la BD) o las marcas de tiempo de auditoría (`createdAt`, `updatedAt`).

> [!TIP]
> Las anotaciones `@Schema` aquí muestran ejemplos de formatos válidos para el registro de un usuario.

### `UserResponse`

Este es el DTO que el microservicio devuelve al cliente tras una consulta exitosa. Al estar diseñado bajo el paradigma de hipermedios, cuenta con una particularidad estructural.

- **Campos incluidos:** Toda la información pública y técnica del usuario: `id`, `username`, `email`, `fullName`, `createdAt` y `updatedAt`.
- **Comportamiento de serialización:** La clase cuenta con la regla de exclusión de nulos (`@JsonInclude(JsonInclude.Include.NON_NULL)`). Si el campo errors no contiene datos, simplemente se omite del JSON final para mantener la respuesta limpia.
- **Estructura HATEOAS:** Para poder inyectar dinámicamente enlaces interactivos (como el link de autoreferencia `"self"`), esta clase hereda directamente de `RepresentationModel<UserResponse>`, la clase de soporte provista por Spring HATEOAS.

> [!TIP]
> Las anotaciones `@Schema` aquí permiten mostrar ejemplos de los datos que se retornan.

## Capa de transformación: El Mapper de Usuarios (`UserMapper`)

Para realizar la conversión de datos entre la entidad de base de datos (`User`) y los objetos de transferencia que viajan por la red (`UserRequest` y `UserResponse`), el sistema utiliza una interfaz de mapeo ubicada en el paquete `cl.duoc.music_hub.user_service.mapper`.

### Configuración y Componentes del Mapper

- **Integración con Spring:** La clase está decorada con la anotación `@Component`. Esto la transforma en un bean administrado por el contenedor de Spring, permitiendo que sea inyectada mediante `@Autowired` o por constructor en cualquier Service o Controller que la requiera.
- **Conversiones Soportadas:** El componente expone tres métodos clave para el flujo de datos:
  - **De `UserRequest` a `User`:** Instancia una nueva entidad `User` y le traspasa los datos limpios de creación enviados por el cliente (`username`, `email`, `fullName`). Por seguridad, este método inicializa el objeto asegurando que el `id` y las fechas de auditoría queden en manos de la base de datos y no del cliente.
  - **De `User` a `UserResponse`:** Toma el registro completo recuperado de la base de datos (incluyendo su `id` autogenerado y las marcas de tiempo `createdAt` y `updatedAt`) y los transfiere uno a uno al DTO de salida utilizando el patrón Builder de Lombok.

## Control Centralizado de Errores: Manejo Global de Excepciones

Para evitar que el microservicio exponga detalles internos del servidor (como trazas de código o errores de SQL) cuando algo sale mal, el sistema utiliza un esquema de **Manejo Global de Excepciones** ubicado en el paquete `cl.duoc.music_hub.user_service.exception`.

Este modelo se desacopla de los controladores individuales, asegurando que cualquier error en la aplicación sea interceptado, registrado en los logs y devuelto al cliente bajo el formato estandarizado de `ApiErrorResponse`.

### Las Excepciones Personalizadas (La lógica de negocio)

Se han definido dos excepciones de tipo unchecked (que heredan de RuntimeException) para representar los escenarios de falla más comunes en la gestión de usuarios:

- **`ResourceNotFoundException`:** Se gatilla cuando un cliente solicita un recurso que no existe en el sistema (por ejemplo, buscar un usuario por un `id` o `username` que no está en la base de datos).
- **`ResourceConflictException`:** Se activa cuando la operación viola una regla de unicidad o consistencia del negocio (por ejemplo, intentar registrar un nuevo usuario utilizando un `username` o un `email` que ya están tomados por otra persona).

### El Interceptor Global (`GlobalExceptionHandler`)

Para capturar estas excepciones en tiempo de ejecución, se ha implementado una clase especial decorada con la anotación `@RestControllerAdvice`. Esta clase actúa como un "filtro de seguridad" que intercepta los errores antes de que salgan de la API.

Dentro de este componente, se utilizan métodos anotados con `@ExceptionHandler`, los cuales se encargan de:

- **Atrapar el error específico:** Escuchar activamente cuándo se lanza un ResourceNotFoundException o un ResourceConflictException.
- **Asignar el Estado HTTP correcto:**
  - Si falta el recurso, el interceptor responde con un `404 Not Found`.
  - Si hay un choque de datos, responde con un `409 Conflict`.
  - Para cualquier otra falla imprevista del sistema (como una caída de la BD), se cuenta con un manejador genérico que responde con un `500 Internal Server Error`.
- **Construir la Respuesta:** Tomar los detalles del error, combinarlos con el `HttpServletRequest` para obtener la ruta (`path`) que falló, y empaquetar todo dentro del DTO `ApiErrorResponse` con la fecha y hora exacta del evento.

## La Capa de Negocio: Interfaz y Lógica del Servicio (`UserService`)

Para garantizar el desacoplamiento y seguir las buenas prácticas de diseño de software (principios SOLID), la capa de negocio del microservicio se encuentra dividida en un contrato de interfaz y su clase de implementación concreta dentro del paquete `cl.duoc.music_hub.user_service.service`.

A continuación, se analiza la estructura y el comportamiento de ambos componentes:

### La Interfaz Contractual (`UserService`)

Representa el contrato público que el microservicio expone a las capas superiores (como el controlador). Define de manera abstracta qué operaciones de negocio están disponibles, ocultando el cómo se resuelven internamente.

El archivo define los siguientes métodos utilizando tipos genéricos seguros y los DTOs ya analizados:

```java
package cl.duoc.music_hub.user_service.service;

import java.util.List;
import cl.duoc.music_hub.user_service.dto.UserRequest;
import cl.duoc.music_hub.user_service.dto.UserResponse;

public interface UserService {
    List<UserResponse> findAll();
    UserResponse findById(Long id);
    UserResponse findByUsername(String username);
    UserResponse create(UserRequest request);
    UserResponse update(Long id, UserRequest request);
    void delete(Long id);
}
```

### La Implementación Concreta (`UserServiceImpl`)

Esta clase contiene la lógica real de la aplicación. Está decorada con la anotación `@Service`, lo que le permite a Spring Boot gestionarla como un Bean e inyectarla donde sea necesario.

Para realizar su trabajo, este componente orquesta de manera limpia las tres piezas clave del microservicio creadas previamente:

1. **`UserRepository`:** Para la persistencia de datos en la BD.
2. **`UserMapper`:** Para transformar manualmente las entidades en DTOs y viceversa.
3. **`@Slf4j`:** Para registrar la trazabilidad de cada acción en la consola de servidores.

#### Análisis de las Reglas de Negocio Implementadas

- **Inyección por Constructor:** En lugar de utilizar `@Autowired` sobre los atributos (una práctica propensa a fallos de inicialización), la clase implementa la inyección de dependencias directamente a través de su constructor (aprovechando la anotación `@RequiredArgsConstructor` de Lombok), asegurando que el Repositorio y el Mapper manual estén listos al arrancar.
- **Integración de Transaccionalidad (`@Transactional`):** Para garantizar la integridad y consistencia de los datos en la base de datos, los métodos de la implementación hacen uso de la anotación `@Transactional` de Spring:
  - En las operaciones de lectura (`findAll`, `findById`, `findByUsername`), se configura como `readOnly = true`, lo que permite optimizar el rendimiento a nivel de base de datos al evitar que el proveedor de persistencia (Hibernate) realice chequeos de cambios (dirty checking).
  - En las operaciones de escritura o mutación (`create`, `update`, `delete`), se aplica el comportamiento por defecto de `@Transactional`. Esto asegura que cada método se ejecute dentro de una transacción segura; si ocurre cualquier excepción de negocio o error imprevisto a mitad de camino, Spring gatillará un rollback automático, dejando la base de datos intacta.
- **Manejo de Errores de Lectura (404 Not Found):** En los métodos `findById`, `update` y `delete`, el servicio utiliza el método `.findById(id)` del repositorio, el cual devuelve un objeto `Optional`. Si el usuario no existe, se hace uso de `.orElseThrow()`, gatillando de inmediato un `ResourceNotFoundException` con un mensaje personalizado.
- **Validación de Duplicados en Creación (409 Conflict):** Durante el flujo de `create`, el servicio valida preventivamente mediante los Query Methods del repositorio si el `username` o el `email` ya están registrados en la base de datos. En caso afirmativo, detiene la operación lanzando un `ResourceConflictException`.
- **Persistencia Segura en Actualizaciones (`PUT`):** En el método `update`, antes de guardar los cambios, el servicio realiza una doble verificación en la base de datos para asegurar la consistencia:
  - Busca si el nuevo `username` ya existe en el sistema. Si existe y pertenece a un `id` distinto al que se está editando, lanza un `ResourceConflictException`.
  - Busca si el nuevo `email` ya está registrado. De igual forma, si pertenece a otro usuario, frena la operación con un `ResourceConflictException`.
  Una vez superadas las validaciones de conflicto en el `update`, el servicio modifica directamente el estado de la entidad recuperada con los nuevos datos provistos por el `UserRequest`. Finalmente, se invoca al repositorio para persistir la entidad modificada y se transforma el resultado al DTO de salida usando el método `toResponse` del mapper. Esto garantiza que propiedades críticas de auditoría (como `createdAt`) permanezcan inalteradas.

## La Puerta de Entrada de la API: El Controlador de Usuarios (`UserController`)

La capa de presentación del microservicio se encuentra centralizada en la clase `UserController`. Este componente se encarga de exponer los endpoints HTTP públicos que permiten interactuar con el recurso "Usuarios", gestionando los códigos de estado HTTP de forma estandarizada y enriqueciendo las respuestas con hipermedios (HATEOAS) y documentación viva (OpenAPI).

### Configuración y Decoradores del Controlador

- **`@RestController`:** Indica a Spring que esta clase es un punto de entrada de API REST donde todas las respuestas se serializarán automáticamente a formato JSON.
- **`@RequestMapping("/api/v1/users")`:** Establece el prefijo base de la URL y la estrategia de versionamiento (v1) para todos los endpoints expuestos en este recurso, siguiendo las mejores prácticas de diseño de APIs RESTful.
- **`@Tag(name = "User Management", description = "Endpoints para el ciclo de vida de usuarios")`:** Anotación de OpenAPI que agrupa y etiqueta visualmente este controlador dentro de la interfaz gráfica de Swagger UI.

### Enriquecimiento de Respuestas con HATEOAS

Para cumplir con el nivel 3 del Modelo de Madurez de Richardson (REST avanzado), el controlador inyecta hipermedios a los DTOs de salida utilizando **Spring HATEOAS**.

Cada vez que se retorna un `UserResponse`, el controlador no solo envía los datos del usuario, sino que le adjunta enlaces dinámicos (`Links`) que le indican al cliente qué acciones puede realizar a continuación. Esto se logra mediante la utilidad `WebMvcLinkBuilder`:

- **Enlace `self`:** Un link directo al recurso individual consultado (ej. `/api/v1/users/1`).
- **Enlace `all-users`:** Un link relacional que apunta al listado completo de usuarios, permitiendo la navegabilidad del ecosistema sin que el cliente tenga que conocer las URLs de memoria.

### Análisis de los Endpoints e Interacción de Capas

El controlador expone 6 operaciones clave mapeadas a los verbos HTTP correspondientes:

- **`GET /` (Listar Todo):** Recupera todos los usuarios desde el UserService, mapea la colección y le inyecta a cada elemento su respectivo enlace self. Retorna un estado `200 OK`.
- **`GET /{id}` (Buscar por ID):** Solicita el usuario por su identificador único. Si existe, le añade los enlaces HATEOAS y retorna un `200 OK`. Si no existe, el servicio lanza un `ResourceNotFoundException` que es interceptado por el manejador global, devolviendo un `404 Not Found`.
- **`GET /search` (Buscar por Username):** Expone un endpoint de consulta mediante un parámetro de URL (`@RequestParam String username`). Mantiene el flujo HATEOAS y retorna un `200 OK`.
- **`POST /` (Creación):** Recibe un objeto `UserRequest`. Aquí se aplica la anotación `@Valid`, gatillando de inmediato las validaciones de Lombok/Jakarta (como `@Email` o `@NotBlank`). Si la estructura falla, se lanza un `MethodArgumentNotValidException` (que devuelve un `400 Bad Request` con la lista de errores). Si pasa las validaciones y el servicio lo crea con éxito, el controlador responde con un estado `201 Created`, incluyendo el recurso creado en el cuerpo de la respuesta.
- **`PUT /{id}` (Actualización):** Recibe el `id` en la URL y el `UserRequest` validado en el cuerpo. Ejecuta la lógica de modificación directa del servicio y retorna un `200 OK` con el objeto actualizado y sus nuevos enlaces.
- **`DELETE /{id}` (Eliminación):** Solicita la baja de un usuario. Al ser una operación que no devuelve cuerpo de respuesta, el controlador utiliza de forma óptima el código de estado `204 No Content` para confirmar que la acción fue procesada exitosamente en el servidor.

### Documentación Rigurosa del Contrato con OpenAPI (`@ApiResponses`)

Para transformar la interfaz de Swagger UI en una verdadera guía interactiva de desarrollo, cada endpoint ha sido blindado con anotaciones de documentación de OpenAPI (`@ApiResponses` y `@ApiResponse`). Esto autogenera la documentación del servicio detallando de forma exacta los flujos de éxito y de error:

- **Códigos de Éxito (`200 OK`, `201 Created`, `204 No Content`):** Mapean los escenarios donde el negocio fluye correctamente, indicándole al cliente cuándo recibirá datos de vuelta o cuándo la acción se procesó sin devolver cuerpo (como en el `DELETE`).
- **Códigos de Error Estructurados (`400 Bad Request`, `404 Not Found`, `409 Conflict`):** Le demuestran al desarrollador que el microservicio está preparado para fallar de forma controlada. Cada uno de estos escenarios está explícitamente enlazado en Swagger al esquema del DTO `ApiErrorResponse`, asegurando que el cliente conozca de antemano la estructura del JSON de error que recibirá ante cualquier excepción.
- **Error Genérico de Blindaje (`500 Internal Server Error`):** Se ha incluido explícitamente en todos los métodos para documentar la existencia de un "mecanismo de seguridad". Este enlace advierte que, si ocurriese una falla crítica e imprevista en el servidor (como la caída de la base de datos), el interceptor global la capturará y devolverá un ApiErrorResponse genérico y seguro, garantizando que el microservicio responda consistentemente incluso en su peor escenario.

### Documentación de Pruebas Unitarias: `UserService`

Esta sección detalla la estrategia, cobertura y diseño de los casos de prueba automatizados para la capa de negocio del microservicio de usuarios, utilizando **JUnit 5** y **Mockito** para el aislamiento de dependencias.

#### Estrategia de Testing

Las pruebas del servicio se configuran como pruebas unitarias puras. No levantan el contexto de Spring Boot (`@SpringBootTest`), garantizando una velocidad de ejecución óptima en el pipeline de Integración Continua (CI/CD) al simular la capa de persistencia.

- Framework Principal: JUnit 5 (Jupiter)
- Librería de Mocks: Mockito 5.x
- Patrón de Diseño de Tests: AAA (Arrange, Act, Assert)

#### Casos de Prueba Cubiertos

A continuación se detallan los escenarios validados en el componente de lógica de negocio:

| Id | Caso de Prueba | Entrada (Input) | Comportamiento Esperado (Output) | Tipo |
| :--- | :--- | :--- | :--- | :--- |
| TC-01 | Obtiene todos los usuario | N/A | Retorna todos los usuarios | Feliz |
| TC-02 | Búsqueda exitosa por ID | id = 1L (Existente) | Retorna el objeto `User` con sus datos mapeados correctamente | Feliz |
| TC-03 | Búsqueda de ID inexistente | id = 99L (No existe) | Lanza una excepción `ResourceNotFoundException` | Alternativo |
| TC-04 | Registro de usuario nuevo | Objeto `User` válido | Persiste en BD y retorna el usuario creado | Feliz |
| TC-05 | Registro con Email duplicado | `email` ya registrado | Lanza `ResourceConflictException` | Alternativo |

#### Estándares de Validación de Código

1. **Independencia Total:** Ningún test puede depender del resultado de otro. Se debe usar `@BeforeEach` para limpiar y resetear las entidades de prueba.
2. **Semántica en Mensajes:** Los métodos deben portar la anotación `@DisplayName` especificando el ID del caso de prueba y una descripción clara en español del comportamiento esperado.
3. **Verificación de Mocks:** Todo flujo completado con éxito debe certificar mediante `verify()` que las llamadas a los repositorios ocurrieron bajo los parámetros esperados, evitando ejecuciones fantasma de código.

## Ejecutar en local

## Verificar el servicio

## Dockerización

### Dockerfile

El `Dockerfile` usa un patrón multi-stage build: la imagen de construcción (con Maven y el JDK completo) se descarta al final, y solo el artefacto compilado pasa a la imagen de ejecución liviana. Esto reduce el tamaño de la imagen final y evita incluir herramientas de desarrollo en producción.

```dockerfile
# Etapa 1: Compilar ------------------------------------------------------------------------------------
# Imagen base con Maven 3.9 y JDK 21 integrados. El alias 'build' permite referenciarla desde la etapa 2
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Se copia el 'pom.xml' antes que el código fuente para aprovechar la caché de capas de Docker
COPY pom.xml .
# Descarga todas las dependencias y las deja en la caché local de Maven. Si el `pom.xml` no cambia,
# Docker reutiliza esta capa sin volver a descargar nada
RUN mvn dependency:go-offline -q

# Recién aquí se copia el código fuente
COPY src ./src

# Compila y empaqueta el JAR. Se omiten los tests porque en la imagen de producción no corresponde ejecutarlos
RUN mvn package -DskipTests -q

# Etapa 2: Crear la imagen final -----------------------------------------------------------------------
# Imagen mínima con solo el JRE (runtime) sobre Alpine Linux. Sin Maven ni JDK
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crea un usuario sin privilegios de sistema. Buena práctica de seguridad: el proceso Java no corre como 'root'
RUN addgroup -S spring && adduser -S spring -G spring
# A partir de acá, todas las instrucciones y el proceso final se ejecutan con el usuario 'spring'
USER spring

# Copia solo el JAR generado desde la etapa 'build', descartando Maven, el JDK y los archivos fuente
COPY --from=build /app/target/*.jar app.jar

# Documenta el puerto que usará el contenedor (no lo publica; eso lo hace 'docker-compose.yml')
EXPOSE 8081

# Comando de arranque en formato exec (sin shell intermedio), lo que permite que las señales del
# sistema operativo lleguen directamente al proceso Java
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Fragmento de `compose.yaml`

```yaml
# Definición del microservicio
user:
  build: ./user-service
  ports:
    - "8081:8081"
  environment:
    - SPRING-DATASOURCE-URL=jdbc:mysql://mysql:3306/user_db?useSSL=false&allowPublicKeyRetrieval=true
    - SPRING-DATASOURCE-USERNAME=${DATABASE_USERNAME}
    - SPRING-DATASOURCE-PASSWORD=${DATABASE_PASSWORD}
    - EUREKA-CLIENT-SERVICEURL-DEFAULTZONE=http://${EUREKA_HOST}:8761/eureka/
  healthcheck:
    test: ["CMD", "wget", "-qO-", "http://localhost:8081/actuator/health"]
    interval: 15s
    timeout: 5s
    retries: 5
  depends_on:
    eureka:
      condition: service_healthy
    mysql:
      condition: service_healthy
  networks:
    - subnet
```

> [!NOTE]
> Los demás servicios que dependan de `user-service` deberán declarar `depends_on: user` con `condition: service_healthy` para garantizar que esté disponible antes de intentar conectarse a él.

## Preguntas frecuentes

- **¿Por qué Flyway y no `ddl-auto: create`?**

  `ddl-auto: create` destruye y recrea el esquema cada vez que la aplicación arranca, lo que significa pérdida de datos. Flyway aplica solo los scripts nuevos, mantiene un historial de migraciones y permite evolucionar el esquema de forma controlada y auditable.

- **¿Por qué separar el DTO de la entidad JPA?**

  Exponer la entidad JPA directamente genera problemas: se pueden filtrar datos sensibles, las relaciones lazy de Hibernate pueden causar errores de serialización JSON, y cualquier cambio en el modelo de datos rompe el contrato de la API. El DTO actúa como contrato estable entre el servicio y sus clientes.

- **¿Por qué los tests no usan `@SpringBootTest`?**

  `@SpringBootTest` levanta el contexto completo de Spring, lo que incluye conectarse a la base de datos, a Eureka y a todos los beans de la aplicación. Esto hace los tests lentos y dependientes de infraestructura externa. Con `@ExtendWith(MockitoExtension.class)` los tests corren en milisegundos y sin dependencias externas.

- **¿Qué pasa si `history-service` llama a `user-service` y este no está disponible?**

  Sin manejo de errores, `history-service` recibirá una excepción de conexión. En este proyecto educativo, ese escenario se documenta pero no se implementa un **Circuit Breaker** (eso corresponde a un nivel más avanzado). Se recomienda al menos capturar la excepción y retornar una respuesta degradada.

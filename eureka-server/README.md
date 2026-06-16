# Eureka Server

Servidor de registro y descubrimiento de servicios para el proyecto **biblioteca-duoc**. Todos los microservicios y el API Gateway se registran aquí al arrancar, y lo consultan para encontrarse entre sí sin necesidad de URLs hardcodeadas.

---

## Tabla de contenidos

- [Eureka Server](#eureka-server)
  - [Tabla de contenidos](#tabla-de-contenidos)
  - [Estructura del módulo](#estructura-del-módulo)
  - [Dependencias Maven](#dependencias-maven)
  - [Configuración](#configuración)
  - [Clase principal](#clase-principal)
  - [Ejecutar en local](#ejecutar-en-local)
  - [Verificar el servidor](#verificar-el-servidor)
    - [Dashboard web](#dashboard-web)
    - [Endpoint de salud](#endpoint-de-salud)
    - [Endpoint del registro (API REST interna)](#endpoint-del-registro-api-rest-interna)
  - [Dockerización](#dockerización)
    - [Dockerfile](#dockerfile)
    - [Fragmento en compose.yaml](#fragmento-en-composeyaml)
  - [Preguntas frecuentes](#preguntas-frecuentes)

## Estructura del módulo

```plaintext
eureka-server/
├── src/
│   └── main/
│       ├── java/
│       │   └── cl/duoc/music_hub/eureka_server/
│       │       └── EurekaServerApplication.java
│       └── resources/
│           └── application.yaml
├── Dockerfile
└── pom.xml
```

## Dependencias Maven

El `eureka-server` necesita solo dos dependencias:

| Dependencia            | Propósito                                                                     |
|:-----------------------|:------------------------------------------------------------------------------|
| `Eureka Server`        | Habilita el servidor Eureka completo                                          |
| `Spring Boot Actuator` | Expone endpoints de salud (`/actuator/health`) útiles para Docker y monitoreo |

## Configuración

El archivo `application.yaml` tiene tres responsabilidades:

1. Identificar el servidor
2. Definir el puerto
3. Evitar que se registre a sí mismo

```yaml
spring:
  application:
    name: eureka-server
eureka:
  client:
    # Evita que el propio servidor intente registrarse como si fuera un cliente
    register-with-eureka: false
    # No intenta obtener el registro de otro Eureka
    fetch-registry: false
    # Establece la ruta por defecto
    service-url:
      defaultZone: http://${EUREKA_HOSTNAME:localhost}:${server.port}/eureka/
  # Configura la instancia
  instance:
    # Establece el nombre del host
    hostname: ${EUREKA_HOSTNAME:localhost}
    # Indica a los microservicios que se registren utilizando su dirección IP
    # en lugar de su nombre de host
    prefer-ip-address: true
server:
  port: 8761
```

El puerto `8761` es la convención estándar para Eureka. Los clientes lo buscan en este puerto por defecto. La expresión `${EUREKA_HOSTNAME:localhost}` le indica al servicio que debe usar el valor del la variable de entorno `EUREKA_HOST` y, si no está establecida, el valor por defecto `localhost`. La expresión `${server.port}` establece que se va a usar el valor definido en `server.port`.

> [!IMPORTANT]
> **¿Por qué estas propiedades?** Internamente, Spring Cloud incluye también las dependencias de cliente Eureka. Sin esta configuración, el servidor intentaría registrarse en sí mismo y mostraría errores en consola al no encontrar a quién conectarse.

## Clase principal

La única modificación a una clase Spring Boot estándar es la anotación `@EnableEurekaServer`:

```java
package cl.duoc.music_hub.eureka_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

`@EnableEurekaServer` activa toda la infraestructura del servidor: el endpoint REST que usan los clientes para registrarse, el dashboard web y el mecanismo de heartbeat.

## Ejecutar en local

**Con Maven:**

```bash
cd eureka-server
./mvnw spring-boot:run
```

**Con el JAR generado:**

```bash
cd eureka-server
./mvnw clean package -DskipTests
java -jar target/eureka-server-0.0.1-SNAPSHOT.jar
```

El servidor estará listo cuando veas en consola:

```plaintext
Started EurekaServerApplication in X.XXX seconds
```

## Verificar el servidor

### Dashboard web

Abre el navegador en <http://localhost:8761>. Verás el dashboard de Eureka con:

- Estado general del servidor
- Sección **"Instances currently registered with Eureka"** — inicialmente vacía, se poblará cuando levantes los demás servicios
- Información del entorno: zona, región, modo de auto-preservación

### Endpoint de salud

```bash
curl http://localhost:8761/actuator/health
# Respuesta esperada: {"status":"UP"}
```

### Endpoint del registro (API REST interna)

```bash
curl -H "Accept: application/json" http://localhost:8761/eureka/apps
```

Responde con el listado completo de servicios registrados en formato JSON. Útil para depurar problemas de descubrimiento.

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
EXPOSE 8761

# Comando de arranque en formato exec (sin shell intermedio), lo que permite que las señales del
# sistema operativo lleguen directamente al proceso Java
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Fragmento en compose.yaml

```yaml
eureka-server:
  build: ./eureka-server
  container_name: eureka-server
  ports:
    - "8761:8761"
  environment:
    - EUREKA_HOST=eureka-server
  healthcheck:
    test: ["CMD", "wget", "-qO-", "http://localhost:8761/actuator/health"]
    interval: 15s
    timeout: 5s
    retries: 5
  networks:
    - duoc-music-hub-net
```

> [!NOTE]
> Los demás servicios declararán `depends_on: eureka-server` con `condition: service_healthy` para garantizar que Eureka esté disponible antes de intentar registrarse.

## Preguntas frecuentes

- **¿Por qué el dashboard muestra `"EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT"`?**

  Es el **modo de auto-preservación** activado. Eureka lo activa cuando detecta que muchos clientes dejaron de enviar heartbeats (por ejemplo, al detener servicios en desarrollo). No es un error: es una medida de seguridad para no eliminar registros ante una eventual partición de red. En entornos de desarrollo puedes desactivarlo con `eureka.server.enable-self-preservation: false`, pero **no se recomienda en producción**.

- **¿Qué pasa si Eureka cae mientras los servicios están corriendo?**

  Los clientes Eureka guardan una copia local del registro en caché. Pueden seguir funcionando durante un tiempo con esa caché, aunque no verán nuevos registros ni actualizaciones hasta que Eureka vuelva a estar disponible.

- **¿El tráfico entre microservicios pasa por Eureka?**

  No. Eureka solo sirve para el **descubrimiento** (saber la dirección de un servicio). Una vez que un cliente conoce la dirección, la comunicación es **directa** entre los dos servicios, sin pasar por Eureka.

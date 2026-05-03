# Contexto de Infraestructura y DevContainers

## Introducción

Este documento describe el entorno de desarrollo reproducible y portable basado en Dev Containers con Docker Compose para el proyecto POS de restaurante. El stack tecnológico consiste en un backend Java con Quarkus y Apache Camel, un frontend Angular, y una base de datos PostgreSQL, todos orquestados mediante Docker Compose.

El objetivo es que cualquier desarrollador pueda incorporarse al proyecto y tener un entorno funcional y consistente en minutos, independientemente de su sistema operativo host, eliminando el problema de "funciona en mi máquina".

---

## Glosario de Infraestructura

- **DevContainer**: Entorno de desarrollo basado en contenedor Docker, definido por archivos de configuración en `.devcontainer/`, que provee un ambiente de desarrollo reproducible.
- **Container_Runtime**: Motor de contenedores utilizado para ejecutar el DevContainer (Docker Desktop, Podman, etc.).
- **Host_Machine**: La máquina física o virtual del desarrollador donde se ejecuta el Container_Runtime.
- **IDE**: Entorno de desarrollo integrado compatible con devcontainers (VS Code, JetBrains, etc.).
- **devcontainer.json**: Archivo de configuración principal del DevContainer que define la orquestación Docker Compose, extensiones, puertos y configuraciones del entorno.
- **Docker_Compose**: Herramienta para definir y ejecutar aplicaciones multi-contenedor mediante un archivo `docker-compose.yml`.
- **Compose_File**: Archivo `docker-compose.yml` que define los servicios, redes y volúmenes del entorno de desarrollo.
- **Backend_Service**: Contenedor que ejecuta la aplicación Java con Quarkus y Apache Camel.
- **Frontend_Service**: Contenedor que ejecuta la aplicación Angular con Node.js.
- **Database_Service**: Contenedor que ejecuta el motor de base de datos del proyecto (PostgreSQL).
- **Workspace**: Directorio del proyecto montado dentro del DevContainer.
- **Extension**: Plugin del IDE instalado automáticamente dentro del DevContainer.
- **Feature**: Componente de devcontainer que instala herramientas o configuraciones adicionales de forma modular.
- **Postcommand**: Comando ejecutado automáticamente después de que el DevContainer es creado o reconstruido.
- **Quarkus**: Framework Java supersónico y subatómico para el desarrollo de microservicios y aplicaciones nativas en la nube.
- **Apache_Camel**: Framework de integración Java basado en patrones de integración empresarial (EIP).
- **Quarkus_CLI**: Herramienta de línea de comandos oficial de Quarkus para crear, gestionar y ejecutar proyectos Quarkus.
- **Angular_CLI**: Herramienta de línea de comandos oficial de Angular para crear, gestionar y compilar proyectos Angular.
- **JDK**: Java Development Kit, entorno de desarrollo para compilar y ejecutar aplicaciones Java.
- **Maven**: Herramienta de gestión de proyectos y dependencias para Java.
- **Dotfiles**: Archivos de configuración personal del desarrollador (`.bashrc`, `.gitconfig`, etc.).

---

## Requisitos de Infraestructura

### Infraestructura 1: Orquestación con Docker Compose

**User Story:** Como desarrollador, quiero que el DevContainer use Docker Compose para orquestar todos los servicios del proyecto, para que el entorno multi-contenedor (backend, frontend, base de datos) se levante de forma coordinada y consistente.

#### Acceptance Criteria

1. THE `devcontainer.json` SHALL referenciar un `docker-compose.yml` mediante la propiedad `dockerComposeFile` en lugar de una imagen única.
2. THE `devcontainer.json` SHALL especificar el servicio principal de desarrollo mediante la propiedad `service` apuntando al Backend_Service o a un contenedor de desarrollo dedicado.
3. THE Compose_File SHALL definir al menos tres servicios: Backend_Service (Quarkus/Camel), Frontend_Service (Angular) y Database_Service.
4. WHEN el desarrollador abre el proyecto en el IDE, THE IDE SHALL detectar la configuración en `.devcontainer/devcontainer.json` y levantar todos los servicios definidos en el Compose_File.
5. THE Compose_File SHALL definir una red interna compartida para que los servicios se comuniquen entre sí por nombre de servicio.
6. IF algún servicio del Compose_File falla al iniciarse, THEN THE Container_Runtime SHALL registrar el error con el nombre del servicio afectado y detener el proceso de arranque.

---

### Infraestructura 2: Herramientas del Backend Java/Quarkus/Camel

**User Story:** Como desarrollador backend, quiero que el contenedor del backend tenga preinstaladas todas las herramientas de Java, Quarkus y Apache Camel, para que pueda desarrollar, compilar y depurar la aplicación sin instalar nada manualmente.

#### Acceptance Criteria

1. THE Backend_Service SHALL incluir un JDK en la versión LTS especificada en la configuración del proyecto (mínimo JDK 17).
2. THE Backend_Service SHALL incluir Maven en la versión especificada en la configuración del proyecto para la gestión de dependencias y el ciclo de vida del build.
3. THE Backend_Service SHALL incluir la Quarkus_CLI en la versión compatible con el proyecto para crear extensiones, ejecutar en modo desarrollo y gestionar el proyecto.
4. WHEN el Backend_Service es reconstruido, THE Backend_Service SHALL reinstalar el JDK, Maven y la Quarkus_CLI a las versiones exactas especificadas en la configuración.
5. THE Backend_Service SHALL configurar las variables de entorno `JAVA_HOME` y `MAVEN_HOME` apuntando a las instalaciones correspondientes dentro del contenedor.
6. THE Backend_Service SHALL incluir las dependencias de Apache Camel requeridas por el proyecto declaradas en el `pom.xml`.
7. WHERE el proyecto requiera compilación nativa con GraalVM, THE Backend_Service SHALL incluir GraalVM y las herramientas `native-image` en la versión compatible con Quarkus.

---

### Infraestructura 3: Herramientas del Frontend Angular

**User Story:** Como desarrollador frontend, quiero que el contenedor del frontend tenga preinstaladas Node.js, npm y Angular CLI, para que pueda desarrollar y compilar la aplicación Angular sin instalar nada manualmente.

#### Acceptance Criteria

1. THE Frontend_Service SHALL incluir Node.js en la versión LTS especificada en el archivo `.nvmrc` o `package.json` del proyecto.
2. THE Frontend_Service SHALL incluir npm en la versión compatible con la versión de Node.js instalada.
3. THE Frontend_Service SHALL incluir la Angular_CLI en la versión especificada en el `package.json` del proyecto para crear componentes, servicios y compilar la aplicación.
4. WHEN el Frontend_Service es reconstruido, THE Frontend_Service SHALL reinstalar Node.js, npm y la Angular_CLI a las versiones exactas especificadas en la configuración.
5. THE Frontend_Service SHALL configurar la variable de entorno `NODE_ENV` con el valor `development` para el entorno de desarrollo.
6. THE Frontend_Service SHALL exponer el servidor de desarrollo de Angular en el puerto 4200 para que sea accesible desde el Host_Machine.

---

### Infraestructura 4: Extensiones del IDE para el stack completo

**User Story:** Como desarrollador, quiero que las extensiones del IDE relevantes para Java, Quarkus, Apache Camel y Angular se instalen automáticamente, para que el entorno de edición esté completamente configurado sin intervención manual.

#### Acceptance Criteria

1. THE `devcontainer.json` SHALL listar las extensiones del IDE requeridas para el proyecto en la sección `customizations.vscode.extensions`.
2. WHEN el DevContainer es iniciado, THE IDE SHALL instalar automáticamente todas las extensiones listadas en `devcontainer.json`.
3. THE `devcontainer.json` SHALL incluir extensiones para soporte de Java: `redhat.java` (Language Support for Java), `vscjava.vscode-java-debug` (Debugger for Java) y `vscjava.vscode-maven` (Maven for Java).
4. THE `devcontainer.json` SHALL incluir extensiones para soporte de Quarkus: `redhat.vscode-quarkus` (Quarkus Tools) para generación de código, autocompletado de propiedades y gestión del proyecto.
5. THE `devcontainer.json` SHALL incluir extensiones para soporte de Apache Camel: `redhat.vscode-apache-camel` (Language Support for Apache Camel) para autocompletado de rutas y validación de DSL.
6. THE `devcontainer.json` SHALL incluir extensiones para soporte de Angular: `Angular.ng-template` (Angular Language Service) para autocompletado en plantillas y `dbaeumer.vscode-eslint` para linting.
7. THE `devcontainer.json` SHALL incluir extensiones de productividad general: `ms-azuretools.vscode-docker` (Docker) para gestionar contenedores desde el IDE y `eamodio.gitlens` (GitLens) para navegación de historial Git.
8. IF una extensión listada no está disponible en el marketplace del IDE, THEN THE IDE SHALL registrar una advertencia sin bloquear el inicio del DevContainer.

---

### Infraestructura 5: Configuración de puertos por servicio

**User Story:** Como desarrollador, quiero que los puertos de cada servicio sean accesibles desde el Host_Machine con etiquetas descriptivas, para que pueda acceder al backend, frontend y base de datos desde mi navegador y herramientas locales.

#### Acceptance Criteria

1. THE `devcontainer.json` SHALL declarar todos los puertos de los servicios en la sección `forwardPorts` para que sean accesibles desde el Host_Machine.
2. THE `devcontainer.json` SHALL declarar el puerto 8080 para el servidor HTTP del Backend_Service (Quarkus).
3. THE `devcontainer.json` SHALL declarar el puerto 5005 para el agente de depuración remota del Backend_Service (Java Debug Wire Protocol).
4. THE `devcontainer.json` SHALL declarar el puerto 4200 para el servidor de desarrollo del Frontend_Service (Angular CLI).
5. THE `devcontainer.json` SHALL declarar el puerto 5432 para el Database_Service (PostgreSQL).
6. THE `devcontainer.json` SHALL asignar etiquetas descriptivas a cada puerto mediante `portsAttributes` indicando el servicio y protocolo (e.g., "Quarkus HTTP", "Angular Dev Server", "PostgreSQL").
7. WHEN el DevContainer es iniciado con puertos declarados, THE Container_Runtime SHALL reenviar automáticamente esos puertos al Host_Machine.

---

### Infraestructura 6: Scripts de inicialización por servicio

**User Story:** Como desarrollador, quiero que las dependencias de cada servicio se instalen automáticamente al crear el contenedor, para que el entorno esté listo para trabajar sin pasos manuales adicionales.

#### Acceptance Criteria

1. THE `devcontainer.json` SHALL definir un `postCreateCommand` que ejecute los scripts de inicialización de todos los servicios tras la creación del contenedor.
2. WHEN el `postCreateCommand` es ejecutado para el Backend_Service, THE Backend_Service SHALL ejecutar `mvn dependency:resolve` para descargar todas las dependencias Maven declaradas en el `pom.xml`.
3. WHEN el `postCreateCommand` es ejecutado para el Frontend_Service, THE Frontend_Service SHALL ejecutar `npm install` para instalar todas las dependencias declaradas en el `package.json`.
4. THE `devcontainer.json` SHALL definir un `postStartCommand` que inicie el servidor de desarrollo de Quarkus en modo `quarkus:dev` con recarga en caliente habilitada.
5. IF el `postCreateCommand` falla durante la instalación de dependencias de cualquier servicio, THEN THE DevContainer SHALL registrar el error completo en la salida del terminal indicando el servicio afectado para facilitar el diagnóstico.
6. THE Compose_File SHALL definir un `healthcheck` para el Database_Service de forma que el Backend_Service espere a que la base de datos esté disponible antes de iniciar.

---

### Infraestructura 7: Configuración de variables de entorno por servicio

**User Story:** Como desarrollador, quiero que las variables de entorno necesarias para cada servicio estén disponibles dentro de sus respectivos contenedores, para que la aplicación funcione correctamente sin configuración adicional.

#### Acceptance Criteria

1. THE Compose_File SHALL definir variables de entorno de desarrollo no sensibles para cada servicio en la sección `environment` de cada servicio.
2. THE Backend_Service SHALL recibir las variables de entorno de conexión a la base de datos: `QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME` y `QUARKUS_DATASOURCE_PASSWORD`.
3. THE Frontend_Service SHALL recibir la variable de entorno `API_URL` apuntando a la URL del Backend_Service dentro de la red Docker Compose.
4. WHERE el proyecto requiera variables de entorno sensibles (claves de API, contraseñas de producción), THE DevContainer SHALL cargarlas desde un archivo `.env` local excluido del control de versiones mediante la directiva `env_file` en el Compose_File.
5. THE Workspace SHALL incluir un archivo `.env.example` con todas las variables de entorno requeridas por todos los servicios y valores de ejemplo no sensibles.
6. IF una variable de entorno requerida por el Backend_Service no está definida al iniciar el contenedor, THEN THE Backend_Service SHALL registrar una advertencia indicando el nombre de la variable faltante antes de intentar conectarse a los servicios dependientes.

---

### Infraestructura 8: Persistencia de datos y caché de dependencias

**User Story:** Como desarrollador, quiero que los datos de la base de datos y las cachés de dependencias persistan entre reinicios del contenedor, para que no pierda datos ni tenga que reinstalar dependencias cada vez que reconstruyo el entorno.

#### Acceptance Criteria

1. THE DevContainer SHALL montar el directorio del proyecto del Host_Machine como el Workspace dentro del contenedor principal de desarrollo.
2. WHEN el contenedor es eliminado y recreado, THE Workspace SHALL conservar todos los archivos del proyecto sin pérdida de datos.
3. THE Compose_File SHALL definir un volumen nombrado para los datos del Database_Service para que persistan entre reinicios y reconstrucciones del contenedor.
4. THE Compose_File SHALL definir un volumen nombrado para la caché del repositorio Maven local (`.m2/repository`) del Backend_Service para reducir el tiempo de descarga de dependencias en reconstrucciones.
5. THE Compose_File SHALL definir un volumen nombrado para el directorio `node_modules` del Frontend_Service para reducir el tiempo de instalación de dependencias en reconstrucciones.
6. WHEN el DevContainer es reconstruido, THE Container_Runtime SHALL reutilizar los volúmenes de caché existentes para reducir el tiempo total de inicialización del entorno.

---

### Infraestructura 9: Documentación del entorno de desarrollo

**User Story:** Como desarrollador nuevo en el proyecto, quiero tener documentación clara sobre cómo usar el DevContainer con Docker Compose, para que pueda incorporarme al proyecto sin necesitar ayuda de otros desarrolladores.

#### Acceptance Criteria

1. THE Workspace SHALL incluir un archivo `README.md` con instrucciones para configurar y usar el DevContainer con Docker Compose.
2. THE `README.md` SHALL documentar los prerequisitos necesarios en el Host_Machine: Docker Desktop (o Docker Engine + Docker Compose plugin), IDE compatible con devcontainers y versión mínima requerida.
3. THE `README.md` SHALL incluir los pasos para abrir el proyecto en el DevContainer por primera vez, incluyendo cómo clonar el repositorio y abrir en el contenedor.
4. THE `README.md` SHALL documentar todos los puertos expuestos por servicio: puerto 8080 (Quarkus HTTP), puerto 5005 (Java Debug), puerto 4200 (Angular Dev Server) y puerto 5432 (PostgreSQL).
5. THE `README.md` SHALL documentar las variables de entorno requeridas por cada servicio y cómo configurarlas copiando el archivo `.env.example` a `.env`.
6. THE `README.md` SHALL documentar los comandos más frecuentes para el desarrollo diario: cómo ejecutar tests del backend (`mvn test`), cómo ejecutar tests del frontend (`ng test`), y cómo acceder a los logs de cada servicio (`docker compose logs -f <service>`).

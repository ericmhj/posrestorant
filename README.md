# POS Restaurant System

Sistema de Punto de Venta para restaurante construido con **Java 17 + Quarkus + Apache Camel**, **Angular 17** y **PostgreSQL 15**, orquestado con Docker Compose y listo para desarrollar dentro de un **Dev Container**.

---

## Prerequisitos

Instala las siguientes herramientas en tu máquina antes de comenzar:

| Herramienta | Versión mínima | Enlace |
|---|---|---|
| Docker Desktop (Windows/macOS) o Docker Engine + Compose plugin (Linux) | Docker 24+ | https://docs.docker.com/get-docker/ |
| Visual Studio Code | 1.85+ | https://code.visualstudio.com/ |
| Extensión Dev Containers (VS Code) | 0.341+ | https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers |

> **Nota:** No necesitas instalar Java, Maven, Node.js ni Angular CLI en tu máquina. El Dev Container los provee automáticamente.

---

## Abrir el proyecto en Dev Container (primera vez)

1. Clona el repositorio:
   ```bash
   git clone <url-del-repositorio>
   cd pos
   ```

2. Copia el archivo de variables de entorno:
   ```bash
   cp .devcontainer/.env.example .devcontainer/.env
   # Edita .devcontainer/.env con tus valores si es necesario
   ```

3. Abre la carpeta en VS Code:
   ```bash
   code .
   ```

4. VS Code detectará la configuración en `.devcontainer/devcontainer.json` y mostrará una notificación en la esquina inferior derecha. Haz clic en **"Reopen in Container"**.

   Alternativamente, abre la paleta de comandos (`Ctrl+Shift+P` / `Cmd+Shift+P`) y ejecuta:
   ```
   Dev Containers: Reopen in Container
   ```

5. Docker descargará las imágenes y construirá los contenedores (solo la primera vez, puede tardar varios minutos). Una vez listo, el terminal del Dev Container estará disponible y Quarkus arrancará automáticamente en modo desarrollo.

---

## Puertos expuestos

| Puerto | Servicio | Descripción |
|--------|----------|-------------|
| **8080** | Quarkus HTTP | API REST (`/api/v1`) y Swagger UI (`/q/swagger-ui`) |
| **5005** | Java Debug (JDWP) | Depuración remota desde VS Code o IntelliJ |
| **4200** | Angular Dev Server | Interfaz de usuario del POS |
| **5432** | PostgreSQL | Base de datos (accesible con cualquier cliente SQL) |

Todos los puertos se reenvían automáticamente al host cuando el Dev Container está activo.

---

## Configuración de variables de entorno

Las variables de entorno se cargan desde `.devcontainer/.env` (excluido del control de versiones).

1. Copia el archivo de ejemplo:
   ```bash
   cp .devcontainer/.env.example .devcontainer/.env
   ```

2. Edita `.devcontainer/.env` con los valores apropiados:

   ```dotenv
   # Base de datos
   POSTGRES_DB=pos_db
   POSTGRES_USER=pos_user
   POSTGRES_PASSWORD=pos_password

   # Backend
   QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://db:5432/pos_db
   QUARKUS_DATASOURCE_USERNAME=pos_user
   QUARKUS_DATASOURCE_PASSWORD=pos_password
   QUARKUS_LOG_LEVEL=DEBUG

   # Frontend
   NODE_ENV=development
   API_URL=http://backend:8080
   WS_URL=ws://backend:8080/ws

   # JWT
   JWT_SECRET=change-me-in-production
   JWT_EXPIRATION_HOURS=8
   ```

3. Si cambias variables de entorno, reconstruye el Dev Container:
   ```
   Dev Containers: Rebuild Container
   ```

---

## Comandos de desarrollo diario

### Backend (Quarkus)

```bash
# Quarkus ya arranca automáticamente en modo dev al abrir el Dev Container.
# Para reiniciarlo manualmente:
cd /workspace/pos-backend
mvn quarkus:dev -Ddebug=5005

# Ejecutar tests del backend
mvn test

# Ejecutar tests de integración
mvn verify

# Compilar sin ejecutar
mvn compile

# Ver logs del contenedor backend
docker compose -f .devcontainer/docker-compose.yml logs -f backend
```

### Frontend (Angular)

```bash
# Iniciar servidor de desarrollo (ya arranca con el Dev Container)
cd /workspace/pos-frontend
ng serve --host 0.0.0.0 --port 4200 --proxy-config proxy.conf.json

# Ejecutar tests del frontend (modo CI, sin watch)
ng test --watch=false --browsers=ChromeHeadless

# Ejecutar tests en modo watch
ng test

# Compilar para producción
ng build --configuration production

# Ver logs del contenedor frontend
docker compose -f .devcontainer/docker-compose.yml logs -f frontend
```

### Base de datos

```bash
# Ver logs de PostgreSQL
docker compose -f .devcontainer/docker-compose.yml logs -f db

# Conectarse a la base de datos con psql (desde dentro del Dev Container)
psql -h db -U pos_user -d pos_db

# Ver todos los logs de todos los servicios
docker compose -f .devcontainer/docker-compose.yml logs -f
```

### Reconstruir el entorno

```bash
# Reconstruir todos los contenedores (desde la paleta de comandos de VS Code)
# Dev Containers: Rebuild Container

# O desde la terminal del host:
docker compose -f .devcontainer/docker-compose.yml down
docker compose -f .devcontainer/docker-compose.yml up --build
```

---

## Estructura del proyecto

```
pos/
├── .devcontainer/
│   ├── devcontainer.json     # Configuración del Dev Container
│   ├── docker-compose.yml    # Orquestación de servicios
│   └── .env.example          # Variables de entorno de ejemplo
├── pos-backend/              # Aplicación Java/Quarkus/Camel
│   ├── src/main/java/com/restaurant/pos/
│   │   ├── auth/             # Autenticación y RBAC
│   │   ├── mesa/             # Gestión de mesas
│   │   ├── cuenta/           # Cuentas y cobro
│   │   ├── pedido/           # Pedidos e ítems
│   │   ├── kds/              # Kitchen Display System
│   │   ├── bds/              # Bar Display System
│   │   ├── producto/         # Productos y categorías
│   │   ├── inventario/       # Control de inventario
│   │   ├── usuario/          # Gestión de usuarios
│   │   ├── reporte/          # Reportes operativos
│   │   ├── camel/            # Rutas Apache Camel
│   │   ├── websocket/        # Endpoint WebSocket
│   │   └── common/           # Filtros, mappers, DTOs comunes
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-dev.properties
│   │   └── db/migration/     # Migraciones Flyway
│   ├── Dockerfile.dev
│   └── pom.xml
├── pos-frontend/             # Aplicación Angular
│   ├── src/app/
│   │   ├── core/             # Auth, WebSocket, HTTP, Error handling
│   │   ├── shared/           # Componentes y pipes reutilizables
│   │   └── features/         # Login, POS, KDS, BDS, Cobro, Admin
│   ├── src/environments/
│   ├── proxy.conf.json       # Proxy para desarrollo
│   ├── Dockerfile.dev
│   └── package.json
├── .gitignore
└── README.md
```

---

## Acceso a herramientas de desarrollo

Una vez que el Dev Container está activo:

- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Health check**: http://localhost:8080/q/health
- **Métricas Prometheus**: http://localhost:8080/q/metrics
- **Aplicación Angular**: http://localhost:4200

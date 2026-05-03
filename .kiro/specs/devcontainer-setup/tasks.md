# Implementation Tasks: POS Restaurant System

> **Feature:** devcontainer-setup
> **Stack:** Java 17 + Quarkus + Apache Camel | Angular | PostgreSQL | Docker Compose
> **Spec:** `.kiro/specs/devcontainer-setup/`

---

## Phase 1: Infrastructure & DevContainer

- [x] 1. Create project directory structure
  - [x] 1.1 Create root workspace with `pos-backend/` and `pos-frontend/` directories
  - [x] 1.2 Create `.devcontainer/` directory at workspace root
  - [x] 1.3 Create `README.md` placeholder at workspace root

- [x] 2. Configure Docker Compose
  - [x] 2.1 Create `.devcontainer/docker-compose.yml` with three services: `db` (PostgreSQL 15), `backend` (Quarkus), `frontend` (Angular)
  - [x] 2.2 Define `pos-network` bridge network shared by all services
  - [x] 2.3 Configure `db` service with `postgres:15-alpine` image, environment variables (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`), port `5432:5432`, named volume `postgres-data`, and healthcheck (`pg_isready` every 10s, timeout 3s, 5 retries)
  - [x] 2.4 Configure `backend` service with `Dockerfile.dev`, environment variables (`QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME`, `QUARKUS_DATASOURCE_PASSWORD`, `QUARKUS_LOG_LEVEL`, `JAVA_TOOL_OPTIONS` for JDWP debug on port 5005), ports `8080:8080` and `5005:5005`, named volume `maven-cache` mounted at `/root/.m2/repository`, `depends_on: db: condition: service_healthy`, and healthcheck (`curl /q/health/live` every 30s)
  - [x] 2.5 Configure `frontend` service with `Dockerfile.dev`, environment variables (`NODE_ENV=development`, `API_URL`), port `4200:4200`, named volume `node-modules`, and `depends_on: backend`
  - [x] 2.6 Declare named volumes: `postgres-data`, `maven-cache`, `node-modules`

- [x] 3. Configure devcontainer.json
  - [x] 3.1 Create `.devcontainer/devcontainer.json` referencing `docker-compose.yml` via `dockerComposeFile` property
  - [x] 3.2 Set `service: backend` and `workspaceFolder: /workspace`
  - [x] 3.3 Configure `forwardPorts: [8080, 5005, 4200, 5432]` with `portsAttributes` labels: "Quarkus HTTP", "Java Debug (JDWP)", "Angular Dev Server", "PostgreSQL"
  - [x] 3.4 Set `postCreateCommand` to run `mvn dependency:resolve -q` in `pos-backend` and `npm install --silent` in `pos-frontend`
  - [x] 3.5 Set `postStartCommand` to start Quarkus in dev mode: `mvn quarkus:dev -Ddebug=5005`
  - [x] 3.6 Configure `customizations.vscode.extensions` with: `redhat.java`, `vscjava.vscode-java-debug`, `vscjava.vscode-maven`, `redhat.vscode-quarkus`, `redhat.vscode-apache-camel`, `Angular.ng-template`, `dbaeumer.vscode-eslint`, `ms-azuretools.vscode-docker`, `eamodio.gitlens`, `humao.rest-client`
  - [x] 3.7 Configure `features` for Java 17 + Maven 3.9 (`ghcr.io/devcontainers/features/java:1`) and Node.js 20 (`ghcr.io/devcontainers/features/node:1`)
  - [x] 3.8 Add `editor.formatOnSave: true` and Java runtime path to `customizations.vscode.settings`

- [x] 4. Create environment configuration files
  - [x] 4.1 Create `.devcontainer/.env.example` with all required variables: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME`, `QUARKUS_DATASOURCE_PASSWORD`, `QUARKUS_LOG_LEVEL`, `NODE_ENV`, `API_URL`, `WS_URL`, `JWT_SECRET`, `JWT_EXPIRATION_HOURS`
  - [x] 4.2 Add `.env` to `.gitignore` to prevent committing secrets
  - [x] 4.3 Create `pos-backend/Dockerfile.dev` based on `eclipse-temurin:17-jdk` with Maven installed and workspace mounted
  - [x] 4.4 Create `pos-frontend/Dockerfile.dev` based on `node:20-alpine` with Angular CLI installed globally

- [x] 5. Initialize Quarkus backend project
  - [x] 5.1 Scaffold `pos-backend/` using Quarkus CLI or Maven archetype with extensions: `resteasy-reactive-jackson`, `hibernate-orm-panache`, `jdbc-postgresql`, `flyway`, `smallrye-openapi`, `smallrye-health`, `micrometer-registry-prometheus`, `websockets`, `camel-quarkus-core`, `camel-quarkus-direct`, `security`
  - [x] 5.2 Create `pos-backend/src/main/resources/application.properties` with datasource config (reading from env vars), Flyway config (`migrate-at-start=true`), HTTP port 8080, CORS enabled, OpenAPI path `/q/openapi`, Swagger UI always included, JSON logging enabled, Prometheus metrics at `/q/metrics`, GZIP compression enabled
  - [x] 5.3 Create `pos-backend/src/main/resources/application-dev.properties` with `QUARKUS_LOG_LEVEL=DEBUG` and `quarkus.swagger-ui.always-include=true`
  - [x] 5.4 Create package structure: `com.restaurant.pos.{auth,mesa,cuenta,pedido,kds,bds,producto,inventario,usuario,reporte,camel,websocket,common}`

- [x] 6. Initialize Angular frontend project
  - [x] 6.1 Scaffold `pos-frontend/` using Angular CLI: `ng new pos-frontend --routing --style=scss --standalone=false`
  - [x] 6.2 Create module/folder structure: `core/{auth,websocket,http,error}`, `shared/{components,pipes}`, `features/{login,pos,kds,bds,cobro,admin}`
  - [x] 6.3 Create `pos-frontend/src/environments/environment.ts` with `apiUrl` and `wsUrl` pointing to `http://localhost:8080` and `ws://localhost:8080/ws`
  - [x] 6.4 Configure Angular proxy (`proxy.conf.json`) to forward `/api` requests to `http://backend:8080` during development
  - [x] 6.5 Install Angular dependencies: `@angular/material`, `rxjs`, `zone.js`

- [x] 7. Write README.md
  - [x] 7.1 Document prerequisites: Docker Desktop (or Docker Engine + Compose plugin), VS Code with Dev Containers extension, minimum versions required
  - [x] 7.2 Document steps to open project in DevContainer for the first time (clone → open in VS Code → "Reopen in Container")
  - [x] 7.3 Document all exposed ports: 8080 (Quarkus HTTP), 5005 (Java Debug), 4200 (Angular Dev Server), 5432 (PostgreSQL)
  - [x] 7.4 Document environment variables setup: copy `.env.example` to `.env` and fill in values
  - [x] 7.5 Document daily development commands: `mvn test`, `ng test --watch=false`, `docker compose logs -f <service>`, `mvn quarkus:dev`


---

## Phase 2: Database Schema & JPA Entities

- [x] 8. Create Flyway migrations
  - [x] 8.1 Create `V1__create_schema.sql` with tables: `usuario`, `auditoria_acceso`, `mesa`, `cuenta`, `categoria`, `producto`, `pedido`, `item_pedido`, `item_inventario`, `movimiento_inventario`, `reserva_inventario`
  - [x] 8.2 Define `usuario` table: `id UUID PK`, `nombre`, `apellido`, `username UNIQUE NOT NULL`, `password_hash NOT NULL`, `rol VARCHAR(20) NOT NULL`, `activo BOOLEAN DEFAULT true`, `intentos_fallidos INT DEFAULT 0`, `bloqueado_hasta TIMESTAMP`, `created_at TIMESTAMP DEFAULT NOW()`
  - [x] 8.3 Define `auditoria_acceso` table: `id UUID PK`, `usuario_id UUID FK → usuario`, `accion VARCHAR(50)`, `resultado VARCHAR(20)`, `fecha_hora TIMESTAMP`, `ip_address VARCHAR(45)`
  - [x] 8.4 Define `mesa` table: `id UUID PK`, `nombre VARCHAR(50) NOT NULL`, `estado VARCHAR(20) NOT NULL DEFAULT 'LIBRE'`, `created_at TIMESTAMP`, `updated_at TIMESTAMP`
  - [x] 8.5 Define `cuenta` table: `id UUID PK`, `mesa_id UUID FK → mesa`, `mesero_id UUID FK → usuario`, `estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA'`, `abierta_en TIMESTAMP`, `cerrada_en TIMESTAMP`, `metodo_pago VARCHAR(30)`, `total DECIMAL(10,2)`
  - [x] 8.6 Define `categoria` table: `id UUID PK`, `nombre VARCHAR(100) NOT NULL`, `descripcion TEXT`
  - [x] 8.7 Define `producto` table: `id UUID PK`, `categoria_id UUID FK → categoria`, `nombre VARCHAR(150) NOT NULL`, `descripcion TEXT`, `precio DECIMAL(10,2) NOT NULL`, `estacion VARCHAR(20) NOT NULL`, `activo BOOLEAN DEFAULT true`, `imagen_url VARCHAR(500)`, `created_at TIMESTAMP`, `updated_at TIMESTAMP`
  - [x] 8.8 Define `pedido` table: `id UUID PK`, `cuenta_id UUID FK → cuenta`, `mesero_id UUID FK → usuario`, `numero_ronda INT NOT NULL`, `created_at TIMESTAMP`
  - [x] 8.9 Define `item_pedido` table: `id UUID PK`, `pedido_id UUID FK → pedido`, `producto_id UUID FK → producto`, `cantidad INT NOT NULL`, `precio_unitario DECIMAL(10,2) NOT NULL`, `modificadores TEXT`, `estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'`, `created_at TIMESTAMP`, `preparando_en TIMESTAMP`, `listo_en TIMESTAMP`
  - [x] 8.10 Define `item_inventario` table: `id UUID PK`, `nombre VARCHAR(150) NOT NULL`, `unidad_medida VARCHAR(30)`, `stock_actual DECIMAL(10,3) NOT NULL DEFAULT 0`, `stock_minimo DECIMAL(10,3) NOT NULL DEFAULT 0`, `stock_maximo DECIMAL(10,3)`, `updated_at TIMESTAMP`
  - [x] 8.11 Define `movimiento_inventario` table: `id UUID PK`, `item_inventario_id UUID FK → item_inventario`, `usuario_id UUID FK → usuario`, `tipo VARCHAR(30) NOT NULL`, `cantidad DECIMAL(10,3) NOT NULL`, `motivo TEXT`, `proveedor VARCHAR(150)`, `fecha_hora TIMESTAMP DEFAULT NOW()`
  - [x] 8.12 Define `reserva_inventario` table: `id UUID PK`, `item_inventario_id UUID FK → item_inventario`, `item_pedido_id UUID FK → item_pedido`, `cantidad DECIMAL(10,3) NOT NULL`, `created_at TIMESTAMP DEFAULT NOW()`
  - [x] 8.13 Add indexes: `usuario(username)`, `mesa(estado)`, `cuenta(mesa_id, estado)`, `item_pedido(pedido_id, estado)`, `item_pedido(estado, created_at)` for KDS/BDS queries
  - [x] 8.14 Create `V2__seed_data.sql` with initial admin user (username: `admin`, password hash for `admin123`), sample categories (Entradas, Platos Fuertes, Postres, Bebidas Alcohólicas, Bebidas Sin Alcohol), and 3 sample tables

- [x] 9. Create JPA entities (Quarkus Panache)
  - [x] 9.1 Create `Usuario.java` entity with all fields, `@Entity`, `@Table(name="usuario")`, enums `Rol {ADMIN, MESERO, COCINA, BARRA}`, and Panache static finders: `findByUsername(String)`, `findActivos()`
  - [x] 9.2 Create `AuditoriaAcceso.java` entity with `@ManyToOne Usuario` and Panache finder: `findByUsuario(UUID usuarioId, int limit)`
  - [x] 9.3 Create `Mesa.java` entity with enum `MesaEstado {LIBRE, OCUPADA, RESERVADA}` and Panache finders: `findByEstado(MesaEstado)`, `findAllOrdered()`
  - [x] 9.4 Create `Cuenta.java` entity with `@ManyToOne Mesa`, `@ManyToOne Usuario mesero`, enum `CuentaEstado {ABIERTA, CERRADA}`, and Panache finders: `findAbiertaByMesa(UUID mesaId)`, `findByMesa(UUID mesaId)`
  - [x] 9.5 Create `Categoria.java` entity with Panache finders: `findAllOrdered()`, `countProductos(UUID categoriaId)`
  - [x] 9.6 Create `Producto.java` entity with `@ManyToOne Categoria`, enum `Estacion {COCINA, BARRA}`, and Panache finders: `findActivos()`, `findByCategoria(UUID categoriaId)`, `findActivoById(UUID id)`
  - [x] 9.7 Create `Pedido.java` entity with `@ManyToOne Cuenta`, `@ManyToOne Usuario mesero`, `@OneToMany List<ItemPedido>`, and Panache finder: `findByCuenta(UUID cuentaId)`
  - [x] 9.8 Create `ItemPedido.java` entity with `@ManyToOne Pedido`, `@ManyToOne Producto`, enum `ItemPedidoEstado {PENDIENTE, PREPARANDO, LISTO}`, and Panache finders: `findByEstacionAndEstado(Estacion, ItemPedidoEstado)`, `findByCuenta(UUID cuentaId)`, `findPendientesByEstacion(Estacion)`
  - [x] 9.9 Create `ItemInventario.java` entity with Panache finders: `findBelowMinStock()`, `findByNombre(String)`
  - [x] 9.10 Create `MovimientoInventario.java` entity with `@ManyToOne ItemInventario`, `@ManyToOne Usuario`, enum `TipoMovimiento {ENTRADA, SALIDA_VENTA, AJUSTE, MERMA}`, and Panache finder: `findByItemAndPeriod(UUID itemId, LocalDateTime desde, LocalDateTime hasta)`
  - [x] 9.11 Create `ReservaInventario.java` entity with `@ManyToOne ItemInventario`, `@ManyToOne ItemPedido`, and Panache finders: `findByItemPedido(UUID itemPedidoId)`, `findByCuenta(UUID cuentaId)`

- [x] 10. Create common infrastructure classes
  - [x] 10.1 Create `TraceIdFilter.java` implementing `ContainerRequestFilter` that generates a UUID `traceId` per request, stores it in `MDC` and request context, and adds it to response headers
  - [x] 10.2 Create `GlobalExceptionMapper.java` implementing `ExceptionMapper<Exception>` that maps `ValidationException` → 400, `BusinessException` → configurable status, unhandled exceptions → 500, all with `traceId` in response body
  - [x] 10.3 Create `ErrorResponse.java` DTO: `{ field, message, traceId, timestamp, status }`
  - [x] 10.4 Create `ApiResponse.java` generic wrapper: `{ data, message, traceId }`
  - [x] 10.5 Create `PaginationParams.java` `@BeanParam` with `page` (default 0) and `size` (default 20, max 100) validated with `@Min`/`@Max`
  - [x] 10.6 Create `BusinessException.java` with `httpStatus` field and subclasses: `MesaOcupadaException`, `StockInsuficienteException`, `RecursoNoEncontradoException`, `ConflictoEstadoException`
  - [x] 10.7 Create `ValidationException.java` with `field` and `message` fields


---

## Phase 3: Authentication, RBAC & User Management

- [ ] 11. Implement password hashing and JWT utilities
  - [ ] 11.1 Add `bcrypt` dependency to `pom.xml` (e.g. `org.mindrot:jbcrypt:0.4`)
  - [ ] 11.2 Create `PasswordHasher.java` with methods `hash(String plaintext): String` and `verify(String plaintext, String hash): boolean` using BCrypt with cost factor 12
  - [ ] 11.3 Create `JwtUtil.java` (or Basic Auth token builder) with methods `generateToken(Usuario): String` and `validateToken(String): Optional<UUID>` — token encodes `userId` and `rol`, expires after `JWT_EXPIRATION_HOURS` hours
  - [ ] 11.4 Create `SessionStore.java` `@ApplicationScoped` in-memory store (ConcurrentHashMap) mapping `token → userId` for session invalidation on logout; include `register(token, userId)`, `invalidate(token)`, `isValid(token): boolean`

- [ ] 12. Implement AuthService and AuthResource
  - [ ] 12.1 Create `AuthService.java` with method `login(String username, String password): AuthLoginResponse` that: finds user by username, verifies password hash, checks `activo=true`, resets `intentos_fallidos` on success, increments on failure, locks account for 15 min after 5 consecutive failures, registers `AuditoriaAcceso` entry, returns token
  - [ ] 12.2 Create `AuthService.logout(String token)` that invalidates token in `SessionStore` and registers `AuditoriaAcceso` entry
  - [ ] 12.3 Create `AuthService.changePassword(UUID userId, String currentPassword, String newPassword)` that verifies current password before updating hash
  - [ ] 12.4 Create `AuthLoginRequest.java` DTO: `{ username, password }` with `@NotBlank` validations
  - [ ] 12.5 Create `AuthLoginResponse.java` DTO: `{ token, usuario: { id, nombre, rol }, expiresAt }`
  - [ ] 12.6 Create `AuthResource.java` JAX-RS resource with:
    - `POST /api/v1/auth/login` (public) → calls `AuthService.login`, returns 200 + `AuthLoginResponse`
    - `POST /api/v1/auth/logout` (authenticated) → calls `AuthService.logout`, returns 204
    - `PUT /api/v1/auth/password` (authenticated) → calls `AuthService.changePassword`, returns 204

- [ ] 13. Implement RBAC security filter
  - [ ] 13.1 Create `AuthenticationFilter.java` implementing `ContainerRequestFilter` with `@Provider` that: extracts token from `Authorization` header, validates via `SessionStore`, loads `Usuario` from DB, aborts with 401 if invalid, stores `Usuario` in request context
  - [ ] 13.2 Create `RoleGuard.java` `@InterceptorBinding` annotation `@RequiresRole(Rol... roles)` and its interceptor that reads `Usuario` from request context and aborts with 403 if role not in allowed list
  - [ ] 13.3 Annotate all resource methods with `@RequiresRole` according to the API design table (Req 10.4–10.7)
  - [ ] 13.4 Create `CurrentUser.java` `@RequestScoped` CDI bean that holds the authenticated `Usuario` for injection into services

- [ ] 14. Implement UsuarioService and UsuarioResource
  - [ ] 14.1 Create `UsuarioService.java` with methods:
    - `findAll(PaginationParams): Page<UsuarioDTO>` — list all users (Admin only)
    - `create(CreateUsuarioRequest): UsuarioDTO` — validate unique username, hash password, persist
    - `update(UUID id, UpdateUsuarioRequest): UsuarioDTO` — update nombre, apellido, rol
    - `deactivate(UUID id)` — set `activo=false`, invalidate active sessions via `SessionStore`
    - `resetPassword(UUID id, String newPassword)` — hash and update password (Admin only)
  - [ ] 14.2 Create `CreateUsuarioRequest.java` DTO: `{ nombre, apellido, username, password, rol }` with `@NotBlank`, `@NotNull` validations
  - [ ] 14.3 Create `UpdateUsuarioRequest.java` DTO: `{ nombre, apellido, rol }`
  - [ ] 14.4 Create `UsuarioDTO.java`: `{ id, nombre, apellido, username, rol, activo, createdAt }`
  - [ ] 14.5 Create `UsuarioResource.java` JAX-RS resource with:
    - `GET /api/v1/usuarios` `@RequiresRole(ADMIN)` → paginated list
    - `POST /api/v1/usuarios` `@RequiresRole(ADMIN)` → create, returns 201
    - `PUT /api/v1/usuarios/{id}` `@RequiresRole(ADMIN)` → update, returns 200
    - `PUT /api/v1/usuarios/{id}/desactivar` `@RequiresRole(ADMIN)` → deactivate, returns 204
    - `PUT /api/v1/usuarios/{id}/reset-password` `@RequiresRole(ADMIN)` → reset password, returns 204

- [ ] 15. Write Auth & User tests
  - [ ] 15.1 Create `AuthServiceTest.java` with unit tests: login success, login wrong password increments counter, login locks after 5 failures, logout invalidates token, change password verifies current password
  - [ ] 15.2 Create `PasswordHasherPropertyTest.java` (PBT — Property 2): for any plaintext password, `hash(p) != p` AND `verify(p, hash(p)) == true`, min 100 iterations with QuickTheories
  - [ ] 15.3 Create `RbacPropertyTest.java` (PBT — Property 3): for any endpoint requiring role R and any user with role ≠ R, the request returns HTTP 403 and the operation is not executed
  - [ ] 15.4 Create `SessionPropertyTest.java` (PBT — Property 4): for any valid token T, after logout with T, any request using T returns 401
  - [ ] 15.5 Create `LockoutPropertyTest.java` (PBT — Property 22): for any user, after exactly 5 consecutive failed login attempts, the account is locked and subsequent attempts return 401 until lockout expires


---

## Phase 4: Mesas, Cuentas, Pedidos & Inventario

- [ ] 16. Implement MesaService and MesaResource
  - [ ] 16.1 Create `MesaService.java` with methods:
    - `findAll(): List<MesaDTO>` — all tables with current state, open cuenta summary (total, elapsed time)
    - `create(CreateMesaRequest): MesaDTO` — Admin only, persist with estado=LIBRE
    - `update(UUID id, UpdateMesaRequest): MesaDTO` — Admin only, update nombre
    - `delete(UUID id)` — Admin only, reject with 409 if mesa has open Cuenta
    - `abrir(UUID id, UUID meseroId): CuentaDTO` — verify estado=LIBRE, create Cuenta, set estado=OCUPADA, broadcast `MESA_ESTADO_CAMBIADO` via WebSocket
  - [ ] 16.2 Create `MesaDTO.java`: `{ id, nombre, estado, cuentaId, totalAcumulado, tiempoAbierta (seconds) }`
  - [ ] 16.3 Create `CreateMesaRequest.java` / `UpdateMesaRequest.java` DTOs with `@NotBlank nombre`
  - [ ] 16.4 Create `MesaResource.java` JAX-RS resource:
    - `GET /api/v1/mesas` `@RequiresRole(MESERO, ADMIN)` → list all
    - `POST /api/v1/mesas` `@RequiresRole(ADMIN)` → create, returns 201
    - `PUT /api/v1/mesas/{id}` `@RequiresRole(ADMIN)` → update, returns 200
    - `DELETE /api/v1/mesas/{id}` `@RequiresRole(ADMIN)` → delete, returns 204
    - `POST /api/v1/mesas/{id}/abrir` `@RequiresRole(MESERO, ADMIN)` → open cuenta, returns 201

- [ ] 17. Implement CuentaService and CuentaResource
  - [ ] 17.1 Create `CuentaService.java` with methods:
    - `findById(UUID id): CuentaDetalleDTO` — cuenta with all pedidos and items, subtotals
    - `addPedido(UUID cuentaId, CreatePedidoRequest, UUID meseroId): PedidoDTO` — verify cuenta is ABIERTA, create Pedido with next `numero_ronda`, process each item via `InventarioService.reservar`, route via Apache Camel, broadcast `PEDIDO_CREADO`
    - `removeItem(UUID cuentaId, UUID pedidoId, UUID itemId, UUID requesterId)` — if item estado=PENDIENTE and not sent, release `ReservaInventario`, delete item; if already sent require Admin role
    - `cobrar(UUID cuentaId, CobroRequest, UUID meseroId): ComprobanteDTO` — warn if items not LISTO, set Cuenta=CERRADA, Mesa=LIBRE, convert reservas to movimientos, broadcast `MESA_ESTADO_CAMBIADO`
  - [ ] 17.2 Create `CuentaDetalleDTO.java`: `{ id, mesa, mesero, estado, abiertaEn, pedidos: [PedidoDTO], total, tiempoAbierta }`
  - [ ] 17.3 Create `PedidoDTO.java`: `{ id, numeroRonda, items: [ItemPedidoDTO], createdAt }`
  - [ ] 17.4 Create `ItemPedidoDTO.java`: `{ id, productoId, productoNombre, cantidad, precioUnitario, modificadores, estado, estacion, mesaNombre, tiempoEspera }`
  - [ ] 17.5 Create `CreatePedidoRequest.java`: `{ items: [{ productoId, cantidad, modificadores }] }`
  - [ ] 17.6 Create `CobroRequest.java`: `{ metodoPago (EFECTIVO|TARJETA_CREDITO|TARJETA_DEBITO), montoRecibido (required if EFECTIVO) }`
  - [ ] 17.7 Create `ComprobanteDTO.java`: `{ numeroCuenta, mesa, items, subtotal, impuestos, total, metodoPago, cambio, cerradaEn }`
  - [ ] 17.8 Create `CuentaResource.java` JAX-RS resource:
    - `GET /api/v1/cuentas/{id}` `@RequiresRole(MESERO, ADMIN)` → cuenta detail
    - `POST /api/v1/cuentas/{id}/pedidos` `@RequiresRole(MESERO, ADMIN)` → add pedido round, returns 201
    - `DELETE /api/v1/cuentas/{cuentaId}/pedidos/{pedidoId}/items/{itemId}` `@RequiresRole(MESERO, ADMIN)` → remove item
    - `POST /api/v1/cuentas/{id}/cobro` `@RequiresRole(MESERO, ADMIN)` → cobrar, returns 200 + comprobante

- [ ] 18. Implement InventarioService and InventarioResource
  - [ ] 18.1 Create `InventarioService.java` with methods:
    - `findAll(PaginationParams): Page<ItemInventarioDTO>` — list with current stock and alert flag
    - `create(CreateItemInventarioRequest): ItemInventarioDTO`
    - `update(UUID id, UpdateItemInventarioRequest): ItemInventarioDTO` — update stock min/max
    - `registrarMovimiento(UUID id, RegistrarMovimientoRequest, UUID userId): MovimientoInventarioDTO` — ENTRADA adds stock, AJUSTE/MERMA requires motivo, updates `stock_actual`, persists `MovimientoInventario`, broadcasts `STOCK_ALERTA` if below minimum
    - `reservar(UUID itemInventarioId, BigDecimal cantidad, UUID itemPedidoId)` — verify `stock_actual - reservas_activas >= cantidad`, create `ReservaInventario`, reduce available stock; throw `StockInsuficienteException` if insufficient
    - `liberarReserva(UUID itemPedidoId)` — delete `ReservaInventario` for item, restore available stock
    - `confirmarReserva(UUID itemPedidoId, UUID userId)` — convert `ReservaInventario` to `MovimientoInventario` tipo=SALIDA_VENTA, delete reserva
    - `getMovimientos(UUID id, LocalDateTime desde, LocalDateTime hasta, PaginationParams): Page<MovimientoInventarioDTO>`
  - [ ] 18.2 Create `ItemInventarioDTO.java`: `{ id, nombre, unidadMedida, stockActual, stockMinimo, stockMaximo, stockDisponible, alertaMinimo, updatedAt }`
  - [ ] 18.3 Create `MovimientoInventarioDTO.java`: `{ id, tipo, cantidad, motivo, proveedor, usuario, fechaHora }`
  - [ ] 18.4 Create `CreateItemInventarioRequest.java` / `UpdateItemInventarioRequest.java` / `RegistrarMovimientoRequest.java` DTOs with validations
  - [ ] 18.5 Create `InventarioResource.java` JAX-RS resource:
    - `GET /api/v1/inventario` `@RequiresRole(ADMIN)` → paginated list
    - `POST /api/v1/inventario` `@RequiresRole(ADMIN)` → create, returns 201
    - `PUT /api/v1/inventario/{id}` `@RequiresRole(ADMIN)` → update
    - `POST /api/v1/inventario/{id}/movimientos` `@RequiresRole(ADMIN)` → register movement
    - `GET /api/v1/inventario/{id}/movimientos` `@RequiresRole(ADMIN)` → movement history (paginated, filterable by date range)

- [ ] 19. Write Mesa, Cuenta & Inventario tests
  - [ ] 19.1 Create `MesaServiceTest.java`: open free table creates cuenta + changes state, open occupied table throws 409, delete table with open cuenta throws 409, close cuenta sets mesa to LIBRE
  - [ ] 19.2 Create `MesaStatePropertyTest.java` (PBT — Properties 5, 6, 7, 8): mesa state is always one of {LIBRE, OCUPADA, RESERVADA}; opening LIBRE mesa → OCUPADA + one open Cuenta; closing Cuenta → mesa LIBRE; opening OCUPADA mesa → rejected
  - [ ] 19.3 Create `InventarioServiceTest.java`: reservar reduces available stock, reservar with insufficient stock throws exception, liberarReserva restores stock, confirmarReserva creates movimiento
  - [ ] 19.4 Create `InventarioPropertyTest.java` (PBT — Properties 9, 10, 12, 20, 21): stock reduces by exact quantity on confirm; item rejected when stock < quantity; delete pre-send item restores stock; stock never goes negative; every stock change creates exactly one MovimientoInventario
  - [ ] 19.5 Create `CobroServiceTest.java`: cobro closes cuenta, sets mesa LIBRE, converts reservas to movimientos, calculates correct change for cash payments
  - [ ] 19.6 Create `CobroPropertyTest.java` (PBT — Properties 14, 15, 16): cuenta total equals sum of items; cobro closes cuenta and frees mesa; cash change = montoRecibido - total


---

## Phase 5: Productos, Categorías & Imagen

- [ ] 20. Implement CategoriaService and CategoriaResource
  - [ ] 20.1 Create `CategoriaService.java` with methods:
    - `findAll(): List<CategoriaDTO>` — ordered by nombre
    - `create(CreateCategoriaRequest): CategoriaDTO`
    - `update(UUID id, UpdateCategoriaRequest): CategoriaDTO`
    - `delete(UUID id)` — count associated Productos; throw `ConflictoEstadoException` with count if > 0
  - [ ] 20.2 Create `CategoriaDTO.java`: `{ id, nombre, descripcion, totalProductos }`
  - [ ] 20.3 Create `CreateCategoriaRequest.java` / `UpdateCategoriaRequest.java` with `@NotBlank nombre`
  - [ ] 20.4 Create `CategoriaResource.java` JAX-RS resource:
    - `GET /api/v1/categorias` `@RequiresRole(MESERO, ADMIN)` → list all
    - `POST /api/v1/categorias` `@RequiresRole(ADMIN)` → create, returns 201
    - `PUT /api/v1/categorias/{id}` `@RequiresRole(ADMIN)` → update, returns 200
    - `DELETE /api/v1/categorias/{id}` `@RequiresRole(ADMIN)` → delete (reject if has products), returns 204

- [ ] 21. Implement ProductoService and ProductoResource
  - [ ] 21.1 Create `ProductoService.java` with methods:
    - `findAll(PaginationParams, boolean soloActivos): Page<ProductoDTO>` — paginated, filter by activo
    - `findById(UUID id): ProductoDTO`
    - `create(CreateProductoRequest): ProductoDTO` — validate categoria exists, persist with activo=true
    - `update(UUID id, UpdateProductoRequest): ProductoDTO` — update attributes; price change only affects new orders (stored as snapshot in ItemPedido.precioUnitario)
    - `deactivate(UUID id)` — set activo=false; active orders not affected
    - `uploadImagen(UUID id, InputStream imageStream, String contentType, long fileSize): ProductoDTO` — validate format (JPG/PNG/WebP) and size (≤ 2MB), store file, update `imagen_url`, broadcast `PRODUCTO_ACTUALIZADO` via WebSocket
    - `deleteImagen(UUID id): ProductoDTO` — remove file, set `imagen_url=null`, broadcast `PRODUCTO_ACTUALIZADO`
  - [ ] 21.2 Create `ImageStorageService.java` `@ApplicationScoped` that: saves uploaded image to a configurable directory (e.g. `/workspace/uploads/productos/`), generates unique filename (`{uuid}.{ext}`), validates MIME type against allowed list, validates file size ≤ 2MB, deletes file on removal, returns relative URL path
  - [ ] 21.3 Create `ProductoDTO.java`: `{ id, nombre, descripcion, precio, categoria: CategoriaDTO, estacion, activo, imagenUrl, createdAt, updatedAt }`
  - [ ] 21.4 Create `CreateProductoRequest.java`: `{ nombre, descripcion, precio, categoriaId, estacion }` with `@NotBlank`, `@NotNull`, `@DecimalMin("0.01")` validations
  - [ ] 21.5 Create `UpdateProductoRequest.java`: `{ nombre, descripcion, precio, categoriaId, estacion }` — all optional fields
  - [ ] 21.6 Create `ProductoResource.java` JAX-RS resource:
    - `GET /api/v1/productos` `@RequiresRole(MESERO, ADMIN)` → paginated list (soloActivos=true by default for MESERO)
    - `POST /api/v1/productos` `@RequiresRole(ADMIN)` → create, returns 201
    - `PUT /api/v1/productos/{id}` `@RequiresRole(ADMIN)` → update, returns 200
    - `DELETE /api/v1/productos/{id}` `@RequiresRole(ADMIN)` → deactivate (soft delete), returns 204
    - `POST /api/v1/productos/{id}/imagen` `@RequiresRole(ADMIN)` → upload image (`multipart/form-data`), returns 200 + updated ProductoDTO
    - `DELETE /api/v1/productos/{id}/imagen` `@RequiresRole(ADMIN)` → remove image, returns 200 + updated ProductoDTO
  - [ ] 21.7 Configure static file serving in Quarkus (`quarkus.http.root-path`) to serve uploaded images from `/uploads/` path

- [ ] 22. Implement product image placeholder logic (Frontend)
  - [ ] 22.1 Create `ProductPlaceholderComponent` Angular standalone component that accepts `@Input() productName: string` and `@Input() imagenUrl: string | null`
  - [ ] 22.2 Implement `generatePlaceholderColor(name: string): string` pure function that: converts product name to a numeric hash (e.g. djb2 algorithm), maps hash to one of 12 predefined accessible background colors, returns hex color string — same name always returns same color (deterministic)
  - [ ] 22.3 Component template: if `imagenUrl` is set, render `<img [src]="imagenUrl" [alt]="productName">`; otherwise render `<div [style.background-color]="color" class="placeholder"><span>{{ productName }}</span></div>`
  - [ ] 22.4 Add SCSS styles: placeholder div is square with `border-radius: 8px`, centered text with contrasting color (white or dark based on background luminance), font-size responsive to container size
  - [ ] 22.5 Export `ProductPlaceholderComponent` from `SharedModule` for use across POS, KDS, BDS and Admin views

- [ ] 23. Write Producto & Categoria tests
  - [ ] 23.1 Create `CategoriaServiceTest.java`: create category, delete category with no products succeeds, delete category with products throws exception with count
  - [ ] 23.2 Create `ProductoServiceTest.java`: create product, deactivate product prevents adding to orders, price change does not affect existing open cuenta items, upload image validates format and size, delete image sets imagenUrl to null
  - [ ] 23.3 Create `PrecioPropertyTest.java` (PBT — Property 17): for any open Cuenta with ItemPedido at price X, changing Producto price to Y does not change existing ItemPedido.precioUnitario
  - [ ] 23.4 Create `ProductoDesactivadoPropertyTest.java` (PBT — Property 18): for any Producto with activo=false, adding it to any Pedido returns HTTP 400
  - [ ] 23.5 Create `PlaceholderColorPropertyTest.java` (PBT — Property 19): for any product name N, calling `generatePlaceholderColor(N)` twice returns identical color; test with min 100 different names using QuickTheories (Java) or Jasmine property helpers (Angular)
  - [ ] 23.6 Create `ImageStorageServiceTest.java`: valid JPG/PNG/WebP accepted, invalid format rejected, file > 2MB rejected, unique filename generated per upload


---

## Phase 6: Apache Camel, WebSocket, KDS, BDS & Cobro

- [ ] 24. Implement Apache Camel routes
  - [ ] 24.1 Add Camel Quarkus dependencies to `pom.xml`: `camel-quarkus-core`, `camel-quarkus-direct`, `camel-quarkus-log`, `camel-quarkus-bean`, `camel-quarkus-micrometer`
  - [ ] 24.2 Create `OrderRoutingRoute.java` extending `RouteBuilder`:
    - Configure `errorHandler(deadLetterChannel("direct:deadLetter").maximumRedeliveries(3).redeliveryDelay(1000).logExhausted(true))`
    - `from("direct:routeOrderItem")` with `routeId("order-routing-route")`
    - Validate message body is not null; send to `direct:deadLetter` if schema invalid
    - `choice()` Content-Based Router: `header("estacion").isEqualTo("COCINA")` → `direct:kdsQueue`; `header("estacion").isEqualTo("BARRA")` → `direct:bdsQueue`; otherwise → `direct:deadLetter`
    - Log processing time in milliseconds on success
  - [ ] 24.3 Create `KDSNotificationRoute.java` extending `RouteBuilder`:
    - `from("direct:kdsQueue")` with `routeId("kds-notification-route")`
    - Call `WebSocketBroadcastService.broadcastToKDS(exchange)`
    - Forward to `direct:metricsCollector`
  - [ ] 24.4 Create `BDSNotificationRoute.java` extending `RouteBuilder`:
    - `from("direct:bdsQueue")` with `routeId("bds-notification-route")`
    - Call `WebSocketBroadcastService.broadcastToBDS(exchange)`
    - Forward to `direct:metricsCollector`
  - [ ] 24.5 Create `DeadLetterRoute.java` extending `RouteBuilder`:
    - `from("direct:deadLetter")` with `routeId("dead-letter-route")`
    - Log at ERROR level: route id, original message body, exception cause, timestamp
    - Call `DeadLetterService.persist(exchange)` to store failed message in DB or log file
  - [ ] 24.6 Create `MetricsRoute.java` extending `RouteBuilder`:
    - `from("direct:metricsCollector")` with `routeId("metrics-route")`
    - Increment Micrometer counter `camel.messages.processed` tagged with `route` name
    - Record processing time in `camel.processing.time` timer
  - [ ] 24.7 Create `DeadLetterService.java` `@ApplicationScoped` that persists failed messages to a `dead_letter_messages` log table or file with: original body, route id, exception message, timestamp, retry count
  - [ ] 24.8 Create `OrderMessageDTO.java`: `{ itemPedidoId, pedidoId, cuentaId, mesaId, mesaNombre, productoNombre, cantidad, modificadores, estacion, createdAt }` — the message payload sent through Camel routes

- [ ] 25. Implement WebSocket endpoint and broadcast service
  - [ ] 25.1 Create `PosWebSocketEndpoint.java` `@ServerEndpoint("/ws")` `@ApplicationScoped`:
    - `@OnOpen onOpen(Session)` — register session in `WebSocketBroadcastService`, log `sessionId` connected
    - `@OnClose onClose(Session)` — unregister session, log `sessionId` disconnected
    - `@OnError onError(Session, Throwable)` — unregister session, log error with `sessionId`
    - `@OnMessage onMessage(String message, Session)` — handle client subscription messages (e.g. subscribe to specific `cuentaId`)
  - [ ] 25.2 Create `WebSocketBroadcastService.java` `@ApplicationScoped`:
    - Maintain `ConcurrentHashMap<String, Session> sessions` (sessionId → Session)
    - `register(Session)` / `unregister(Session)` methods
    - `broadcast(WebSocketEvent event)` — serialize event to JSON, send to all open sessions asynchronously, log disconnected sessions and remove them
    - `broadcastToKDS(Exchange)` — extract `OrderMessageDTO` from exchange, create `PEDIDO_CREADO` event, broadcast only to sessions with role COCINA or ADMIN
    - `broadcastToBDS(Exchange)` — same for BARRA role
    - `sendSyncState(Session)` — on reconnect, send `SYNC_STATE` event with current active mesas, pedidos, KDS items, BDS items
  - [ ] 25.3 Create `WebSocketEvent.java` DTO: `{ tipo: WebSocketEventTipo, payload: Object, timestamp: Instant }` with enum `WebSocketEventTipo { PEDIDO_CREADO, ITEM_ESTADO_CAMBIADO, PEDIDO_COMPLETO, MESA_ESTADO_CAMBIADO, STOCK_ALERTA, SYNC_STATE, BACKEND_UNAVAILABLE }`
  - [ ] 25.4 Configure Jackson serialization for `WebSocketEvent` to produce compact JSON with ISO-8601 timestamps

- [ ] 26. Implement KDSService and KDSResource
  - [ ] 26.1 Create `KDSService.java` with methods:
    - `findItems(): List<ItemPedidoDTO>` — items with estacion=COCINA and estado IN (PENDIENTE, PREPARANDO), ordered by `created_at` ASC (oldest first)
    - `updateEstado(UUID itemId, ItemPedidoEstado nuevoEstado, UUID userId)` — validate transition (PENDIENTE→PREPARANDO→LISTO only), set `preparando_en` or `listo_en` timestamp, persist, broadcast `ITEM_ESTADO_CAMBIADO`; if all items in pedido are LISTO broadcast `PEDIDO_COMPLETO`
  - [ ] 26.2 Create `KDSResource.java` JAX-RS resource:
    - `GET /api/v1/kds/items` `@RequiresRole(COCINA, ADMIN)` → list pending/in-progress items for kitchen
    - `PUT /api/v1/kds/items/{id}/estado` `@RequiresRole(COCINA, ADMIN)` → update item state, returns 200 + updated `ItemPedidoDTO`
  - [ ] 26.3 Add `tiempoEspera` calculation in `ItemPedidoDTO` mapping: seconds elapsed since `created_at`; flag `demorado=true` if > 900 seconds (15 min) for KDS

- [ ] 27. Implement BDSService and BDSResource
  - [ ] 27.1 Create `BDSService.java` with methods:
    - `findItems(): List<ItemPedidoDTO>` — items with estacion=BARRA and estado IN (PENDIENTE, PREPARANDO), ordered by `created_at` ASC
    - `updateEstado(UUID itemId, ItemPedidoEstado nuevoEstado, UUID userId)` — same logic as KDS; broadcast events; flag `demorado=true` if > 600 seconds (10 min) for BDS
  - [ ] 27.2 Create `BDSResource.java` JAX-RS resource:
    - `GET /api/v1/bds/items` `@RequiresRole(BARRA, ADMIN)` → list pending/in-progress items for bar
    - `PUT /api/v1/bds/items/{id}/estado` `@RequiresRole(BARRA, ADMIN)` → update item state, returns 200 + updated `ItemPedidoDTO`

- [ ] 28. Write Camel, WebSocket, KDS & BDS tests
  - [ ] 28.1 Create `OrderRoutingRouteTest.java` using `@QuarkusTest` with Camel test support: item with estacion=COCINA routes to kdsQueue only; item with estacion=BARRA routes to bdsQueue only; invalid message routes to deadLetter
  - [ ] 28.2 Create `RoutingPropertyTest.java` (PBT — Property 11): for any ItemPedido with estacion=COCINA, it appears in KDS queue and NOT in BDS queue; for estacion=BARRA, appears in BDS only
  - [ ] 28.3 Create `DeadLetterPropertyTest.java` (PBT — Property 24): for any message that fails schema validation or fails processing 3 times, it is captured by DLC and not silently discarded
  - [ ] 28.4 Create `WebSocketIntegrationTest.java` using `@QuarkusTest` with WebSocket client: connect, receive SYNC_STATE on connect, receive PEDIDO_CREADO when order is placed, receive ITEM_ESTADO_CAMBIADO when KDS updates item, reconnect after disconnect receives SYNC_STATE
  - [ ] 28.5 Create `KDSServiceTest.java`: items ordered by created_at ASC, state transition PENDIENTE→PREPARANDO→LISTO valid, invalid transitions rejected, PEDIDO_COMPLETO broadcast when all items LISTO, demorado flag set after 15 min
  - [ ] 28.6 Create `KDSOrderPropertyTest.java` (PBT — Property 13): for any list of KDS items, they are always ordered by created_at ascending
  - [ ] 28.7 Create `BDSServiceTest.java`: same as KDS but with 10-minute demorado threshold and BARRA estacion filter
  - [ ] 28.8 Create `BDSOrderPropertyTest.java` (PBT — Property 13 for BDS): items always ordered by created_at ascending


---

## Phase 7: Reportes & Frontend Angular

- [ ] 29. Implement ReporteService and ReporteResource
  - [ ] 29.1 Create `ReporteService.java` with methods:
    - `getReporteVentas(LocalDateTime desde, LocalDateTime hasta): ReporteVentasDTO` — query closed Cuentas in period: total ventas, count cuentas, ticket promedio, desglose por metodo_pago
    - `getReporteProductos(LocalDateTime desde, LocalDateTime hasta): List<ReporteProductoItemDTO>` — join ItemPedido + Producto for closed Cuentas: nombre, cantidad vendida, ingresos, categoria; ordered by cantidad DESC
    - `getReporteInventario(LocalDateTime desde, LocalDateTime hasta): ReporteInventarioDTO` — current stock per item, movements in period, active minimum stock alerts
    - `getReporteOperacion(LocalDateTime desde, LocalDateTime hasta): ReporteOperacionDTO` — avg preparation time per Estacion (preparando_en - created_at, listo_en - preparando_en), total pedidos processed, total cancellations
    - `exportVentasCSV(LocalDateTime desde, LocalDateTime hasta): byte[]` — generate CSV bytes for ventas report
    - `exportProductosCSV(LocalDateTime desde, LocalDateTime hasta): byte[]` — generate CSV bytes for productos report
  - [ ] 29.2 Create report DTOs: `ReporteVentasDTO { totalVentas, numeroCuentas, ticketPromedio, desglosePago: Map<String,BigDecimal>, periodo }`, `ReporteProductoItemDTO { nombre, cantidadVendida, ingresos, categoria }`, `ReporteInventarioDTO { items: List<ItemInventarioDTO>, movimientos, alertasActivas }`, `ReporteOperacionDTO { tiempoPromedioCocinaSeg, tiempoPromedioBarra Seg, totalPedidos, totalCancelaciones }`
  - [ ] 29.3 Create `ReporteResource.java` JAX-RS resource (all `@RequiresRole(ADMIN)`):
    - `GET /api/v1/reportes/ventas?desde=&hasta=` → ReporteVentasDTO; return empty report with zeros if no data
    - `GET /api/v1/reportes/productos?desde=&hasta=` → List<ReporteProductoItemDTO>
    - `GET /api/v1/reportes/inventario?desde=&hasta=` → ReporteInventarioDTO
    - `GET /api/v1/reportes/operacion?desde=&hasta=` → ReporteOperacionDTO
    - `GET /api/v1/reportes/ventas/export?desde=&hasta=` → CSV download (`Content-Disposition: attachment`)
    - `GET /api/v1/reportes/productos/export?desde=&hasta=` → CSV download
  - [ ] 29.4 Create `ReporteServiceTest.java`: ventas total equals sum of closed cuentas, empty period returns zeros, productos ordered by cantidad DESC
  - [ ] 29.5 Create `ReporteTotalesPropertyTest.java` (PBT — Property 23): for any time period P, total_ventas in report equals sum of all Cuenta.total where cerrada_en in P and estado=CERRADA

- [ ] 30. Implement Angular core services
  - [ ] 30.1 Create `AuthService` (`core/auth/auth.service.ts`): `login(username, password): Observable<AuthLoginResponse>`, `logout(): Observable<void>`, `changePassword(current, newPass): Observable<void>`, store token in `sessionStorage`, expose `currentUser$: BehaviorSubject<UsuarioDTO | null>`, `isAuthenticated(): boolean`, `hasRole(rol: string): boolean`
  - [ ] 30.2 Create `AuthGuard` (`core/auth/auth.guard.ts`) implementing `CanActivate`: redirect to `/login` if not authenticated
  - [ ] 30.3 Create `RoleGuard` (`core/auth/role.guard.ts`) implementing `CanActivate`: check `AuthService.hasRole()`, redirect to `/unauthorized` if role insufficient
  - [ ] 30.4 Create `AuthInterceptor` (`core/auth/auth.interceptor.ts`) implementing `HttpInterceptor`: attach `Authorization` header to all requests; on HTTP 401 response clear session and redirect to `/login` with "sesión expirada" message
  - [ ] 30.5 Create `WebSocketService` (`core/websocket/websocket.service.ts`): connect to `environment.wsUrl`, expose `events$: Observable<WebSocketEvent>`, implement exponential backoff reconnection (1s→2s→4s→8s→16s→30s max), expose `connectionStatus$: BehaviorSubject<'connected'|'reconnecting'|'error'>`, on reconnect request SYNC_STATE
  - [ ] 30.6 Create `ApiService` (`core/http/api.service.ts`): base HTTP service reading `environment.apiUrl`, generic `get<T>`, `post<T>`, `put<T>`, `delete<T>` methods with error handling
  - [ ] 30.7 Create `GlobalErrorHandler` (`core/error/global-error-handler.ts`) implementing `ErrorHandler`: log error with component context to console, show user-friendly toast notification without technical details
  - [ ] 30.8 Configure `AppModule` with: `HttpClientModule`, `RouterModule`, provide `AuthInterceptor` as `HTTP_INTERCEPTORS`, provide `GlobalErrorHandler` as `ErrorHandler`

- [ ] 31. Implement Angular routing and layout
  - [ ] 31.1 Configure `AppRoutingModule` with routes: `/login` (public), `/pos` (MESERO, ADMIN), `/kds` (COCINA, ADMIN), `/bds` (BARRA, ADMIN), `/admin/productos`, `/admin/inventario`, `/admin/usuarios`, `/admin/reportes`, `/admin/mesas` (all ADMIN), `/unauthorized`
  - [ ] 31.2 Apply `AuthGuard` to all routes except `/login`; apply `RoleGuard` with required role to each protected route
  - [ ] 31.3 Create `AppShellComponent` with: top navigation bar showing current user name + role + logout button, WebSocket connection status indicator (green/yellow/red dot), router outlet
  - [ ] 31.4 Create `ConnectionStatusBannerComponent`: shown when `WebSocketService.connectionStatus$ === 'error'`, displays warning message and disables action buttons via shared `DisabledWhenOfflineDirective`

- [ ] 32. Implement Angular feature modules

  - [ ] 32.1 Create `LoginComponent` (`features/login/`): form with username + password fields, call `AuthService.login()`, redirect to role-appropriate default route on success, show error message on failure

  - [ ] 32.2 Create `PosComponent` (`features/pos/`): fetch all mesas on init, subscribe to `WebSocketService.events$` filtering `MESA_ESTADO_CAMBIADO`, display grid of `MesaCardComponent`
  - [ ] 32.3 Create `MesaCardComponent`: display mesa nombre + estado with color coding (LIBRE=green, OCUPADA=red, RESERVADA=yellow), show elapsed time and total if OCUPADA, click opens `CuentaDetailComponent`
  - [ ] 32.4 Create `CuentaDetailComponent`: show all pedido rounds with items and states, `PedidoFormComponent` to add new round, cobro button
  - [ ] 32.5 Create `PedidoFormComponent`: product selector with `ProductPlaceholderComponent` thumbnails, quantity input, modificadores text field, add to cart, send round button
  - [ ] 32.6 Create `CobroComponent` (`features/cobro/`): show cuenta summary with subtotal + tax + total, payment method selector, cash amount input with change calculation, confirm button

  - [ ] 32.7 Create `KdsComponent` (`features/kds/`): fetch KDS items on init, subscribe to `PEDIDO_CREADO` and `ITEM_ESTADO_CAMBIADO` WebSocket events, display grid of `KdsItemComponent`
  - [ ] 32.8 Create `KdsItemComponent`: show product name, quantity, modificadores, mesa name, elapsed time timer (updates every second), highlight in red if `demorado=true` (>15 min), buttons to change state PENDIENTE→PREPARANDO→LISTO

  - [ ] 32.9 Create `BdsComponent` (`features/bds/`): same as KDS but for bar items, highlight if `demorado=true` (>10 min)
  - [ ] 32.10 Create `BdsItemComponent`: same as `KdsItemComponent` with 10-minute threshold

  - [ ] 32.11 Create `AdminProductosComponent` (`features/admin/productos/`): paginated product list with search, create/edit form with `ProductPlaceholderComponent` preview, image upload input (accept JPG/PNG/WebP, max 2MB client-side validation), delete image button
  - [ ] 32.12 Create `AdminInventarioComponent` (`features/admin/inventario/`): inventory list with stock alerts highlighted, movement registration form (ENTRADA/AJUSTE/MERMA with motivo), movement history table with date filter
  - [ ] 32.13 Create `AdminUsuariosComponent` (`features/admin/usuarios/`): user list, create/edit form with role selector, deactivate button, reset password button
  - [ ] 32.14 Create `AdminReportesComponent` (`features/admin/reportes/`): date range picker, tabs for Ventas/Productos/Inventario/Operacion, data tables, CSV export buttons
  - [ ] 32.15 Create `AdminMesasComponent` (`features/admin/mesas/`): mesa list, create/edit/delete forms

- [ ] 33. Write Angular tests
  - [ ] 33.1 Create `auth.service.spec.ts`: login stores token, logout clears token, `isAuthenticated()` returns false after logout
  - [ ] 33.2 Create `auth.guard.spec.ts`: unauthenticated user redirected to `/login`
  - [ ] 33.3 Create `auth.interceptor.spec.ts`: token attached to requests, HTTP 401 clears session and redirects
  - [ ] 33.4 Create `websocket.service.spec.ts`: reconnection attempts with exponential backoff, SYNC_STATE requested on reconnect
  - [ ] 33.5 Create `product-placeholder.component.spec.ts` (PBT — Property 19 frontend): `generatePlaceholderColor(name)` called twice with same name returns same color; test with 50+ different names
  - [ ] 33.6 Create `mesa-card.component.spec.ts`: LIBRE shows green, OCUPADA shows red, RESERVADA shows yellow, elapsed time displayed when OCUPADA
  - [ ] 33.7 Create `kds-item.component.spec.ts`: demorado class applied after 15 min, state buttons trigger correct API calls
  - [ ] 33.8 Create `pos-flow.e2e.spec.ts` (E2E): login as mesero → open mesa → add pedido → verify KDS receives item → mark as LISTO → cobrar → verify mesa is LIBRE


---

## Phase 8: Health Checks, Metrics, Logging & Remaining PBT

- [ ] 34. Implement health checks and metrics
  - [ ] 34.1 Add MicroProfile Health dependency to `pom.xml`: `quarkus-smallrye-health`
  - [ ] 34.2 Create `DatabaseHealthCheck.java` implementing `HealthCheck` with `@Liveness`: execute `SELECT 1` against PostgreSQL; return UP if successful, DOWN with error message if connection fails
  - [ ] 34.3 Create `DatabaseReadinessCheck.java` implementing `HealthCheck` with `@Readiness`: verify active DB connection AND verify all Camel routes are started via `CamelContext.getRouteController()`; return DOWN with failing component name if either check fails
  - [ ] 34.4 Create `CamelRoutesHealthCheck.java` implementing `HealthCheck` with `@Readiness`: iterate all registered Camel routes, return DOWN with route id if any route is in STOPPED or FAILED state
  - [ ] 34.5 Verify Quarkus exposes `/q/health/live` (liveness) and `/q/health/ready` (readiness) automatically via SmallRye Health
  - [ ] 34.6 Add `quarkus-micrometer-registry-prometheus` to `pom.xml` and verify `/q/metrics` endpoint returns Prometheus format with: HTTP request counts by endpoint and status code, response time percentiles (p50/p95/p99), JVM memory usage
  - [ ] 34.7 Register custom Micrometer counters in `MetricsRoute.java`: `camel.messages.processed{route}`, `camel.messages.failed{route}`, `camel.dead.letter.count`
  - [ ] 34.8 Add `/q/info` endpoint configuration in `application.properties` with build metadata: `quarkus.info.git.enabled=true` to expose branch and commit hash
  - [ ] 34.9 Configure Docker Compose `healthcheck` for `backend` service: `curl -f http://localhost:8080/q/health/live` every 30s, timeout 5s, 3 retries, start_period 60s
  - [ ] 34.10 Configure Docker Compose `healthcheck` for `db` service: `pg_isready -U pos_user -d pos_db` every 10s, timeout 3s, 5 retries

- [ ] 35. Implement structured logging
  - [ ] 35.1 Configure JSON structured logging in `application.properties`: `quarkus.log.console.json=true`, `quarkus.log.console.json.pretty-print=false`
  - [ ] 35.2 Verify `TraceIdFilter.java` (Task 10.1) stores `traceId` in MDC so all log entries within a request automatically include it in JSON output
  - [ ] 35.3 Add `traceId` field to all log statements in service layer using `MDC.get("traceId")` — verify INFO logs for transaction start/end include operation type and duration ms
  - [ ] 35.4 Configure log levels per profile in `application-dev.properties` (`QUARKUS_LOG_LEVEL=DEBUG`) and `application-prod.properties` (`QUARKUS_LOG_LEVEL=INFO`)
  - [ ] 35.5 Add FATAL-level startup check in `AppLifecycleBean.java` `@ApplicationScoped` `@Observes StartupEvent`: attempt DB connection; if fails log FATAL with datasource URL (without password) and call `System.exit(1)`
  - [ ] 35.6 Add WARN-level logging in all service methods that throw `BusinessException` (stock insuficiente, estado inválido, permisos insuficientes) — log field name and reason without sensitive user data
  - [ ] 35.7 Add INFO-level logging for transaction boundaries in `@Transactional` methods: log operation type, entity id, and duration on completion

- [ ] 36. Implement frontend connectivity monitoring
  - [ ] 36.1 Update `WebSocketService` to emit `connectionStatus$` values: `'connected'` on open, `'reconnecting'` during backoff, `'error'` after max retries exceeded
  - [ ] 36.2 Create `BackendHealthService` (`core/http/backend-health.service.ts`): poll `GET /q/health/live` every 30s; if no response within 10s emit `backendUnavailable$: true`; restore to `false` when backend responds
  - [ ] 36.3 Update `ConnectionStatusBannerComponent` to show banner when either `WebSocketService.connectionStatus$ === 'error'` OR `BackendHealthService.backendUnavailable$ === true`
  - [ ] 36.4 Create `DisabledWhenOfflineDirective` Angular directive: subscribes to `BackendHealthService.backendUnavailable$`, sets `[disabled]=true` on host element when offline, restores when online

- [ ] 37. Write remaining property-based tests
  - [ ] 37.1 Create `ValidationPropertyTest.java` (PBT — Property 25): for any HTTP request with missing required field F, response is HTTP 400 with body containing field name F and validation description; test across all resource endpoints
  - [ ] 37.2 Create `PaginationPropertyTest.java` (PBT — Property 26): for any paginated request with `page=P` and `size=S` (S ≤ 100), response contains at most S items and includes `totalElements`, `totalPages`, `currentPage` metadata
  - [ ] 37.3 Create `LoggingPropertyTest.java` (PBT — Property 27): for any HTTP request R that generates multiple log entries, all entries share the same `traceId` value; verify by capturing MDC in test
  - [ ] 37.4 Create `AuthPropertyTest.java` (PBT — Property 1): for any credentials where username does not exist or password does not match hash, authentication returns HTTP 401
  - [ ] 37.5 Add QuickTheories dependency to `pom.xml`: `org.quicktheories:quicktheories:0.26` scope `test`; verify minimum 100 iterations per property test via `qt().withExamples(100)`

- [ ] 38. Write smoke and integration tests
  - [ ] 38.1 Create `HealthCheckTest.java` `@QuarkusTest`: `GET /q/health/live` returns 200 when DB is up; `GET /q/health/ready` returns 200 when DB and Camel routes are up; `GET /q/health/ready` returns 503 when DB is down (use `@QuarkusTestResource` to simulate DB failure)
  - [ ] 38.2 Create `MetricsTest.java` `@QuarkusTest`: `GET /q/metrics` returns 200 with `Content-Type: text/plain` (Prometheus format); response contains `camel_messages_processed_total` metric after processing an order
  - [ ] 38.3 Create `OpenApiTest.java` `@QuarkusTest`: `GET /q/openapi` returns 200 with valid OpenAPI 3.0 JSON; all resource paths are documented; `GET /q/swagger-ui` returns 200 in dev profile
  - [ ] 38.4 Create `CorsTest.java` `@QuarkusTest`: request from allowed origin returns CORS headers; request from unauthorized origin returns HTTP 403
  - [ ] 38.5 Create `GzipTest.java` `@QuarkusTest`: response body > 1KB with `Accept-Encoding: gzip` header returns compressed response with `Content-Encoding: gzip`

- [ ] 39. Final integration and cleanup
  - [ ] 39.1 Run full test suite (`mvn test`) and fix any failing tests
  - [ ] 39.2 Run `ng test --watch=false` and fix any failing Angular tests
  - [ ] 39.3 Verify DevContainer builds successfully end-to-end: `docker compose up --build` starts all 3 services, healthchecks pass, backend is reachable at `http://localhost:8080/q/health/ready`
  - [ ] 39.4 Verify Angular dev server starts at `http://localhost:4200` and can reach backend API
  - [ ] 39.5 Verify all 27 correctness properties have corresponding property-based tests tagged with `@Tag("Feature: devcontainer-setup, Property N: ...")`
  - [ ] 39.6 Update `README.md` with any final commands, known issues, and contribution guidelines


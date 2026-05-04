# Contexto del Proyecto POS Restaurante

## Descripción
Sistema de Punto de Venta (POS) para restaurante. Proyecto nuevo desde cero con DevContainer.

## Stack Tecnológico
- **Backend**: Java 17 + Quarkus 3.8.3 + Apache Camel 3.8.0 + PostgreSQL 15
- **Frontend**: Angular 17 (SPA standalone components)
- **Orquestación**: Docker Compose (3 servicios: db, backend, frontend)
- **Testing**: JUnit 5 + QuickTheories (property-based testing)
- **DevContainer**: VS Code Dev Containers con features Java 17 + Node 20

## Estructura del Proyecto
```
pos/
├── .devcontainer/          # Docker Compose + devcontainer.json + .env.example
├── .kiro/specs/devcontainer-setup/  # Spec completo (requirements, design, tasks)
├── pos-backend/            # Java/Quarkus/Camel — puerto 8080
└── pos-frontend/           # Angular 17 — puerto 4200
```

## Módulos Implementados (Backend)
- `auth/` — Autenticación Basic Auth + RBAC (roles: ADMIN, MESERO, COCINA, BARRA)
- `mesa/` — Gestión de mesas (estados: LIBRE, OCUPADA, RESERVADA)
- `cuenta/` — Cuentas, pedidos y cobro con IVA 16%
- `pedido/` — Items de pedido con snapshot de precio
- `kds/` — Kitchen Display System (cocina, demora >15min)
- `bds/` — Bar Display System (barra, demora >10min)
- `producto/` — Productos + categorías + imagen (JPG/PNG/WebP ≤2MB) + placeholder
- `inventario/` — Stock con reservas, movimientos auditados, alertas
- `usuario/` — CRUD usuarios con desactivación e invalidación de sesiones
- `reporte/` — Ventas, productos, inventario, operación + export CSV
- `camel/` — OrderRoutingRoute (Content-Based Router) + Dead Letter Channel
- `websocket/` — WebSocket en /ws con broadcast por rol y SYNC_STATE
- `health/` — Liveness + Readiness checks + métricas Prometheus
- `common/` — TraceIdFilter, GlobalExceptionMapper, PaginationParams, AppLifecycleBean

## Módulos Implementados (Frontend Angular)
- `core/auth/` — AuthService, AuthGuard, RoleGuard, AuthInterceptor
- `core/websocket/` — WebSocketService con reconexión exponencial (1s→2s→4s→8s→16s)
- `core/http/` — BackendHealthService (polling cada 30s)
- `features/login/` — LoginComponent con redirección por rol
- `features/pos/` — PosComponent (mapa de mesas con WebSocket)
- `features/kds/` — KdsComponent (cocina, actualización en tiempo real)
- `features/bds/` — BdsComponent (barra, actualización en tiempo real)
- `features/cobro/` — CobroComponent (resumen + IVA + cambio)
- `features/admin/` — Productos, Inventario, Usuarios, Reportes, Mesas
- `shared/components/product-placeholder/` — Placeholder determinista (djb2 hash)
- `shared/directives/disabled-when-offline/` — Deshabilita botones sin conexión

## Base de Datos
- 11 tablas con Flyway migrations (V1__create_schema.sql, V2__seed_data.sql)
- Usuario admin inicial: username=`admin`, password=`admin123`
- 5 categorías y 5 mesas de ejemplo en seed data

## Puertos
| Puerto | Servicio |
|--------|----------|
| 8080 | Quarkus HTTP + Swagger UI (/q/swagger-ui) |
| 5005 | Java Debug (JDWP) |
| 4200 | Angular Dev Server |
| 5432 | PostgreSQL |

## Propiedades de Corrección (27 Properties PBT)
Implementadas con QuickTheories. Ver `design.md` para la lista completa.
Cubren: RBAC, máquinas de estado, inventario, enrutamiento Camel, cobro, placeholder, logging.

## Cómo Levantar
```bash
cp .devcontainer/.env.example .devcontainer/.env
# Abrir en VS Code → "Dev Containers: Reopen in Container"
# O: docker compose -f .devcontainer/docker-compose.yml up --build db backend
```

## Spec
Ver `.kiro/specs/devcontainer-setup/` para:
- `requirements.md` — 17 requerimientos funcionales (Req 10-26)
- `design.md` — Arquitectura, modelo de datos, API REST, Camel, WebSocket
- `tasks.md` — 39 tareas de implementación en 8 fases (todas completadas)
- `infrastructure-context.md` — Requisitos de infraestructura DevContainer

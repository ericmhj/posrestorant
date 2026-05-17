# 📊 Plan de Trabajo: Reportes Granulares

**Objetivo**: Implementar reportes granulares de forma incremental, asegurando sincronización Backend ↔ Frontend

**Duración estimada**: 5 fases de 2-3 días cada una

---

## 🎯 Principios del Plan

✅ **Dependency-First**: Implementar siempre backend antes que frontend
✅ **Incremental**: Cada fase entrega funcionalidad usable
✅ **Testeable**: Cada fase se valida con Swagger/Postman
✅ **No Breaking**: Cambios anteriores siempre funcionan
✅ **Documentado**: DTOs y endpoints claros

---

## 📅 FASE 1: INFRAESTRUCTURA COMPARTIDA (1-2 días)

### Objetivo
Establecer DTOs, servicios base y endpoints comunes para todas las fases.

#### Backend
- [ ] **DTO Nuevo**: `PeriodoFiltroDTO` - Rango fechas, turno, usuario
- [ ] **Enum Nuevo**: `TurnoEnum` (ALMUERZO, CENA, NOCHE)
- [ ] **Base Query**: Método helper en `ReporteService` para filtrar por período/turno
- [ ] **Endpoint**: `GET /api/v1/reportes/filtros/turnos` - Lista de turnos
- [ ] **Endpoint**: `GET /api/v1/reportes/filtros/meseros` - Lista de meseros activos
- [ ] **Validación**: Verificar fechas válidas, usuario existe

#### Frontend
- [ ] **Servicio**: `reporte.service.ts` con métodos base
- [ ] **Modelo**: `PeriodoFiltro` interface
- [ ] **Modelo**: `TurnoEnum` type
- [ ] **Componente**: `filtro-reporte.component.ts` - Selector visual
- [ ] **Módulo**: Crear carpeta `/reportes` con estructura base
- [ ] **Routing**: Configurar rutas base `/admin/reportes`

#### ✔️ Validación (Fase 1)
```bash
# Backend
curl http://localhost:8080/api/v1/reportes/filtros/turnos
curl http://localhost:8080/api/v1/reportes/filtros/meseros

# Frontend
- Filtro visible en pantalla
- Selector de fechas funciona
- Dropdown de meseros carga
```

---

## 📅 FASE 2: REPORTE DE VENTAS GRANULAR (2-3 días)

### Objetivo
Reemplazar reporte de ventas actual con versión granular + por mesero.

#### Backend

**2a. Actualizar Reporte de Ventas General**
- [ ] **DTO Update**: `ReporteVentasDTO` agregar campos:
  - `desglosePorTurno: Map<String, BigDecimal>`
  - `desglosePorHora: Map<Integer, BigDecimal>`
  - `ventasMin: BigDecimal`, `ventasMax: BigDecimal`
  - `medianaVentas: BigDecimal`
  
- [ ] **Query Update**: Modificar `getReporteVentas()` para incluir:
  - Turno (almuerzo/cena/noche)
  - Hora del día
  - Estadísticas (min/max/mediana)

- [ ] **Endpoint Update**: `GET /api/v1/reportes/ventas`
  - Agregar parámetro `?turno=ALMUERZO` (opcional)
  - Agregar parámetro `?detalleHora=true` (por defecto false)

**2b. Nuevo Reporte: Ventas por Mesero**
- [ ] **DTO Nuevo**: `ReporteMeseroDTO`
  ```
  userId: UUID
  nombreMesero: String
  totalVentas: BigDecimal
  numeroCuentas: Long
  ticketPromedio: BigDecimal
  propinaTotalRecibida: BigDecimal
  propinaPromedio: BigDecimal
  tiempoPromedioMinutos: Double
  tasaCancelacion: Double (0.0-1.0)
  eficiencia: Double (%)
  ```

- [ ] **Query Nuevo**: `getReporteMeserosPorPeriodo(LocalDateTime desde, hasta, turno?)`
  - JOIN con tabla Usuario
  - Calcular tiempo promedio por cuenta
  - Calcular tasa de cancelación

- [ ] **Endpoint Nuevo**: `GET /api/v1/reportes/ventas/meseros?desde=&hasta=&turno=`
  - Retorna `List<ReporteMeseroDTO>`

- [ ] **Endpoint Nuevo**: `GET /api/v1/reportes/ventas/meseros/{userId}?desde=&hasta=`
  - Retorna detalle individual de un mesero

#### Frontend

- [ ] **Modelo**: `ReporteMesero` interface + `ReporteVentas` update
- [ ] **Componente**: `ventas-general.component.ts`
  - Mostrar reporte general con estadísticas mejoradas
  - Gráfico de línea: Ventas por hora (Chart.js)
  - Tabla: Desglose por turno
  
- [ ] **Componente**: `ventas-mesero.component.ts`
  - Tabla con lista de meseros
  - Columnas: Nombre, Cuentas, Ventas, Ticket, Propina, Eficiencia, Devoluciones
  - Click en fila → detalle individual
  
- [ ] **Componente**: `ventas-mesero-detalle.component.ts`
  - Gráfico de ventas diarias del mesero
  - Estadísticas individuales
  - Botón volver

- [ ] **Servicio Update**: `reporte.service.ts`
  - `getReporteVentas(filtro: PeriodoFiltro): Observable<ReporteVentas>`
  - `getReporteMeseros(filtro: PeriodoFiltro): Observable<ReporteMesero[]>`
  - `getReporteMeseroDetalle(userId, filtro): Observable<ReporteMesero>`

#### ✔️ Validación (Fase 2)
```bash
# Backend - Swagger UI
curl http://localhost:8080/q/swagger-ui  # Probar endpoints interactivamente

# Frontend
- Filtro + clic "Ventas" → muestra reporte actualizado
- Nuevo desglose por hora visible
- Click en mesero → abre detalle
- Gráfico renderiza correctamente
```

---

## 📅 FASE 3: REPORTE DE PRODUCTOS RENTABILIDAD (2-3 días)

### Objetivo
Añadir análisis de margen, costo y ROI por producto.

#### Backend

**3a. Nuevas Columnas en BD** (si no existen)
- [ ] Verificar: `Producto.precioUnitario`, `Producto.costoCunitario`
- [ ] Si falta: Agregar migración Flyway

**3b. DTO Nuevo**
- [ ] **DTO**: `ReporteProductoRentabilidadDTO`
  ```
  productoId: UUID
  nombre: String
  cantidad: Long
  ingresoTotal: BigDecimal
  costoTotal: BigDecimal
  margenBruto: BigDecimal ($)
  margenPorcentaje: BigDecimal (%)
  roi: BigDecimal (veces)
  categoria: String
  estacion: String
  ```

**3c. Query Nueva**
- [ ] Método: `getReporteRentabilidad(desde, hasta, categoría?, estacion?)`
  - Calcular margen = (precio - costo) * cantidad
  - Calcular ROI = ingresos / costo
  - Ordenar por margen DESC

**3d. Endpoints Nuevos**
- [ ] `GET /api/v1/reportes/productos/rentabilidad?desde=&hasta=&categoria=&estacion=`
  - Retorna `List<ReporteProductoRentabilidadDTO>`

#### Frontend

- [ ] **Modelo**: `ReporteProductoRentabilidad` interface
- [ ] **Componente**: `productos-rentabilidad.component.ts`
  - Tabla interactiva: Producto, Qty, Ingresos, Costo, Margen$, Margen%, ROI
  - Orden clickeable por columna
  - Color: Verde (alto margen) → Rojo (bajo margen)
  - Gráfico de barras: Top 10 por margen

- [ ] **Componente**: `productos-rentabilidad-detalle.component.ts`
  - Gráfico línea: Evolución margen durante período
  - Comparar contra otros productos

- [ ] **Servicio Update**: `reporte.service.ts`
  - `getReporteRentabilidad(filtro: PeriodoFiltro): Observable<ReporteProductoRentabilidad[]>`

#### ✔️ Validación (Fase 3)
```bash
# Backend - Swagger
curl http://localhost:8080/api/v1/reportes/productos/rentabilidad?desde=...

# Frontend
- Tabla muestra datos correcto
- Ordenar por columna funciona
- Colores reflejan margen
- Gráfico renderiza
```

---

## 📅 FASE 4: REPORTE DE OPERACIÓN GRANULAR (2-3 días)

### Objetivo
Desglosar horas pico, eficiencia de estaciones, cuello de botella.

#### Backend

**4a. DTO Nuevo: Horas Pico**
- [ ] **DTO**: `ReporteHoraPicoDTO`
  ```
  hora: Integer (0-23)
  ventasTotal: BigDecimal
  cuentasTotal: Long
  tiempoPromedioPedido: Double (segundos)
  articulosVendidos: Long
  estacionMasCongestinada: String (COCINA/BARRA)
  ```

**4b. DTO Nuevo: Estaciones**
- [ ] **DTO**: `ReporteEstacionDTO`
  ```
  estacion: String (COCINA/BARRA)
  tiempoPromedio: Double (segundos)
  tiempoMin: Double
  tiempoMax: Double
  totalPedidos: Long
  productoMasLento: String
  productoMasRapido: String
  productosDetalles: List<ProductoTiempoDTO>
  ```

**4c. Queries Nuevas**
- [ ] Método: `getReporteHorasPico(desde, hasta, turno?)`
  - Agrupar por hora
  - Calcular métricas por hora

- [ ] Método: `getReporteEstaciones(desde, hasta, estacion)`
  - Estadísticas de tiempo por estación
  - Listar productos por tiempo

**4d. Endpoints Nuevos**
- [ ] `GET /api/v1/reportes/operacion/horas-pico?desde=&hasta=&turno=`
  - Retorna `List<ReporteHoraPicoDTO>`

- [ ] `GET /api/v1/reportes/operacion/estaciones/{estacion}?desde=&hasta=`
  - Retorna `ReporteEstacionDTO`

#### Frontend

- [ ] **Modelo**: Interfaces de DTOs nuevos
- [ ] **Componente**: `horas-pico.component.ts`
  - Gráfico línea: Ingresos por hora
  - Gráfico barras: Cuentas por hora
  - Tabla: Estadísticas por hora
  - Highlight: Hora pico

- [ ] **Componente**: `estaciones.component.ts`
  - Dos paneles lado a lado: COCINA vs BARRA
  - Cada panel: tiempo promedio, min, max, total
  - Tabla: productos ordenados por tiempo
  - Indicador: "Cuello de botella en X"

- [ ] **Componente**: `operacion-detalle.component.ts`
  - Comparar dos estaciones
  - Ver evolución en el tiempo

- [ ] **Servicio Update**: `reporte.service.ts`
  - `getReporteHorasPico(filtro): Observable<ReporteHoraPico[]>`
  - `getReporteEstaciones(estacion, filtro): Observable<ReporteEstacion>`

#### ✔️ Validación (Fase 4)
```bash
# Backend - Swagger
curl http://localhost:8080/api/v1/reportes/operacion/horas-pico?desde=...
curl http://localhost:8080/api/v1/reportes/operacion/estaciones/COCINA?desde=...

# Frontend
- Gráficos renderizaban
- Tabla muestra datos correctos
- Identificar cuello botella visible
```

---

## 📅 FASE 5: REPORTES AVANZADOS (2-3 días)

### Objetivo
Dashboard gerencial, inventario con proyecciones, exportación.

#### Backend

**5a. Dashboard Gerencial - DTOs**
- [ ] **DTO**: `DashboardReportesDTO`
  ```
  vendidoHoy: BigDecimal
  cuentasHoy: Long
  tiempoPromedioCocina: Double
  tiempoPromedioBarra: Double
  meseroTop: String (nombre)
  productoTop: String (nombre)
  horaMaximaVentas: Integer
  alertasInventario: List<AlertaInventario>
  periodo: PeriodoFiltroDTO
  ```

**5b. Proyecciones Inventario - DTOs**
- [ ] **DTO**: `ProyeccionInventarioDTO`
  ```
  itemId: UUID
  nombre: String
  stockActual: BigDecimal
  consumoPromedioDia: BigDecimal
  diasParaAgotarse: Integer
  proximaFechaAgotamiento: LocalDate
  nivelMinimo: BigDecimal
  necesitaReorden: Boolean
  proyeccion7dias: List<ProyeccionDiaDTO>
  ```

**5c. Queries Nuevas**
- [ ] Método: `getDashboardReportes(desde, hasta)`
  - Consolidar KPIs principales

- [ ] Método: `getProyeccionesInventario(dias: Integer)`
  - Calcular consumo promedio
  - Proyectar hacia adelante

**5d. Endpoints Nuevos**
- [ ] `GET /api/v1/reportes/dashboard?desde=&hasta=`
  - Retorna `DashboardReportesDTO`

- [ ] `GET /api/v1/reportes/inventario/proyecciones?dias=7`
  - Retorna `List<ProyeccionInventarioDTO>`

- [ ] `GET /api/v1/reportes/{tipoReporte}/export?formato=CSV&desde=&hasta=`
  - Retorna archivo bytes

#### Frontend

- [ ] **Modelo**: Interfaces de DTOs nuevos
- [ ] **Componente**: `dashboard-reportes.component.ts`
  - 6 tarjetas KPI principales
  - Cada tarjeta clickeable → detalle
  - Selector período (Hoy/Semana/Mes)
  - Grid responsive

- [ ] **Componente**: `inventario-proyecciones.component.ts`
  - Tabla: Producto, Stock Actual, Consumo/día, Días restantes
  - Color: Verde (abundante) → Rojo (urgente)
  - Botón "Generar orden de compra"
  - Gráfico: Proyección 7 días para producto seleccionado

- [ ] **Componente**: `exportar-reporte.component.ts` (compartido)
  - Selector formato (CSV/PDF/XLSX)
  - Botón descargar
  - Indicador de progreso

- [ ] **Servicio Update**: `reporte.service.ts`
  - `getDashboardReportes(filtro): Observable<Dashboard>`
  - `getProyeccionesInventario(dias): Observable<Proyeccion[]>`
  - `exportarReporte(tipo, formato, filtro): Observable<Blob>`

- [ ] **Routing Update**: Agregar rutas a nuevos componentes

#### ✔️ Validación (Fase 5)
```bash
# Backend - Swagger
curl http://localhost:8080/api/v1/reportes/dashboard?desde=...
curl http://localhost:8080/api/v1/reportes/inventario/proyecciones?dias=7

# Frontend
- Dashboard carga 6 tarjetas
- Click en tarjeta → navega a detalle
- Inventario muestra proyecciones
- Descargar CSV funciona
```

---

## 🔄 SINCRONIZACIÓN: Checklist por Fase

### Fase 1
- [ ] Backend: DTOs y endpoints base compilados
- [ ] Swagger muestra nuevos endpoints
- [ ] Frontend: Servicio consume endpoints correctamente
- [ ] Filtro componente renderiza sin errores

### Fase 2
- [ ] Backend: Queries devuelven datos correctos
- [ ] Frontend: Tablas muestran datos sin deformaciones
- [ ] Gráficos renderizen correctamente
- [ ] Navegación entre vistas funciona

### Fase 3
- [ ] Backend: Cálculos de margen y ROI son correctos
- [ ] Frontend: Colores reflejan los valores
- [ ] Ordenamiento por columna funciona

### Fase 4
- [ ] Backend: Timestamps en queries son consistentes
- [ ] Frontend: Gráficos temporales muestran datos correctos
- [ ] Identificación de cuello de botella visible

### Fase 5
- [ ] Backend: Dashboard consolidado contiene todos los KPIs
- [ ] Frontend: Todas las rutas funcionan
- [ ] Exportación genera archivos válidos
- [ ] Responsivo en móvil

---

## 📊 Matriz de Dependencias

```
FASE 1 (Base)
    ↓
FASE 2 (Ventas) — FASE 3 (Productos) — FASE 4 (Operación)
    ↓                   ↓                   ↓
    └───────────────────┴───────────────────┘
                    ↓
                FASE 5 (Dashboard + Exportación)
```

**Nota**: Las fases 2, 3, 4 pueden hacerse **en paralelo** si tienes dos desarrolladores.

---

## 🧪 Testing por Fase

| Fase | Backend Test | Frontend Test | E2E |
|------|--------------|---------------|-----|
| 1 | Endpoints retornan 200 | Componentes renderizan | Filtros funcionan |
| 2 | Queries SQL correctas | Tablas sin deformación | Navegar meseros |
| 3 | Cálculos de margen | Colores correctos | Ordenar columna |
| 4 | Agrupaciones por hora | Gráficos renderizaban | Identificar cuello |
| 5 | Consolidación de KPIs | Dashboard completo | Exportar archivo |

---

## ⚠️ Riesgos y Mitigación

| Riesgo | Causa | Solución |
|--------|-------|----------|
| Queries lentas | BD grande | Agregar índices en `createdAt`, `cerradaEn` |
| Datos inconsistentes | Cache | Invalidar cache después de cada update |
| Gráficos rotos | Librería incompatible | Usar `ng-chartjs` 4.1+ compatible Angular 17 |
| Filtro no funciona | Validación | Validar fechas en backend + frontend |


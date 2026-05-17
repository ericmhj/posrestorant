# 📝 LISTADO DETALLADO: Cambios Backend ↔ Frontend por Funcionalidad

---

## 📌 FUNCIONALIDAD 1: Reporte de Ventas General (Mejorado)

### ❌ ANTES
```
Backend: ReporteVentas simple (total, promedio, métodos pago)
Frontend: Card basico mostrando 3 números
```

### ✅ DESPUÉS

#### BACKEND - Cambios
```java
// 1. DTO ACTUALIZADO
ARCHIVO: ReporteVentasDTO.java
├─ totalVentas: BigDecimal (EXISTENTE)
├─ numeroCuentas: Long (EXISTENTE)
├─ ticketPromedio: BigDecimal (EXISTENTE)
├─ desglosePago: Map (EXISTENTE)
├─ NEW → desglosePorTurno: Map<String, BigDecimal>
├─ NEW → desglosePorHora: Map<Integer, BigDecimal>
├─ NEW → ventasMinima: BigDecimal
├─ NEW → ventasMaxima: BigDecimal
└─ NEW → medianaVentas: BigDecimal

// 2. QUERY ACTUALIZADA
ARCHIVO: ReporteService.java → getReporteVentas()
├─ CAMBIO: Agregar parámetro turno?
├─ CAMBIO: Agregar agrupación por HOUR(cerradaEn)
├─ CAMBIO: Calcular MIN, MAX, MEDIAN
└─ CAMBIO: GROUP BY turno (ALMUERZO/CENA/NOCHE basado en hora)

// 3. ENDPOINT ACTUALIZADO
GET /api/v1/reportes/ventas
├─ NUEVO PARAM: ?turno=ALMUERZO (opcional)
├─ NUEVO PARAM: ?detalleHora=true (default: false)
└─ RESPUESTA: ReporteVentasDTO con nuevos campos
```

#### FRONTEND - Cambios
```typescript
// 1. MODELO ACTUALIZADO
ARCHIVO: src/app/features/admin/reportes/models/reporte-ventas.model.ts
├─ NUEVO: export interface ReporteVentas
└─ CAMPOS: todos los del DTO backend (con tipos TypeScript)

// 2. COMPONENTE ACTUALIZADO
ARCHIVO: ventas-general.component.ts
├─ TEMPLATE CAMBIOS:
│  ├─ OLD: 3 párrafos simples
│  ├─ NEW: 6 tarjetas de métrica (Total, Cuentas, Ticket, Min, Max, Mediana)
│  ├─ NEW: Selector Turno: <select [(ngModel)]="turnoSeleccionado">
│  └─ NEW: Checkbox "Detalles por hora"
│
├─ COMPONENTE CAMBIOS:
│  ├─ NEW: turnoSeleccionado: string
│  ├─ NEW: detalleHoraEnabled: boolean
│  ├─ NEW: ventasReporte: ReporteVentas (tipado)
│  ├─ UPDATE: loadVentas() → incluir turno y detalleHora en params
│  └─ NEW: onTurnoChange() → reload con nuevo turno

// 3. GRÁFICO NUEVO
ARCHIVO: ventas-general.component.ts
├─ NUEVA LIBRERÍA: Chart.js / ng-chartjs
├─ TIPO: Line chart
├─ EJE X: Horas (0-23)
├─ EJE Y: Ventas por hora
└─ DATOS: ventasReporte.desglosePorHora

// 4. TABLA NUEVA
ARCHIVO: ventas-general.component.ts (inline o sub-componente)
├─ COLUMNAS: Turno | Total Ventas | Número Cuentas | Ticket Promedio
└─ DATOS: ventasReporte.desglosePago (mapeado)

// 5. SERVICIO ACTUALIZADO
ARCHIVO: reporte.service.ts
├─ MÉTODO ACTUALIZADO: getReporteVentas(filtro: PeriodoFiltro, turno?: string)
└─ RETURN: Observable<ReporteVentas>
```

#### VERIFICACIÓN
```bash
# Backend - Swagger: http://localhost:8080/q/swagger-ui
GET /api/v1/reportes/ventas
  Parámetros visibles: desde, hasta, turno, detalleHora
  Response 200: { totalVentas, numeroCuentas, desglosePorHora {...} }

# Frontend - http://localhost:4200
- Selector Turno visible y funcional
- Gráfico renderiza línea con datos de desglosePorHora
- Tabla muestra desglose por turno
- Click en turno filtra datos
```

---

## 📌 FUNCIONALIDAD 2: Reporte de Ventas por Mesero (NUEVO)

### ❌ ANTES
```
No existe
```

### ✅ DESPUÉS

#### BACKEND - Cambios
```java
// 1. DTO NUEVO
ARCHIVO: pos-backend/src/main/java/com/restaurant/pos/reporte/ReporteMeseroDTO.java
CONTENIDO:
├─ userId: UUID
├─ nombreMesero: String
├─ totalVentas: BigDecimal
├─ numeroCuentas: Long
├─ ticketPromedio: BigDecimal
├─ propinaTotalRecibida: BigDecimal
├─ propinaPromedio: BigDecimal
├─ tiempoPromedioMinutos: Double
├─ tasaCancelacion: Double (0.0-1.0)
└─ eficiencia: Double (0-100)

// 2. QUERY NUEVA
ARCHIVO: ReporteService.java
MÉTODO: getReporteMeserosPorPeriodo(LocalDateTime desde, hasta, String turno)
├─ SQL: JOIN ItemPedido ip
├─ SQL: JOIN Pedido p ON ip.pedido_id = p.id
├─ SQL: JOIN Cuenta c ON p.cuenta_id = c.id
├─ SQL: JOIN Usuario u ON c.mesero_id = u.id  [NUEVO CAMPO: mesero_id en Cuenta?]
├─ WHERE: c.cerradaEn BETWEEN desde AND hasta
├─ WHERE: c.estado = 'CERRADA'
├─ GROUP BY: u.id, u.nombre
├─ SELECT: SUM(c.total), COUNT(c.id), AVG(c.total), ...
└─ ORDEN: ORDER BY SUM(c.total) DESC

// 3. QUERY NUEVA (Detalle Individual)
ARCHIVO: ReporteService.java
MÉTODO: getReporteMeseroDetalle(UUID userId, LocalDateTime desde, hasta, String turno)
├─ Similar a anterior pero para UN usuario
└─ RETORNA: ReporteMeseroDTO

// 4. ENDPOINTS NUEVOS
ARCHIVO: ReporteResource.java
├─ ENDPOINT: GET /api/v1/reportes/ventas/meseros?desde=&hasta=&turno=
│  ├─ PARAMS: desde, hasta, turno (opcional)
│  ├─ RESPONSE: List<ReporteMeseroDTO>
│  └─ ROLES: ADMIN
│
└─ ENDPOINT: GET /api/v1/reportes/ventas/meseros/{userId}?desde=&hasta=&turno=
   ├─ PARAMS: userId (path), desde, hasta, turno
   ├─ RESPONSE: ReporteMeseroDTO
   └─ ROLES: ADMIN, GERENTE, o el propio mesero

// 5. VALIDACIÓN (Backend)
ARCHIVO: ReporteService.java
├─ Verificar Usuario existe
├─ Verificar fechas válidas (desde < hasta)
└─ Verificar turno válido (enum)
```

#### FRONTEND - Cambios
```typescript
// 1. MODELO NUEVO
ARCHIVO: models/reporte-mesero.model.ts
├─ export interface ReporteMesero { ...todos los campos del DTO }
└─ CARPETA: src/app/features/admin/reportes/models/

// 2. COMPONENTE NUEVO
ARCHIVO: ventas-mesero.component.ts
├─ RUTA: /admin/reportes/ventas/meseros
├─ TEMPLATE:
│  ├─ <app-filtro-reporte> ← Reutilizable
│  ├─ TABLA:
│  │  ├─ COLUMNAS: Mesero | Cuentas | Ventas | Ticket | Propina | Eficiencia | Devoluciones
│  │  ├─ FILAS: *ngFor="let mesero of meseros"
│  │  ├─ CLICK: (click)="abrirDetalle(mesero.userId)"
│  │  └─ SORTING: (click)="ordenarPor('ventas')" ← clickeable
│  │
│  ├─ COLORES (condicionales):
│  │  ├─ Eficiencia > 90% → Verde
│  │  ├─ Eficiencia 70-90% → Amarillo
│  │  └─ Eficiencia < 70% → Rojo
│  │
│  └─ BOTONES:
│     ├─ [Exportar CSV]
│     └─ [Cargar Más]
│
├─ LÓGICA:
│  ├─ ngOnInit(): cargar meseros inicial (últimos 30 días)
│  ├─ onFiltroChange(filtro): recargar con nuevo período
│  ├─ abrirDetalle(userId): navegar a /detalle/{userId}
│  ├─ ordenarPor(campo): reordenar tabla local
│  └─ exportar(): llamar a servicio descargar CSV
│
└─ VARIABLES:
   ├─ meseros: ReporteMesero[]
   ├─ cargando: boolean
   ├─ ordenarPor: string
   └─ ordenAscendente: boolean

// 3. COMPONENTE NUEVO (Detalle)
ARCHIVO: ventas-mesero-detalle.component.ts
├─ RUTA: /admin/reportes/ventas/meseros/{userId}
├─ TEMPLATE:
│  ├─ HEADER: Nombre del mesero + período
│  ├─ TARJETAS: 6 KPIs (Ventas, Cuentas, Ticket, Propinas, Eficiencia, Devoluciones)
│  ├─ GRÁFICO LÍNEA: Ventas diarias durante período
│  ├─ GRÁFICO COMPARACIÓN: Este mesero vs. promedio equipo
│  └─ BOTÓN: [Volver a Listado]
│
└─ LÓGICA:
   ├─ ngOnInit(): leer userId de route params
   ├─ cargarDetalle(userId): llamar a servicio
   ├─ renderGraficoVentas(): Chart.js
   └─ calcularComparativa(): comparar con promedio

// 4. SERVICIO ACTUALIZADO
ARCHIVO: reporte.service.ts
├─ MÉTODO NUEVO: getReporteMeseros(filtro: PeriodoFiltro): Observable<ReporteMesero[]>
├─ MÉTODO NUEVO: getReporteMeseroDetalle(userId: string, filtro: PeriodoFiltro): Observable<ReporteMesero>
└─ MÉTODO ACTUALIZADO: exportarReporte(tipo: 'meseros', filtro)

// 5. ROUTING NUEVO
ARCHIVO: app-routing.module.ts
├─ RUTA: /admin/reportes/ventas/meseros → ventas-mesero.component
└─ RUTA: /admin/reportes/ventas/meseros/:userId → ventas-mesero-detalle.component

// 6. COMPONENTE COMPARTIDO USADO
ARCHIVO: compartido/filtro-reporte.component.ts (EXISTENTE de Fase 1)
└─ Selector período, turno, mesero
```

#### VERIFICACIÓN
```bash
# Backend - Swagger
GET /api/v1/reportes/ventas/meseros?desde=2024-01-01&hasta=2024-01-31&turno=ALMUERZO
Response: [ { userId: "...", nombreMesero: "Ana", totalVentas: 1800, ... }, ... ]

GET /api/v1/reportes/ventas/meseros/550e8400-e29b-41d4-a716-446655440000
Response: { userId: "...", nombreMesero: "Ana", totalVentas: 1800, ... }

# Frontend - http://localhost:4200/admin/reportes/ventas/meseros
- Tabla carga con meseros
- Click en fila → navega a /meseros/{userId}
- Detalle muestra 6 tarjetas y gráfico
- Ordenar por columna reordena tabla
```

---

## 📌 FUNCIONALIDAD 3: Reporte de Rentabilidad (NUEVO)

### ❌ ANTES
```
Solo Top 10 productos, sin análisis de costo/margen
```

### ✅ DESPUÉS

#### BACKEND - Cambios
```java
// 1. DTO NUEVO
ARCHIVO: ReporteProductoRentabilidadDTO.java
├─ productoId: UUID
├─ nombre: String
├─ cantidad: Long
├─ ingresoTotal: BigDecimal
├─ costoTotal: BigDecimal
├─ margenBruto: BigDecimal ($)
├─ margenPorcentaje: BigDecimal (%)
├─ roi: BigDecimal (veces)
├─ categoria: String
└─ estacion: String

// 2. VERIFICACIÓN: Campos en BD
ARCHIVO: DB Schema (Flyway migration si falta)
├─ TABLA: Producto
│  ├─ ✓ precioVenta (ya existe?)
│  └─ ⚠️ costUnitario (¿AGREGAR si falta?)
│
└─ SI FALTA: Crear migration
   ├─ ALTER TABLE producto ADD COLUMN costo_unitario DECIMAL(10,2);
   └─ Data fix: UPDATE producto SET costo_unitario = precio_venta * 0.3; (ej: 30% margen base)

// 3. QUERY NUEVA
ARCHIVO: ReporteService.java
MÉTODO: getReporteRentabilidad(LocalDateTime desde, hasta, String categoria, String estacion)
├─ SQL: SELECT p.nombre, 
│         SUM(ip.cantidad) as cantidad,
│         SUM(ip.precioUnitario * ip.cantidad) as ingreso,
│         SUM(p.costUnitario * ip.cantidad) as costo,
│         ... calcular margen y ROI ...
├─ SQL: FROM ItemPedido ip
├─ SQL: JOIN Producto p
├─ SQL: JOIN Pedido ped
├─ SQL: JOIN Cuenta c WHERE c.cerradaEn BETWEEN desde AND hasta
├─ WHERE: c.estado = 'CERRADA'
├─ WHERE: (categoria si se pasa) y (estacion si se pasa)
├─ GROUP BY: p.id, p.nombre, p.categoria, p.estacion
└─ ORDER BY: (ingreso - costo) DESC

// 4. ENDPOINT NUEVO
ARCHIVO: ReporteResource.java
├─ ENDPOINT: GET /api/v1/reportes/productos/rentabilidad
│  ├─ PARAMS: desde, hasta, categoria?, estacion?
│  ├─ RESPONSE: List<ReporteProductoRentabilidadDTO>
│  └─ ORDEN: Por margen DESC
│
└─ VALIDACIÓN:
   ├─ Fechas válidas
   └─ Categoría/Estación en lista válida
```

#### FRONTEND - Cambios
```typescript
// 1. MODELO NUEVO
ARCHIVO: models/reporte-producto-rentabilidad.model.ts
└─ export interface ReporteProductoRentabilidad { ... }

// 2. COMPONENTE NUEVO
ARCHIVO: productos-rentabilidad.component.ts
├─ RUTA: /admin/reportes/productos/rentabilidad
├─ TEMPLATE:
│  ├─ <app-filtro-reporte> (con selector Categoría, Estación)
│  │
│  ├─ TABLA:
│  │  ├─ COLUMNAS (clickeable para sort): 
│  │  │  ├─ Producto | Qty | Ingresos | Costo | Margen$ | Margen% | ROI
│  │  │  
│  │  ├─ FILAS: *ngFor="let prod of productos"
│  │  │  └─ Color ROW según margen%:
│  │  │     ├─ > 75% → Verde (#27ae60)
│  │  │     ├─ 50-75% → Amarillo (#f39c12)
│  │  │     └─ < 50% → Rojo (#e74c3c)
│  │  │
│  │  ├─ CLICK ROW: abrirDetalle(productoId)
│  │  └─ PAGINACIÓN: 20 por página
│  │
│  ├─ GRÁFICO: Barras horizontales
│  │  ├─ TOP 10 productos por margen$
│  │  ├─ EJE X: Margen ($)
│  │  └─ COLORES: verde/amarillo/rojo por margen%
│  │
│  └─ RESUMEN:
│     ├─ Margen Promedio: X%
│     ├─ Total Ingresos: $X
│     ├─ Total Costos: $X
│     └─ Margen Total: $X
│
├─ LÓGICA:
│  ├─ ngOnInit(): cargar productos initial
│  ├─ onFiltroChange(filtro): recargar
│  ├─ ordenarPor(columna): reorder tabla
│  ├─ abrirDetalle(productoId): navegar
│  ├─ getColorMargen(pct): string → color basado %
│  └─ exportarExcel(): descargar XLSX
│
└─ VARIABLES:
   ├─ productos: ReporteProductoRentabilidad[]
   ├─ ordenadoPor: string
   ├─ ordenAscendente: boolean
   └─ resumen: { margenPromedio, totalIngresos, ... }

// 3. COMPONENTE NUEVO (Detalle)
ARCHIVO: productos-rentabilidad-detalle.component.ts
├─ RUTA: /admin/reportes/productos/rentabilidad/:productoId
├─ TEMPLATE:
│  ├─ TARJETA PRODUCTO:
│  │  ├─ Nombre, Categoría, Estación
│  │  └─ Imagen (si existe)
│  │
│  ├─ TARJETAS KPI:
│  │  ├─ Cantidad vendida
│  │  ├─ Ingreso Total
│  │  ├─ Costo Total
│  │  ├─ Margen ($)
│  │  ├─ Margen (%)
│  │  └─ ROI
│  │
│  ├─ GRÁFICO LÍNEA: 
│  │  ├─ Evolución margen$ diario durante período
│  │  ├─ Permitir comparar contra otro producto
│  │  └─ Mostrar promedio de categoría
│  │
│  └─ TABLA: Detalle diario
│     ├─ Fecha | Cantidad | Ingresos | Costo | Margen
│     └─ Permite expandir cada fila
│
└─ LÓGICA:
   ├─ ngOnInit(): leer productId de route, cargar
   ├─ renderGraficoEvolucion()
   └─ compararConOtro(productoId2)

// 4. SERVICIO ACTUALIZADO
ARCHIVO: reporte.service.ts
├─ MÉTODO NUEVO: getReporteRentabilidad(filtro: PeriodoFiltro, categoria?, estacion?): Observable<ReporteProductoRentabilidad[]>
├─ MÉTODO NUEVO: getReporteRentabilidadDetalle(productoId: string, filtro): Observable<ReporteProductoRentabilidad>
└─ MÉTODO NUEVO: compararProductosRentabilidad(productoId1, productoId2, filtro): Observable<ComparativaDTO>

// 5. ROUTING NUEVO
ARCHIVO: app-routing.module.ts
├─ RUTA: /admin/reportes/productos/rentabilidad → productos-rentabilidad.component
└─ RUTA: /admin/reportes/productos/rentabilidad/:productoId → productos-rentabilidad-detalle.component

// 6. ESTILOS NUEVO
ARCHIVO: productos-rentabilidad.component.scss
├─ CLASES: .margen-alto { background: #d5f4e6; }
├─ CLASES: .margen-medio { background: #fef9e7; }
└─ CLASES: .margen-bajo { background: #fadbd8; }
```

#### VERIFICACIÓN
```bash
# Backend - Swagger
GET /api/v1/reportes/productos/rentabilidad?desde=2024-01-01&hasta=2024-01-31
Response: [ 
  { productoId: "...", nombre: "Costilla", cantidad: 12, ingresoTotal: 240, costoTotal: 60, margenBruto: 180, margenPorcentaje: 75, roi: 4.0, ... },
  ... 
]

# Frontend - http://localhost:4200/admin/reportes/productos/rentabilidad
- Tabla carga datos
- Filas coloreadas según margen%
- Click en fila → abre detalle
- Gráfico renderiza TOP 10
- Ordenar por columna funciona
```

---

## 📌 FUNCIONALIDAD 4: Análisis Horas Pico (NUEVO)

### ❌ ANTES
```
No existe análisis temporal
```

### ✅ DESPUÉS

#### BACKEND - Cambios
```java
// 1. DTO NUEVO
ARCHIVO: ReporteHoraPicoDTO.java
├─ hora: Integer (0-23)
├─ ventasTotal: BigDecimal
├─ cuentasTotal: Long
├─ tiempoPromedioPedidoSegundos: Double
├─ articulosVendidos: Long
├─ estacionMasCongestinada: String
└─ utilidadBruta: BigDecimal

// 2. QUERY NUEVA
ARCHIVO: ReporteService.java
MÉTODO: getReporteHorasPico(LocalDateTime desde, hasta, String turno)
├─ SQL: SELECT EXTRACT(HOUR FROM c.cerradaEn) as hora,
│         SUM(c.total) as ventas,
│         COUNT(DISTINCT c.id) as cuentas,
│         AVG(FUNCTION('EXTRACT', 'EPOCH' FROM (ip.listoEn - ip.createdAt))) as timeAvg,
│         SUM(ip.cantidad) as articulos,
│         ... calcular estacion mas congestionada ...
├─ SQL: FROM Cuenta c
├─ SQL: LEFT JOIN Pedido p ON c.id = p.cuenta_id
├─ SQL: LEFT JOIN ItemPedido ip ON p.id = ip.pedido_id
├─ WHERE: c.cerradaEn BETWEEN desde AND hasta AND c.estado = 'CERRADA'
├─ GROUP BY: EXTRACT(HOUR FROM c.cerradaEn)
└─ ORDER BY: hora ASC

// 3. ENDPOINT NUEVO
ARCHIVO: ReporteResource.java
├─ ENDPOINT: GET /api/v1/reportes/operacion/horas-pico
│  ├─ PARAMS: desde, hasta, turno?
│  ├─ RESPONSE: List<ReporteHoraPicoDTO> (24 registros máximo)
│  └─ ORDEN: Por hora (0-23)
│
└─ VALIDACIÓN:
   ├─ Fechas válidas
   └─ Turno válido si se pasa
```

#### FRONTEND - Cambios
```typescript
// 1. MODELO NUEVO
ARCHIVO: models/reporte-hora-pico.model.ts
└─ export interface ReporteHoraPico { ... }

// 2. COMPONENTE NUEVO
ARCHIVO: horas-pico.component.ts
├─ RUTA: /admin/reportes/operacion/horas-pico
├─ TEMPLATE:
│  ├─ <app-filtro-reporte> (con selector Turno)
│  │
│  ├─ DOS GRÁFICOS LADO A LADO:
│  │  ├─ GRÁFICO LÍNEA (Ingresos por hora):
│  │  │  ├─ EJE X: Hora (0-23)
│  │  │  ├─ EJE Y: Ventas ($)
│  │  │  ├─ HIGHLIGHT: Punto máximo en ROJO
│  │  │  └─ TOOLTIP: Mostrar hora exacta, valor exacto
│  │  │
│  │  └─ GRÁFICO BARRAS (Cuentas por hora):
│  │     ├─ EJE X: Hora (0-23)
│  │     ├─ EJE Y: # de cuentas
│  │     └─ COLOR: Según intensidad (gradient)
│  │
│  ├─ TABLA DETALLE:
│  │  ├─ COLUMNAS: Hora | Ventas | Cuentas | Ticket Prom | Tiempo Prom | Artículos
│  │  ├─ FILAS: *ngFor="let hora of horas"
│  │  └─ HIGHLIGHT: Fila hora-pico en amarillo
│  │
│  ├─ INDICADORES:
│  │  ├─ 📊 Hora Pico: 13:00 (máximo)
│  │  ├─ 📉 Hora Baja: 15:00 (mínimo)
│  │  ├─ 📈 Promedio: $350
│  │  └─ 📊 Rango: $100 - $600
│  │
│  └─ ACCIONES:
│     ├─ [Expandir Hora Pico] → detalle
│     └─ [Exportar CSV]
│
├─ LÓGICA:
│  ├─ ngOnInit(): cargar horas-pico
│  ├─ onFiltroChange(filtro): recargar
│  ├─ renderGraficoVentas(): Chart.js (línea)
│  ├─ renderGraficoCuentas(): Chart.js (barras)
│  ├─ calcularHoraPico(): MAX en array
│  ├─ calcularHoraBaja(): MIN en array
│  └─ expandirHora(hora): navegar a detalle
│
└─ VARIABLES:
   ├─ horas: ReporteHoraPico[]
   ├─ horaPico: ReporteHoraPico
   ├─ horaBaja: ReporteHoraPico
   ├─ promedioVentas: number
   └─ chartVentas: Chart, chartCuentas: Chart

// 3. COMPONENTE SUB-DETALLE (si es necesario)
ARCHIVO: horas-pico-detalle.component.ts (opcional)
├─ RUTA: /admin/reportes/operacion/horas-pico/:hora
├─ MUESTRA:
│  ├─ Qué pasó en esa hora específica
│  ├─ Productos más vendidos
│  ├─ Meseros más activos
│  └─ Estación con más congestión
│
└─ Permite volver a grafico principal

// 4. SERVICIO ACTUALIZADO
ARCHIVO: reporte.service.ts
└─ MÉTODO NUEVO: getReporteHorasPico(filtro: PeriodoFiltro, turno?): Observable<ReporteHoraPico[]>

// 5. ROUTING NUEVO
ARCHIVO: app-routing.module.ts
├─ RUTA: /admin/reportes/operacion/horas-pico → horas-pico.component
└─ RUTA: /admin/reportes/operacion/horas-pico/:hora → horas-pico-detalle.component (opcional)
```

#### VERIFICACIÓN
```bash
# Backend - Swagger
GET /api/v1/reportes/operacion/horas-pico?desde=2024-01-01&hasta=2024-01-31&turno=ALMUERZO
Response: [
  { hora: 12, ventasTotal: 500, cuentasTotal: 20, tiempoPromedioPedidoSegundos: 240, ... },
  { hora: 13, ventasTotal: 680, cuentasTotal: 30, tiempoPromedioPedidoSegundos: 245, ... },
  ...
]

# Frontend - http://localhost:4200/admin/reportes/operacion/horas-pico
- 2 gráficos renderizados correctamente
- Tabla muestra 24 filas (0-23)
- Hora pico identificada y destacada
- Tooltip en gráficos funciona
```

---

## 📌 FUNCIONALIDAD 5: Desempeño de Estaciones (NUEVO)

### ❌ ANTES
```
Solo promedio general (tiempoPromedioCocina, tiempoPromedioBarra)
```

### ✅ DESPUÉS

#### BACKEND - Cambios
```java
// 1. DTO NUEVO
ARCHIVO: ReporteEstacionDTO.java
├─ estacion: String (COCINA / BARRA)
├─ tiempoPromedio: Double (segundos)
├─ tiempoMinimo: Double
├─ tiempoMaximo: Double
├─ totalPedidos: Long
├─ productoMasLento: String
├─ tiempoMasLentoSegundos: Double
├─ productoMasRapido: String
├─ tiempoMasRapidoSegundos: Double
├─ productosDetalle: List<ProductoTiempoDTO>
└─ cuelloBotellaIndicador: Boolean

// 2. DTO AUXILIAR
ARCHIVO: ProductoTiempoDTO.java
├─ nombre: String
├─ cantidad: Long
├─ tiempoPromedio: Double (segundos)
├─ tiempoMin: Double
├─ tiempoMax: Double
└─ tiempoTotal: Double (suma tiempos)

// 3. QUERIES NUEVAS
ARCHIVO: ReporteService.java
MÉTODO: getReporteEstaciones(String estacion, LocalDateTime desde, hasta)
├─ SQL: SELECT p.estacion,
│         AVG(FUNCTION('EXTRACT', 'EPOCH' FROM (ip.listoEn - ip.createdAt))) as tiempoPromedio,
│         MIN(...), MAX(...), COUNT(ip.id),
│         p.nombre (para más lento), ...
├─ SQL: FROM ItemPedido ip
├─ SQL: JOIN Producto p WHERE p.estacion = :estacion
├─ WHERE: ip.listoEn IS NOT NULL AND ip.createdAt BETWEEN desde AND hasta
├─ GROUP BY: p.nombre (para obtener detalle por producto)
└─ ORDER BY: tiempoPromedio DESC

MÉTODO: getProductosTiempoEstacion(String estacion, LocalDateTime desde, hasta)
└─ Retorna lista de ProductoTiempoDTO ordenada por tiempo

// 4. ENDPOINTS NUEVOS
ARCHIVO: ReporteResource.java
├─ ENDPOINT: GET /api/v1/reportes/operacion/estaciones/{estacion}
│  ├─ PATH PARAM: estacion = COCINA|BARRA
│  ├─ QUERY PARAMS: desde, hasta
│  ├─ RESPONSE: ReporteEstacionDTO
│  └─ Status 400 si estación inválida
│
└─ VALIDACIÓN:
   ├─ Estación válida (enum)
   └─ Fechas válidas
```

#### FRONTEND - Cambios
```typescript
// 1. MODELOS NUEVOS
ARCHIVO: models/reporte-estacion.model.ts
├─ export interface ReporteEstacion { ... }
└─ export interface ProductoTiempo { ... }

// 2. COMPONENTE NUEVO
ARCHIVO: estaciones.component.ts
├─ RUTA: /admin/reportes/operacion/estaciones
├─ LAYOUT: DOS COLUMNAS (Cocina | Barra)
│
├─ CADA COLUMNA:
│  ├─ HEADER: Título + período
│  │
│  ├─ TARJETAS KPI (2x3 grid):
│  │  ├─ Tiempo Promedio
│  │  ├─ Tiempo Mínimo
│  │  ├─ Tiempo Máximo
│  │  ├─ Total Pedidos
│  │  ├─ Producto Más Lento
│  │  └─ Producto Más Rápido
│  │
│  ├─ TABLA: Productos ordenados por tiempo
│  │  ├─ COLUMNAS: Producto | Qty | Tiempo Prom | Min | Max | Total
│  │  └─ FILAS: *ngFor="let prod of estacion.productosDetalle"
│  │
│  └─ GRÁFICO BARRAS HORIZONTAL:
│     ├─ TOP 10 productos
│     ├─ EJE X: Tiempo (segundos)
│     ├─ EJE Y: Nombre producto
│     └─ COLORES: Verde (rápido) → Rojo (lento)
│
├─ INDICADOR GLOBAL:
│  ├─ SI tiempoPromedioCocina > 2 * tiempoPromedioBarra:
│  │  └─ ⚠️ CUELLO DE BOTELLA: Cocina es 2x más lenta
│  │
│  └─ SI tiempoPromedioBarra > 2 * tiempoPromedioCocina:
│     └─ ⚠️ CUELLO DE BOTELLA: Barra es 2x más lenta
│
├─ <app-filtro-reporte> (período)
│
└─ ACCIONES:
   ├─ [Comparar Estaciones] → gráfico comparativo
   └─ [Exportar Análisis]

├─ LÓGICA:
│  ├─ ngOnInit(): cargar ambas estaciones en paralelo
│  ├─ onFiltroChange(filtro): recargar
│  ├─ cargarEstacion(estacion: string): Observable
│  ├─ renderProductosPorTiempo(estacion): Chart.js (barras)
│  ├─ identificarCuelloDeBottella(): calcular ratio
│  ├─ compararEstaciones(): mostrar lado a lado
│  └─ exportarAnalisis(): PDF/CSV
│
└─ VARIABLES:
   ├─ cocina: ReporteEstacion
   ├─ barra: ReporteEstacion
   ├─ cuelloDeBottella: string (COCINA|BARRA|NINGUNO)
   └─ cargando: boolean

// 3. COMPONENTE COMPARATIVO (sub-componente)
ARCHIVO: estaciones-comparativa.component.ts (reutilizable)
├─ INPUT: @Input() estacion1: ReporteEstacion
├─ INPUT: @Input() estacion2: ReporteEstacion
├─ MUESTRA:
│  ├─ Gráfico de línea: Tiempo promedio comparativa
│  ├─ Tabla: Lado a lado
│  └─ Diferencias porcentuales
│
└─ PERMITE:
   └─ Identificar qué producto es más lento en cada estación

// 4. SERVICIO ACTUALIZADO
ARCHIVO: reporte.service.ts
└─ MÉTODO NUEVO: getReporteEstaciones(estacion: string, filtro: PeriodoFiltro): Observable<ReporteEstacion>

// 5. ROUTING NUEVO
ARCHIVO: app-routing.module.ts
└─ RUTA: /admin/reportes/operacion/estaciones → estaciones.component
```

#### VERIFICACIÓN
```bash
# Backend - Swagger
GET /api/v1/reportes/operacion/estaciones/COCINA?desde=2024-01-01&hasta=2024-01-31
Response: {
  estacion: "COCINA",
  tiempoPromedio: 252.5,
  tiempoMinimo: 45.0,
  tiempoMaximo: 580.0,
  totalPedidos: 145,
  productoMasLento: "Paella",
  tiempoMasLentoSegundos: 580.0,
  productosDetalle: [ ... ],
  cuelloBotellaIndicador: true
}

# Frontend - http://localhost:4200/admin/reportes/operacion/estaciones
- Dos columnas visible
- Tarjetas KPI cargan datos
- Tabla productos ordenada por tiempo
- Gráfico renderiza
- Indicador de cuello de botella visible
```

---

## 📌 FUNCIONALIDAD 6: Dashboard Gerencial (NUEVO - Consolidado)

### ❌ ANTES
```
No existe
```

### ✅ DESPUÉS

#### BACKEND - Cambios
```java
// 1. DTO NUEVO (Consolidado)
ARCHIVO: DashboardReportesDTO.java
├─ SECTION 1: KPIs Diarios
│  ├─ vendidoHoy: BigDecimal
│  ├─ cuentasHoy: Long
│  ├─ ticketPromedioHoy: BigDecimal
│  └─ propinaTotalHoy: BigDecimal
│
├─ SECTION 2: Performance
│  ├─ tiempoPromedioCocina: Double (segundos)
│  ├─ tiempoPromedioBarra: Double (segundos)
│  ├─ eficienciaPromedio: Double (%)
│  └─ cuelloBotellaEstacion: String
│
├─ SECTION 3: Top Datos
│  ├─ meseroTopId: UUID
│  ├─ meseroTopNombre: String
│  ├─ meseroTopVentas: BigDecimal
│  ├─ productoTopNombre: String
│  ├─ productoTopVentas: Long
│  └─ horaMaximaVentas: Integer
│
├─ SECTION 4: Alertas
│  ├─ alertasInventario: List<AlertaInventarioDTO>
│  ├─ alertasOperacion: List<AlertaOperacionDTO>
│  └─ alertasVentas: List<AlertaVentasDTO>
│
└─ SECTION 5: Período
   └─ periodo: PeriodoFiltroDTO

// 2. DTOs Auxiliares (Alertas)
ARCHIVO: AlertaInventarioDTO.java
├─ itemId: UUID
├─ nombre: String
├─ stockActual: BigDecimal
├─ nivelMinimo: BigDecimal
├─ urgencia: String (CRITICA, ALTA, MEDIA)
└─ accion: String ("Reordenar ahora")

ARCHIVO: AlertaOperacionDTO.java
├─ tipo: String (CUELLO_BOTELLA, BAJA_EFICIENCIA)
├─ descripcion: String
└─ recomendacion: String

ARCHIVO: AlertaVentasDTO.java
├─ tipo: String (BAJO_DESEMPEÑO, ALTA_TASA_CANCELACION)
├─ meseroId: UUID
├─ meseroNombre: String
└─ recomendacion: String

// 3. QUERY NUEVA (Consolidada)
ARCHIVO: ReporteService.java
MÉTODO: getDashboardReportes(LocalDateTime desde, hasta)
├─ Llamar internamente a:
│  ├─ getReporteVentas(desde, hasta) → KPIs diarios
│  ├─ getReporteOperacion(desde, hasta) → tiempos
│  ├─ getReporteMeserosPorPeriodo(desde, hasta) → top mesero
│  ├─ getReporteProductos(desde, hasta) → top producto
│  ├─ getReporteHorasPico(desde, hasta) → hora máxima
│  └─ getAlertasInventario() → alertas activas
│
├─ Consolidar todo en DashboardReportesDTO
├─ Calcular alertas y recomendaciones
└─ RETORNA: DashboardReportesDTO completo

// 4. ENDPOINT NUEVO
ARCHIVO: ReporteResource.java
├─ ENDPOINT: GET /api/v1/reportes/dashboard
│  ├─ QUERY PARAMS: desde, hasta (opcional, default: hoy)
│  ├─ RESPONSE: DashboardReportesDTO
│  └─ Cache: 5 minutos (reducir carga BD)
│
└─ VALIDACIÓN:
   └─ Fechas válidas
```

#### FRONTEND - Cambios
```typescript
// 1. MODELOS NUEVOS
ARCHIVO: models/dashboard-reportes.model.ts
├─ export interface DashboardReportes { ... }
├─ export interface AlertaInventario { ... }
├─ export interface AlertaOperacion { ... }
└─ export interface AlertaVentas { ... }

// 2. COMPONENTE NUEVO (Principal)
ARCHIVO: dashboard-reportes.component.ts
├─ RUTA: /admin/reportes/dashboard (DEFAULT AL ENTRAR A REPORTES)
├─ TEMPLATE:
│  ├─ HEADER:
│  │  ├─ Título "Dashboard Gerencial"
│  │  ├─ Selector período: [Hoy] [Semana] [Mes] [Custom]
│  │  └─ Último update: hace X segundos
│  │
│  ├─ SECTION 1: KPIs Principales (6 tarjetas)
│  │  ├─ 📊 Ventas Hoy: $5,200
│  │  ├─ 📝 Cuentas Hoy: 24
│  │  ├─ 💰 Ticket Promedio: $216.67
│  │  ├─ 💵 Propinas Hoy: $125
│  │  ├─ 👨 Mesero Top: Ana ($1,800)
│  │  └─ 📦 Producto Top: Costilla (45 unid)
│  │
│  │ CADA TARJETA:
│  │ ├─ Icono + Valor grande
│  │ ├─ Comparación con período anterior ▲/▼ %
│  │ └─ Click → navega a detalle
│  │
│  ├─ SECTION 2: Performance de Operación (2x2 grid)
│  │  ├─ ⏱️ Cocina: 4.2 min avg
│  │  ├─ ⏱️ Barra: 2.1 min avg
│  │  ├─ ⚡ Eficiencia Gral: 92%
│  │  └─ ⚠️ Cuello Botella: Cocina (2x más lenta)
│  │
│  │ CADA ITEM:
│  │ └─ Click → abre detalle estación
│  │
│  ├─ SECTION 3: Alertas y Recomendaciones
│  │  ├─ 🔴 CRÍTICAS:
│  │  │  ├─ Inventario bajo: Carne (4kg, 5 días)
│  │  │  └─ Alta cancelación: Carlos (8% cancelaciones)
│  │  │
│  │  ├─ 🟡 ADVERTENCIAS:
│  │  │  ├─ Barra muy congestionada (80% capacidad)
│  │  │  └─ Margen bajo en Pasta (40%)
│  │  │
│  │  └─ [Descartar] [Ver Detalles] botones
│  │
│  ├─ SECTION 4: Mini Gráficos (2x2 grid)
│  │  ├─ Línea: Ventas últimas 24h
│  │  ├─ Barras: Cuentas por turno
│  │  ├─ Pie: Método pago
│  │  └─ Barras: Top 5 productos
│  │
│  ├─ SECTION 5: Acciones Rápidas
│  │  ├─ [Ver Detalle Meseros]
│  │  ├─ [Ver Rentabilidad Productos]
│  │  ├─ [Revisar Inventario]
│  │  └─ [Generar Reporte Completo]
│  │
│  └─ FOOTER:
│     ├─ Last refresh: 2024-01-15 14:32:00
│     └─ [🔄 Actualizar Ahora]
│
├─ LÓGICA:
│  ├─ ngOnInit(): cargar dashboard
│  ├─ onPeriodoChange(periodo): recargar
│  ├─ irADetalle(seccion): navegar
│  ├─ calcularComparativa(): comparar vs período anterior
│  ├─ renderTarjetas(): 6 tarjetas con colores
│  ├─ renderMiniGraficos(): Chart.js (4 charts)
│  ├─ mostrarAlertas(): filtrar por urgencia
│  ├─ descartar Alerta(id): marcar como leída
│  └─ auto-refresh cada 5 minutos
│
└─ VARIABLES:
   ├─ dashboard: DashboardReportes
   ├─ periodo: 'hoy'|'semana'|'mes'|'custom'
   ├─ cargando: boolean
   ├─ alertasVisibles: Alert[]
   └─ chartConfigs: { [key: string]: ChartConfiguration }

// 3. COMPONENTE SUB-TARJETA (Reutilizable)
ARCHIVO: kpi-tarjeta.component.ts
├─ INPUT: @Input() titulo: string
├─ INPUT: @Input() valor: string | number
├─ INPUT: @Input() icono: string
├─ INPUT: @Input() comparativa: number (%)
├─ INPUT: @Input() color: string
├─ OUTPUT: @Output() onClick = new EventEmitter()
└─ EMITE: click para navegar a detalle

// 4. COMPONENTE SUB-ALERTA (Reutilizable)
ARCHIVO: alerta-reporte.component.ts
├─ INPUT: @Input() alerta: Alert
├─ INPUT: @Input() urgencia: 'critica'|'alta'|'media'
└─ EMITE: onDescartar, onVerDetalles

// 5. SERVICIO ACTUALIZADO
ARCHIVO: reporte.service.ts
├─ MÉTODO NUEVO: getDashboardReportes(filtro: PeriodoFiltro): Observable<DashboardReportes>
├─ MÉTODO NUEVO: compararPeriodos(periodoActual, periodoAnterior): Observable<Comparativa>
└─ SUBJECT: dashboardRefresh$ (para auto-refresh cada 5 min)

// 6. ROUTING NUEVO
ARCHIVO: app-routing.module.ts
├─ RUTA: /admin/reportes → dashboard-reportes.component (redirectTo)
└─ RUTA: /admin/reportes/dashboard → dashboard-reportes.component
```

#### VERIFICACIÓN
```bash
# Backend - Swagger
GET /api/v1/reportes/dashboard?desde=2024-01-15&hasta=2024-01-15
Response: {
  vendidoHoy: 5200,
  cuentasHoy: 24,
  ticketPromedioHoy: 216.67,
  tiempoPromedioCocina: 252.0,
  tiempoPromedioBarra: 126.0,
  meseroTopNombre: "Ana",
  meseroTopVentas: 1800,
  productoTopNombre: "Costilla",
  productoTopVentas: 45,
  horaMaximaVentas: 13,
  alertasInventario: [ ... ],
  alertasOperacion: [ ... ],
  alertasVentas: [ ... ]
}

# Frontend - http://localhost:4200/admin/reportes/dashboard
- 6 tarjetas KPI cargan datos correctos
- 2x2 grid con alertas visible
- Mini gráficos renderizados
- Botones de acción funcionales
- Auto-refresh cada 5 min
- Responsivo en móvil
```

---

## 📌 FUNCIONALIDAD 7: Proyecciones de Inventario (NUEVO)

### ❌ ANTES
```
Solo estado actual de inventario
```

### ✅ DESPUÉS

#### BACKEND - Cambios
```java
// 1. DTO NUEVO
ARCHIVO: ProyeccionInventarioDTO.java
├─ itemId: UUID
├─ nombre: String
├─ unidadMedida: String
├─ stockActual: BigDecimal
├─ consumoPromedioDia: BigDecimal
├─ diasParaAgotarse: Integer
├─ proximaFechaAgotamiento: LocalDate
├─ nivelMinimo: BigDecimal
├─ nivelOptimo: BigDecimal
├─ necesitaReorden: Boolean
└─ proyeccion7dias: List<ProyeccionDiaDTO>

// 2. DTO AUXILIAR
ARCHIVO: ProyeccionDiaDTO.java
├─ fecha: LocalDate
├─ stockProyectado: BigDecimal
├─ consumoEsedia: BigDecimal
├─ estado: String (ABUNDANTE, NORMAL, BAJO, CRITICO)
└─ recomendacion: String

// 3. QUERY NUEVA
ARCHIVO: ReporteService.java
MÉTODO: getProyeccionesInventario(Integer dias) // ej: 7
├─ Para cada ItemInventario:
│  ├─ Calcular consumoPromedioDia = (suma últimos 30 días) / 30
│  ├─ Proyectar hacia adelante N días
│  │  └─ stockProyectadoDia[i] = stockActual - (consumoPromedioDia * i)
│  ├─ Calcular cuándo se agota: cuando stock <= 0
│  ├─ Comparar vs nivelMinimo/nivelOptimo
│  └─ Generar recomendación
│
└─ RETORNA: List<ProyeccionInventarioDTO>

// 4. ENDPOINT NUEVO
ARCHIVO: ReporteResource.java
├─ ENDPOINT: GET /api/v1/reportes/inventario/proyecciones
│  ├─ QUERY PARAMS: dias=7 (default)
│  ├─ RESPONSE: List<ProyeccionInventarioDTO>
│  └─ Orden: Por "necesitaReorden" DESC (críticos primero)
│
└─ VALIDACIÓN:
   └─ dias entre 1-90
```

#### FRONTEND - Cambios
```typescript
// 1. MODELOS NUEVOS
ARCHIVO: models/proyeccion-inventario.model.ts
├─ export interface ProyeccionInventario { ... }
└─ export interface ProyeccionDia { ... }

// 2. COMPONENTE NUEVO
ARCHIVO: inventario-proyecciones.component.ts
├─ RUTA: /admin/reportes/inventario/proyecciones
├─ TEMPLATE:
│  ├─ SELECTOR: [7 días] [14 días] [30 días]
│  │
│  ├─ TABLA PRINCIPAL:
│  │  ├─ COLUMNAS: Item | Stock Actual | Consumo/día | Días Restantes | Acción
│  │  ├─ FILAS: *ngFor="let item of proyecciones"
│  │  │
│  │  ├─ COLORES FILA (según estado):
│  │  │  ├─ 🔴 CRÍTICO: Stock < 3 días + rojo
│  │  │  ├─ 🟡 BAJO: Stock 3-7 días + amarillo
│  │  │  ├─ 🟢 NORMAL: Stock > 7 días + verde
│  │  │  └─ ✅ ABUNDANTE: Stock > 15 días + verde claro
│  │  │
│  │  ├─ EXPAND ICON: Click para ver proyección 7 días
│  │  └─ ACCIONES:
│  │     ├─ [Reordenar] (si necesita)
│  │     └─ [Detalle]
│  │
│  ├─ FILA EXPANDIBLE (sub-tabla):
│  │  ├─ Mostrar: Fecha | Stock Proyectado | Consumo Ese Día
│  │  ├─ 7 filas (una por día)
│  │  ├─ Gráfico línea: evolución stock
│  │  └─ Marker: fecha de agotamiento
│  │
│  ├─ RESUMEN:
│  │  ├─ Items Críticos: 2
│  │  ├─ Items Bajos: 5
│  │  ├─ Items Normales: 18
│  │  └─ Total Items: 25
│  │
│  ├─ ACCIONES BULK:
│  │  ├─ [☑️ Seleccionar Críticos]
│  │  ├─ [✓ Generar Orden Compra]
│  │  └─ [📧 Enviar a Proveedor]
│  │
│  └─ GRÁFICO GLOBAL:
│     ├─ Línea: Stock promedio general (7 días)
│     └─ Area: Zona segura (> nivel mínimo)
│
├─ LÓGICA:
│  ├─ ngOnInit(): cargar proyecciones (7 días default)
│  ├─ onDiasChange(dias): recargar con nuevo período
│  ├─ expandirItem(itemId): toggle sub-tabla
│  ├─ renderProyeccion(itemId): Chart.js (línea)
│  ├─ seleccionarCriticos(): checkbox todos críticos
│  ├─ generarOrdenCompra(): crear documento
│  ├─ getColorEstado(estado): devuelve color CSS
│  └─ exportarProyecciones(): PDF/CSV
│
└─ VARIABLES:
   ├─ proyecciones: ProyeccionInventario[]
   ├─ dias: number (7|14|30)
   ├─ itemsExpandidos: Set<UUID>
   ├─ itemsSeleccionados: Set<UUID>
   └─ resumen: { criticos, bajos, normales, total }

// 3. COMPONENTE SUB-MODAL
ARCHIVO: ordenar-compra.component.ts
├─ MODAL: Al hacer click [Reordenar] o [Generar Orden]
├─ CAMPOS:
│  ├─ Items: mostrar seleccionados
│  ├─ Cantidad a reordenar: auto-calculada + editable
│  ├─ Proveedor: selectable
│  ├─ Fecha entrega estimada: selector
│  └─ Notas: textarea
│
├─ BOTONES:
│  ├─ [Cancelar]
│  ├─ [Guardar como Borrador]
│  └─ [Enviar a Proveedor]
│
└─ INTEGRACIÓN:
   └─ POST /api/v1/inventario/ordenes-compra (nuevo endpoint)

// 4. SERVICIO ACTUALIZADO
ARCHIVO: reporte.service.ts
├─ MÉTODO NUEVO: getProyeccionesInventario(dias: number): Observable<ProyeccionInventario[]>
├─ MÉTODO NUEVO: generarOrdenCompra(items: UUID[], cantidades: number[]): Observable<Orden>
└─ MÉTODO NUEVO: exportarProyecciones(formato: 'PDF'|'CSV'): Observable<Blob>

// 5. ROUTING NUEVO
ARCHIVO: app-routing.module.ts
└─ RUTA: /admin/reportes/inventario/proyecciones → inventario-proyecciones.component
```

#### VERIFICACIÓN
```bash
# Backend - Swagger
GET /api/v1/reportes/inventario/proyecciones?dias=7
Response: [
  {
    itemId: "...",
    nombre: "Carne",
    stockActual: 25,
    consumoPromedioDia: 3.5,
    diasParaAgotarse: 7,
    proximaFechaAgotamiento: "2024-01-22",
    necesitaReorden: true,
    proyeccion7dias: [
      { fecha: "2024-01-15", stockProyectado: 25, ... },
      { fecha: "2024-01-16", stockProyectado: 21.5, ... },
      ...
    ]
  },
  ...
]

# Frontend - http://localhost:4200/admin/reportes/inventario/proyecciones
- Tabla carga con proyecciones
- Filas coloreadas según estado
- Click expand muestra sub-tabla + gráfico
- Resumen cuenta items correctamente
- Botón [Generar Orden Compra] abre modal
```

---

## 📊 MATRIZ RESUMEN: Cambios por Funcionalidad

| # | Funcionalidad | Backend - DTOs | Backend - Queries | Backend - Endpoints | Frontend - Componentes | Frontend - Modelos | Frontend - Rutas |
|---|---|---|---|---|---|---|---|
| 1 | Ventas (Mejorado) | 1 UPDATE | 1 UPDATE | 1 UPDATE | 1 NEW + gráfico | 1 UPDATE | 1 UPDATE |
| 2 | Ventas Mesero | 1 NEW | 2 NEW | 2 NEW | 2 NEW | 1 NEW | 2 NEW |
| 3 | Rentabilidad | 1 NEW | 1 NEW | 1 NEW | 2 NEW + gráfico | 1 NEW | 2 NEW |
| 4 | Horas Pico | 1 NEW | 1 NEW | 1 NEW | 2 NEW + 2 gráficos | 1 NEW | 2 NEW |
| 5 | Estaciones | 1 NEW + 1 AUX | 2 NEW | 1 NEW | 2 NEW + gráfico | 2 NEW | 1 NEW |
| 6 | Dashboard | 1 NEW + 3 AUX | 1 NEW | 1 NEW | 2 NEW + 4 gráficos | 4 NEW | 1 NEW |
| 7 | Proyecciones | 1 NEW + 1 AUX | 1 NEW | 1 NEW | 2 NEW + gráfico | 2 NEW | 1 NEW |
| **TOTAL** | — | **9 NEW + 2 UPDATE** | **9 NEW + 1 UPDATE** | **8 NEW + 1 UPDATE** | **14 NEW** | **12 NEW** | **10 NEW + 1 UPDATE** |

---

## 🔗 Dependencias Entre Funcionalidades

```
FASE 1 (INFRAESTRUCTURA)
└─ PeriodoFiltroDTO, FilterReporteComponent, ReporteService base

├─── FASE 2 (VENTAS)
│    └─ ReporteVentasDTO (UPDATE), ReporteMeseroDTO (NEW)
│       → ventas-general.component, ventas-mesero.component
│
├─── FASE 3 (PRODUCTOS)
│    └─ ReporteProductoRentabilidadDTO (NEW)
│       → productos-rentabilidad.component
│       Nota: No depende de FASE 2
│
├─── FASE 4 (OPERACIÓN)
│    └─ ReporteHoraPicoDTO, ReporteEstacionDTO (NEW)
│       → horas-pico.component, estaciones.component
│       Nota: No depende de FASE 2-3
│
└─── FASE 5 (CONSOLIDADO)
     ├─ DashboardReportesDTO (NEW) - DEPENDE DE: FASE 2, 3, 4
     │  → dashboard-reportes.component
     │
     └─ ProyeccionInventarioDTO (NEW) - INDEPENDIENTE
        → inventario-proyecciones.component

🎯 Conclusión: Las fases 2, 3, 4 pueden desarrollarse EN PARALELO
              La fase 5 debe esperar a que terminen las anteriores
```


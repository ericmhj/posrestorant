# Requirements Document

> **Contexto de infraestructura**: Ver `infrastructure-context.md` para los requisitos del entorno DevContainer con Docker Compose (herramientas, extensiones IDE, puertos, volúmenes y documentación).

## Introduction

Este documento define los requerimientos funcionales del sistema POS (Punto de Venta) para restaurante. El sistema se compone de un backend Java con Quarkus y Apache Camel, una interfaz de usuario Angular, y una base de datos PostgreSQL. El entorno de desarrollo está descrito en `infrastructure-context.md`.

## Glossary

### Términos del Dominio de Negocio

- **POS_System**: Sistema de Punto de Venta; componente principal de la aplicación que gestiona mesas, pedidos y cobros.
- **Mesa**: Unidad física del restaurante identificada por número o nombre, que puede estar libre, ocupada o reservada.
- **Cuenta**: Registro de todos los pedidos asociados a una Mesa durante una sesión de servicio; permanece abierta hasta el cobro.
- **Pedido**: Conjunto de ítems solicitados por un cliente en una Mesa, asociado a una Cuenta y dirigido a una o más Estaciones.
- **Item_Pedido**: Línea individual dentro de un Pedido que referencia un Producto, una cantidad y modificadores opcionales.
- **Estacion**: Punto de preparación dentro del restaurante; puede ser Cocina (platillos) o Barra (bebidas).
- **KDS**: Kitchen Display System; interfaz de la Estacion de Cocina para recibir y gestionar pedidos de platillos.
- **BDS**: Bar Display System; interfaz de la Estacion de Barra para recibir y gestionar pedidos de bebidas.
- **Producto**: Ítem del menú con nombre, precio, categoría y Estacion de preparación asignada.
- **Categoria**: Clasificación de Productos (ej: entradas, platos fuertes, postres, bebidas alcohólicas, bebidas sin alcohol).
- **Inventario**: Registro del stock disponible de insumos o productos con cantidades y umbrales de alerta.
- **Movimiento_Inventario**: Registro de cada cambio en el stock: entrada, salida por venta, ajuste manual o merma.
- **Reserva_Inventario**: Cantidad de stock apartada temporalmente al registrar un Pedido, antes de confirmar el consumo.
- **Usuario**: Persona autenticada en el sistema con un rol asignado.
- **Rol**: Conjunto de permisos que define las acciones que un Usuario puede realizar; valores: Admin, Mesero, Cocina, Barra.
- **RBAC**: Control de acceso basado en roles (Role-Based Access Control).
- **Reporte**: Documento generado por el sistema que consolida datos operativos o financieros en un período de tiempo.
- **Turno**: Período de operación del restaurante con fecha/hora de apertura y cierre, usado para delimitar reportes.
- **WebSocket**: Protocolo de comunicación bidireccional en tiempo real usado para sincronizar estados entre POS, KDS y BDS.
- **API_Gateway**: Punto de entrada único que enruta las peticiones HTTP/WebSocket a los microservicios correspondientes.

---

## Requirements

### Requirement 10: Autenticación y Control de Acceso

**User Story:** Como administrador del restaurante, quiero que el sistema controle el acceso mediante usuarios y roles, para que cada persona solo pueda realizar las acciones correspondientes a su función.

#### Acceptance Criteria

1. WHEN un Usuario intenta acceder al sistema, THE POS_System SHALL requerir credenciales válidas (usuario y contraseña) antes de permitir el acceso.
2. THE POS_System SHALL autenticar a los Usuarios mediante Basic Auth con contraseñas almacenadas con hash seguro (bcrypt o equivalente).
3. THE POS_System SHALL asignar a cada Usuario exactamente un Rol de los valores permitidos: Admin, Mesero, Cocina o Barra.
4. WHILE un Usuario con Rol Mesero está autenticado, THE POS_System SHALL permitir únicamente las operaciones de gestión de mesas, registro de pedidos y cobro.
5. WHILE un Usuario con Rol Cocina está autenticado, THE KDS SHALL permitir únicamente la visualización y actualización de estado de pedidos de platillos asignados a la Estacion de Cocina.
6. WHILE un Usuario con Rol Barra está autenticado, THE BDS SHALL permitir únicamente la visualización y actualización de estado de pedidos de bebidas asignados a la Estacion de Barra.
7. WHILE un Usuario con Rol Admin está autenticado, THE POS_System SHALL permitir el acceso completo a todos los módulos: POS, KDS, BDS, Inventario, Productos, Usuarios y Reportes.
8. IF un Usuario intenta realizar una acción fuera de los permisos de su Rol, THEN THE POS_System SHALL rechazar la solicitud y retornar un error de autorización con código HTTP 403.
9. WHEN un Usuario cierra sesión, THE POS_System SHALL invalidar el token de sesión activo e impedir su reutilización.

---

### Requirement 11: Gestión de Mesas

**User Story:** Como mesero, quiero gestionar el estado de las mesas del restaurante, para que pueda saber cuáles están disponibles, abrir cuentas y asignar pedidos correctamente.

#### Acceptance Criteria

1. THE POS_System SHALL mantener un registro de todas las Mesas del restaurante con identificador único, nombre o número, y estado actual.
2. THE POS_System SHALL gestionar los estados de cada Mesa con los valores: Libre, Ocupada y Reservada.
3. WHEN un Mesero selecciona una Mesa con estado Libre, THE POS_System SHALL crear una Cuenta nueva asociada a esa Mesa y cambiar su estado a Ocupada.
4. WHEN una Cuenta es cerrada mediante cobro, THE POS_System SHALL cambiar el estado de la Mesa asociada a Libre.
5. WHILE una Mesa tiene estado Ocupada, THE POS_System SHALL mostrar el tiempo transcurrido desde la apertura de la Cuenta y el total acumulado de la Cuenta.
6. IF un Mesero intenta abrir una Cuenta en una Mesa con estado Ocupada, THEN THE POS_System SHALL rechazar la operación y mostrar un mensaje indicando que la Mesa ya tiene una Cuenta abierta.
7. THE POS_System SHALL permitir al Usuario con Rol Admin crear, editar y eliminar Mesas del sistema.
8. WHEN una Mesa es eliminada, THE POS_System SHALL verificar que no tenga una Cuenta abierta; IF la Mesa tiene una Cuenta abierta, THEN THE POS_System SHALL rechazar la eliminación y notificar al Usuario.

---

### Requirement 12: Registro y Gestión de Pedidos

**User Story:** Como mesero, quiero registrar los pedidos de los clientes y enviarlos automáticamente a la estación de preparación correspondiente, para que cocina y barra reciban los pedidos en tiempo real sin necesidad de comunicación manual.

#### Acceptance Criteria

1. WHEN un Mesero agrega un Item_Pedido a una Cuenta abierta, THE POS_System SHALL registrar el ítem con Producto, cantidad, modificadores opcionales y marca de tiempo.
2. WHEN un Item_Pedido es registrado, THE POS_System SHALL verificar que el Producto tenga stock disponible en el Inventario antes de confirmar el ítem.
3. IF el stock disponible en el Inventario es insuficiente para la cantidad solicitada en el Item_Pedido, THEN THE POS_System SHALL rechazar el ítem y notificar al Mesero con el stock disponible actual.
4. WHEN un Item_Pedido es confirmado, THE POS_System SHALL crear una Reserva_Inventario por la cantidad correspondiente y reducir el stock disponible.
5. WHEN un Pedido es enviado a preparación, THE POS_System SHALL enrutar cada Item_Pedido a la Estacion asignada al Producto: platillos al KDS y bebidas al BDS.
6. THE POS_System SHALL transmitir los Pedidos a las Estaciones mediante WebSocket para garantizar la recepción en tiempo real sin necesidad de recarga manual.
7. WHEN un Mesero elimina un Item_Pedido de una Cuenta antes de enviarlo a preparación, THE POS_System SHALL liberar la Reserva_Inventario correspondiente y restaurar el stock disponible.
8. IF un Item_Pedido ya fue enviado a preparación, THEN THE POS_System SHALL requerir autorización de un Usuario con Rol Admin para permitir su cancelación.
9. THE POS_System SHALL permitir agregar múltiples rondas de pedidos a una misma Cuenta mientras la Mesa esté en estado Ocupada.

---

### Requirement 13: Estación de Cocina (KDS)

**User Story:** Como cocinero, quiero ver los pedidos de platillos en tiempo real y actualizar su estado de preparación, para que el mesero sepa cuándo están listos para servir.

#### Acceptance Criteria

1. WHEN un Pedido con Items_Pedido de platillos es enviado desde el POS, THE KDS SHALL mostrar el pedido en la pantalla de la Estacion de Cocina en menos de 3 segundos.
2. THE KDS SHALL mostrar para cada Item_Pedido: nombre del Producto, cantidad, modificadores, número de Mesa y tiempo transcurrido desde la recepción.
3. THE KDS SHALL gestionar los estados de cada Item_Pedido con los valores: Pendiente, Preparando y Listo.
4. WHEN un Usuario con Rol Cocina cambia el estado de un Item_Pedido a Preparando, THE KDS SHALL registrar la marca de tiempo de inicio de preparación y notificar al POS_System mediante WebSocket.
5. WHEN un Usuario con Rol Cocina cambia el estado de un Item_Pedido a Listo, THE KDS SHALL notificar al POS_System mediante WebSocket para que el Mesero sea alertado.
6. WHEN todos los Items_Pedido de un Pedido alcanzan el estado Listo, THE POS_System SHALL mostrar una alerta visual al Mesero indicando que el Pedido está completo para servir.
7. THE KDS SHALL ordenar los Items_Pedido por marca de tiempo de recepción, mostrando primero los más antiguos.
8. WHILE un Item_Pedido lleva más de 15 minutos en estado Pendiente o Preparando, THE KDS SHALL resaltar visualmente el ítem para indicar demora.

---

### Requirement 14: Estación de Barra (BDS)

**User Story:** Como bartender, quiero ver los pedidos de bebidas en tiempo real y actualizar su estado de preparación, para que el mesero sepa cuándo están listas para servir.

#### Acceptance Criteria

1. WHEN un Pedido con Items_Pedido de bebidas es enviado desde el POS, THE BDS SHALL mostrar el pedido en la pantalla de la Estacion de Barra en menos de 3 segundos.
2. THE BDS SHALL mostrar para cada Item_Pedido: nombre del Producto, cantidad, modificadores, número de Mesa y tiempo transcurrido desde la recepción.
3. THE BDS SHALL gestionar los estados de cada Item_Pedido con los valores: Pendiente, Preparando y Listo.
4. WHEN un Usuario con Rol Barra cambia el estado de un Item_Pedido a Preparando, THE BDS SHALL registrar la marca de tiempo de inicio de preparación y notificar al POS_System mediante WebSocket.
5. WHEN un Usuario con Rol Barra cambia el estado de un Item_Pedido a Listo, THE BDS SHALL notificar al POS_System mediante WebSocket para que el Mesero sea alertado.
6. WHEN todos los Items_Pedido de bebidas de un Pedido alcanzan el estado Listo, THE POS_System SHALL mostrar una alerta visual al Mesero indicando que las bebidas están listas.
7. THE BDS SHALL ordenar los Items_Pedido por marca de tiempo de recepción, mostrando primero los más antiguos.
8. WHILE un Item_Pedido lleva más de 10 minutos en estado Pendiente o Preparando, THE BDS SHALL resaltar visualmente el ítem para indicar demora.

---

### Requirement 15: Cobro y Cierre de Cuenta

**User Story:** Como mesero, quiero cobrar la cuenta de una mesa y cerrarla, para que el sistema registre el pago, libere la mesa y actualice el inventario.

#### Acceptance Criteria

1. WHEN un Mesero solicita el cobro de una Cuenta, THE POS_System SHALL mostrar el resumen de todos los Items_Pedido con subtotales, impuestos aplicables y total a pagar.
2. THE POS_System SHALL registrar el método de pago utilizado para cada cobro con los valores permitidos: Efectivo, Tarjeta de Crédito o Tarjeta de Débito.
3. WHEN el cobro es confirmado, THE POS_System SHALL cambiar el estado de la Cuenta a Cerrada, registrar la marca de tiempo de cierre y cambiar el estado de la Mesa a Libre.
4. WHEN el cobro es confirmado, THE POS_System SHALL convertir todas las Reservas_Inventario de la Cuenta en Movimientos_Inventario de tipo salida por venta.
5. IF algún Item_Pedido de la Cuenta tiene estado distinto a Listo al momento del cobro, THEN THE POS_System SHALL mostrar una advertencia al Mesero indicando los ítems pendientes antes de permitir continuar con el cobro.
6. THE POS_System SHALL generar un comprobante de venta con número de Cuenta, Mesa, lista de ítems, total, método de pago y marca de tiempo del cierre.
7. IF el método de pago es Efectivo, THEN THE POS_System SHALL calcular y mostrar el cambio a devolver al cliente basado en el monto recibido ingresado por el Mesero.

---

### Requirement 16: Gestión de Productos y Menú

**User Story:** Como administrador, quiero gestionar el catálogo de productos del menú incluyendo su imagen visual, para que los meseros puedan registrar pedidos con información actualizada de precios, disponibilidad y una representación visual clara de cada producto.

#### Acceptance Criteria

1. THE POS_System SHALL permitir al Usuario con Rol Admin crear Productos con los atributos: nombre, descripción, precio, Categoria, Estacion de preparación asignada e imagen opcional.
2. THE POS_System SHALL permitir al Usuario con Rol Admin editar los atributos de un Producto existente, incluyendo subir, reemplazar o eliminar su imagen.
3. WHEN el precio de un Producto es modificado, THE POS_System SHALL aplicar el nuevo precio únicamente a los Pedidos creados después de la modificación, sin afectar Cuentas abiertas.
4. THE POS_System SHALL permitir al Usuario con Rol Admin desactivar un Producto para que no aparezca disponible en el POS sin eliminarlo del historial.
5. WHILE un Producto está desactivado, THE POS_System SHALL impedir que los Meseros lo agreguen a nuevos Pedidos.
6. THE POS_System SHALL clasificar cada Producto en exactamente una Categoria y asignarlo a exactamente una Estacion (Cocina o Barra).
7. THE POS_System SHALL permitir al Usuario con Rol Admin crear, editar y eliminar Categorias de Productos.
8. IF un Usuario intenta eliminar una Categoria que tiene Productos asociados, THEN THE POS_System SHALL rechazar la eliminación y notificar al Usuario la cantidad de Productos afectados.
9. THE POS_System SHALL aceptar imágenes de Producto en los formatos JPG, PNG y WebP con un tamaño máximo de 2MB por archivo.
10. WHEN el Usuario con Rol Admin sube una imagen para un Producto, THE POS_System SHALL almacenar la imagen y asociarla al Producto para su visualización en el POS y en la pantalla de administración.
11. WHEN un Producto no tiene imagen asignada, THE Frontend_Service SHALL mostrar un placeholder visual que contenga el nombre del Producto centrado sobre un fondo de color, simulando la presencia de una imagen.
12. THE Frontend_Service SHALL generar el color de fondo del placeholder de forma determinista a partir del nombre del Producto, de modo que el mismo Producto siempre muestre el mismo color en todos los clientes.
13. WHEN el Usuario con Rol Admin elimina la imagen de un Producto, THE POS_System SHALL revertir la visualización del Producto al placeholder de texto de forma inmediata en todos los clientes conectados.

---

### Requirement 17: Control de Inventario

**User Story:** Como administrador, quiero controlar el stock de insumos y productos, para que el sistema impida ventas sin existencias y mantenga un registro auditado de todos los movimientos.

#### Acceptance Criteria

1. THE POS_System SHALL mantener un registro de Inventario por cada insumo o Producto con: nombre, unidad de medida, stock actual, stock mínimo y stock máximo.
2. THE POS_System SHALL registrar un Movimiento_Inventario por cada cambio de stock con: tipo (entrada, salida por venta, ajuste, merma), cantidad, fecha/hora y Usuario responsable.
3. WHEN el stock de un ítem del Inventario cae por debajo del stock mínimo configurado, THE POS_System SHALL generar una alerta visible para el Usuario con Rol Admin.
4. THE POS_System SHALL impedir la confirmación de un Item_Pedido cuando el stock disponible del Producto asociado sea igual a cero.
5. WHEN un Pedido es cancelado después de haber sido enviado a preparación, THE POS_System SHALL revertir las Reservas_Inventario correspondientes y restaurar el stock disponible, previa autorización de un Usuario con Rol Admin.
6. THE POS_System SHALL permitir al Usuario con Rol Admin registrar entradas de stock con cantidad, proveedor opcional y fecha.
7. THE POS_System SHALL permitir al Usuario con Rol Admin registrar ajustes de inventario (mermas, correcciones) con motivo obligatorio para mantener la trazabilidad.
8. THE POS_System SHALL garantizar que el stock disponible nunca sea negativo; IF una operación resultara en stock negativo, THEN THE POS_System SHALL rechazar la operación y notificar al Usuario.

---

### Requirement 18: Gestión de Usuarios y Roles

**User Story:** Como administrador, quiero gestionar los usuarios del sistema y sus roles, para que el acceso esté controlado y actualizado según el personal activo del restaurante.

#### Acceptance Criteria

1. THE POS_System SHALL permitir al Usuario con Rol Admin crear nuevos Usuarios con nombre, apellido, nombre de usuario único, contraseña inicial y Rol asignado.
2. THE POS_System SHALL permitir al Usuario con Rol Admin editar el nombre, apellido y Rol de un Usuario existente.
3. THE POS_System SHALL permitir al Usuario con Rol Admin desactivar un Usuario para impedir su acceso sin eliminar su historial de actividad.
4. WHEN un Usuario es desactivado, THE POS_System SHALL invalidar inmediatamente cualquier sesión activa de ese Usuario.
5. THE POS_System SHALL permitir a cada Usuario cambiar su propia contraseña proporcionando la contraseña actual como verificación.
6. THE POS_System SHALL permitir al Usuario con Rol Admin restablecer la contraseña de cualquier Usuario sin requerir la contraseña actual.
7. IF un Usuario intenta autenticarse con credenciales incorrectas 5 veces consecutivas, THEN THE POS_System SHALL bloquear temporalmente la cuenta por 15 minutos y notificar al Usuario con Rol Admin.
8. THE POS_System SHALL mantener un registro de auditoría de los accesos al sistema con: Usuario, fecha/hora, acción (inicio de sesión, cierre de sesión, bloqueo) y resultado.

---

### Requirement 19: Reportes Operativos y Financieros

**User Story:** Como administrador, quiero generar reportes de ventas, inventario y operación, para que pueda tomar decisiones basadas en datos reales del negocio.

#### Acceptance Criteria

1. THE POS_System SHALL generar un Reporte de Ventas por período (día, semana, mes o rango personalizado) con: total de ventas, número de Cuentas cerradas, ticket promedio y desglose por método de pago.
2. THE POS_System SHALL generar un Reporte de Productos más vendidos por período con: nombre del Producto, cantidad vendida, ingresos generados y Categoria.
3. THE POS_System SHALL generar un Reporte de Inventario con: stock actual de cada ítem, Movimientos_Inventario del período seleccionado y alertas de stock mínimo activas.
4. THE POS_System SHALL generar un Reporte de Operación por Turno con: tiempo promedio de preparación por Estacion, número de Pedidos procesados y número de cancelaciones.
5. WHEN un Reporte es generado, THE POS_System SHALL calcular los datos directamente desde los registros persistidos en la base de datos para garantizar consistencia con las transacciones realizadas.
6. THE POS_System SHALL permitir al Usuario con Rol Admin exportar cualquier Reporte en formato CSV.
7. IF el período seleccionado para un Reporte no contiene datos, THEN THE POS_System SHALL mostrar el Reporte con valores en cero e indicar explícitamente que no hay registros para el período.
8. THE POS_System SHALL restringir el acceso a todos los Reportes exclusivamente a Usuarios con Rol Admin.

---

### Requirement 20: Sincronización en Tiempo Real entre Estaciones

**User Story:** Como operador del restaurante, quiero que los cambios de estado de los pedidos se reflejen en tiempo real en todas las pantallas del sistema, para que POS, cocina y barra estén siempre sincronizados sin necesidad de recargar manualmente.

#### Acceptance Criteria

1. THE POS_System SHALL mantener conexiones WebSocket activas con todos los clientes conectados (POS, KDS, BDS) para la transmisión de eventos en tiempo real.
2. WHEN el estado de un Item_Pedido cambia en el KDS o BDS, THE POS_System SHALL propagar el cambio de estado a todos los clientes suscritos a esa Cuenta en menos de 2 segundos.
3. WHEN un nuevo Pedido es registrado en el POS, THE POS_System SHALL notificar a la Estacion correspondiente mediante WebSocket en menos de 3 segundos.
4. IF la conexión WebSocket de un cliente se interrumpe, THEN THE POS_System SHALL intentar reconectar automáticamente con retroceso exponencial hasta un máximo de 5 intentos antes de mostrar un error de conectividad al Usuario.
5. WHEN un cliente WebSocket se reconecta después de una interrupción, THE POS_System SHALL enviar el estado actual de todos los Pedidos activos para sincronizar el cliente con el estado del servidor.
6. THE POS_System SHALL registrar en el log del sistema cada evento de desconexión y reconexión de clientes WebSocket con marca de tiempo y identificador del cliente.

---

### Requirement 21: Rutas de Integración con Apache Camel

**User Story:** Como desarrollador backend, quiero definir y gestionar rutas de integración con Apache Camel, para que el sistema pueda orquestar flujos de datos entre el POS, las estaciones y servicios externos de forma declarativa y mantenible.

#### Acceptance Criteria

1. THE Backend_Service SHALL exponer las rutas de integración de Apache_Camel como beans de Spring/CDI registrados en el contexto de Quarkus.
2. WHEN una ruta de Apache_Camel es iniciada, THE Backend_Service SHALL registrar en el log el identificador de la ruta, el endpoint de origen y el estado de inicio.
3. THE Backend_Service SHALL definir una ruta de Apache_Camel para el enrutamiento de Items_Pedido hacia la Estacion correspondiente (KDS o BDS) basándose en la Categoria del Producto.
4. WHEN un mensaje entra a una ruta de Apache_Camel, THE Backend_Service SHALL validar el esquema del mensaje antes de procesarlo; IF el mensaje no cumple el esquema, THEN THE Backend_Service SHALL enviar el mensaje a una ruta de error dedicada (Dead Letter Channel).
5. THE Backend_Service SHALL implementar el patrón Dead Letter Channel de Apache_Camel para capturar mensajes que fallen después de 3 reintentos, registrando el mensaje original y la causa del fallo.
6. WHEN una ruta de Apache_Camel procesa un mensaje exitosamente, THE Backend_Service SHALL registrar en el log el identificador de la ruta, el tiempo de procesamiento en milisegundos y el identificador del mensaje.
7. THE Backend_Service SHALL exponer métricas de las rutas de Apache_Camel (mensajes procesados, mensajes fallidos, tiempo promedio de procesamiento) a través del endpoint de métricas de Quarkus.
8. WHERE el proyecto requiera integración con sistemas externos (correo, notificaciones push), THE Backend_Service SHALL implementar la integración mediante componentes de Apache_Camel sin lógica de transporte en el código de negocio.

---

### Requirement 22: API REST con Quarkus

**User Story:** Como desarrollador backend, quiero exponer los recursos del sistema como una API REST con Quarkus, para que el frontend Angular y otros clientes puedan consumir las operaciones del POS de forma estándar y documentada.

#### Acceptance Criteria

1. THE Backend_Service SHALL exponer todos los recursos del POS_System como endpoints REST bajo el path base `/api/v1` siguiendo las convenciones de diseño RESTful.
2. THE Backend_Service SHALL responder a todas las solicitudes HTTP con los códigos de estado estándar: 200 para éxito, 201 para creación, 400 para solicitud inválida, 401 para no autenticado, 403 para no autorizado, 404 para recurso no encontrado y 500 para error interno.
3. THE Backend_Service SHALL serializar y deserializar todos los cuerpos de solicitud y respuesta en formato JSON con codificación UTF-8.
4. WHEN el Backend_Service recibe una solicitud con cuerpo JSON inválido o campos requeridos faltantes, THE Backend_Service SHALL retornar HTTP 400 con un cuerpo JSON que incluya el campo afectado y una descripción del error de validación.
5. THE Backend_Service SHALL documentar todos los endpoints REST mediante la especificación OpenAPI 3.0, accesible en el path `/q/openapi`.
6. THE Backend_Service SHALL exponer una interfaz Swagger UI en el path `/q/swagger-ui` en el perfil de desarrollo para facilitar la exploración y prueba de la API.
7. WHEN el Backend_Service procesa una solicitud que modifica datos, THE Backend_Service SHALL ejecutar la operación dentro de una transacción de base de datos; IF la transacción falla, THEN THE Backend_Service SHALL hacer rollback y retornar HTTP 500 con un identificador de correlación para trazabilidad.
8. THE Backend_Service SHALL implementar paginación en todos los endpoints que retornen colecciones, aceptando los parámetros `page` (número de página, base 0) y `size` (tamaño de página, máximo 100).

---

### Requirement 23: Interfaz de Usuario con Angular

**User Story:** Como desarrollador frontend, quiero construir la interfaz de usuario del POS con Angular, para que los operadores del restaurante tengan una experiencia de usuario consistente, reactiva y accesible desde cualquier navegador moderno.

#### Acceptance Criteria

1. THE Frontend_Service SHALL servir la aplicación Angular como una Single Page Application (SPA) accesible en el puerto 4200 durante el desarrollo.
2. THE Frontend_Service SHALL implementar el enrutamiento de la aplicación con Angular Router, definiendo rutas protegidas que requieran autenticación y rutas públicas para el login.
3. WHEN un Usuario no autenticado intenta acceder a una ruta protegida, THE Frontend_Service SHALL redirigir al Usuario a la pantalla de login sin mostrar contenido protegido.
4. THE Frontend_Service SHALL gestionar el estado de la sesión del Usuario mediante un servicio de autenticación que almacene el token en `sessionStorage` y lo incluya en cada solicitud HTTP al Backend_Service.
5. THE Frontend_Service SHALL implementar un interceptor HTTP de Angular que adjunte el token de autenticación a todas las solicitudes al Backend_Service y maneje las respuestas HTTP 401 redirigiendo al login.
6. THE Frontend_Service SHALL implementar la pantalla del POS con un componente de mapa de mesas que muestre el estado visual de cada Mesa (Libre, Ocupada, Reservada) con actualización en tiempo real vía WebSocket.
7. THE Frontend_Service SHALL implementar los componentes KDS y BDS como vistas independientes accesibles según el Rol del Usuario autenticado.
8. WHEN el Frontend_Service recibe un error HTTP del Backend_Service, THE Frontend_Service SHALL mostrar un mensaje de error descriptivo al Usuario sin exponer detalles técnicos internos del servidor.
9. THE Frontend_Service SHALL ser compatible con los navegadores Chrome, Firefox, Edge y Safari en sus versiones estables más recientes.

---

### Requirement 24: Comunicación Frontend-Backend

**User Story:** Como desarrollador, quiero que el frontend Angular y el backend Quarkus se comuniquen de forma segura y eficiente, para que los datos fluyan correctamente entre la interfaz de usuario y la lógica de negocio.

#### Acceptance Criteria

1. THE Frontend_Service SHALL comunicarse con el Backend_Service exclusivamente a través de la URL configurada en la variable de entorno `API_URL`, sin URLs hardcodeadas en el código fuente.
2. THE Frontend_Service SHALL establecer conexiones WebSocket con el Backend_Service en el path `/ws` para recibir eventos en tiempo real de cambios de estado de Pedidos y Mesas.
3. WHEN la conexión WebSocket entre el Frontend_Service y el Backend_Service se interrumpe, THE Frontend_Service SHALL intentar reconectar automáticamente con retroceso exponencial (1s, 2s, 4s, 8s, máximo 30s) y mostrar un indicador visual de estado de conexión al Usuario.
4. THE Backend_Service SHALL implementar CORS permitiendo solicitudes desde el origen del Frontend_Service configurado en las variables de entorno, rechazando solicitudes de orígenes no autorizados con HTTP 403.
5. THE Frontend_Service SHALL enviar todas las solicitudes HTTP al Backend_Service con el header `Content-Type: application/json` y el header `Accept: application/json`.
6. WHEN el Backend_Service retorna HTTP 401, THE Frontend_Service SHALL limpiar el token de sesión almacenado, redirigir al Usuario a la pantalla de login y mostrar un mensaje indicando que la sesión expiró.
7. THE Backend_Service SHALL implementar compresión GZIP para respuestas HTTP con cuerpo mayor a 1KB para reducir el ancho de banda consumido entre servicios.

---

### Requirement 25: Manejo de Errores y Logging

**User Story:** Como desarrollador y operador, quiero que el sistema registre errores y eventos operativos de forma estructurada, para que pueda diagnosticar problemas en producción y desarrollo sin acceso directo a los contenedores.

#### Acceptance Criteria

1. THE Backend_Service SHALL emitir todos los logs en formato JSON estructurado con los campos: `timestamp`, `level`, `logger`, `message`, `traceId` y `spanId` para facilitar la correlación de trazas.
2. THE Backend_Service SHALL asignar un identificador de correlación único (`traceId`) a cada solicitud HTTP entrante y propagarlo en todos los logs y respuestas de error generados durante el procesamiento de esa solicitud.
3. WHEN el Backend_Service captura una excepción no controlada, THE Backend_Service SHALL registrar el stack trace completo en nivel ERROR con el `traceId` correspondiente y retornar HTTP 500 al cliente con el `traceId` como referencia.
4. THE Backend_Service SHALL registrar en nivel WARN todas las validaciones de negocio fallidas (stock insuficiente, estado de Mesa inválido, permisos insuficientes) sin incluir datos sensibles del Usuario en el mensaje de log.
5. THE Frontend_Service SHALL capturar todos los errores no controlados de Angular mediante un `ErrorHandler` global y registrarlos en la consola del navegador con el contexto del componente y la acción que originó el error.
6. THE Backend_Service SHALL configurar el nivel de log mediante la variable de entorno `QUARKUS_LOG_LEVEL`, con valor `DEBUG` en el perfil de desarrollo y `INFO` en el perfil de producción.
7. IF el Backend_Service no puede conectarse a la base de datos al iniciar, THEN THE Backend_Service SHALL registrar el error de conexión en nivel FATAL con los detalles de configuración (sin contraseña) y terminar el proceso con código de salida distinto de cero.
8. THE Backend_Service SHALL registrar en nivel INFO el inicio y fin de cada transacción de base de datos que modifique datos, incluyendo el tipo de operación y el tiempo de ejecución en milisegundos.

---

### Requirement 26: Health Checks y Monitoreo

**User Story:** Como operador del sistema, quiero que cada servicio exponga endpoints de health check y métricas, para que pueda monitorear el estado del sistema y detectar degradaciones antes de que afecten a los usuarios.

#### Acceptance Criteria

1. THE Backend_Service SHALL exponer un endpoint de liveness en `/q/health/live` que retorne HTTP 200 cuando el proceso está en ejecución y HTTP 503 cuando el proceso está en estado degradado irrecuperable.
2. THE Backend_Service SHALL exponer un endpoint de readiness en `/q/health/ready` que retorne HTTP 200 únicamente cuando el Backend_Service tiene conectividad activa con el Database_Service y todas las rutas de Apache_Camel están iniciadas.
3. WHEN el Database_Service no está disponible, THE Backend_Service SHALL retornar HTTP 503 en el endpoint `/q/health/ready` con un cuerpo JSON que indique el componente fallido y el motivo.
4. THE Backend_Service SHALL exponer métricas en formato Prometheus en el endpoint `/q/metrics`, incluyendo: número de solicitudes HTTP por endpoint y código de estado, tiempo de respuesta en percentiles p50/p95/p99, y uso de memoria JVM.
5. THE Compose_File SHALL configurar un `healthcheck` para el Backend_Service que consulte `/q/health/live` cada 30 segundos con un timeout de 5 segundos y 3 reintentos antes de marcar el servicio como `unhealthy`.
6. THE Compose_File SHALL configurar un `healthcheck` para el Database_Service que verifique la disponibilidad del motor de base de datos cada 10 segundos con un timeout de 3 segundos.
7. WHEN el Frontend_Service detecta que el Backend_Service no responde a solicitudes HTTP en más de 10 segundos, THE Frontend_Service SHALL mostrar un banner de advertencia al Usuario indicando que el servicio no está disponible y deshabilitar las acciones que requieran conectividad.
8. THE Backend_Service SHALL exponer el endpoint `/q/info` con información de la versión del build, rama de Git y hash del commit para facilitar la identificación de la versión desplegada.

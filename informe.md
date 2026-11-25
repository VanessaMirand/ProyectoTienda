# Informe de Evaluación Arquitectónica — Variante Q2 / Fila 1

**Integrantes:** Paula Miranda, Sergio Rodríguez  
**Variante asignada:** Q2 / Fila 1 (Mouse: $45.000, stock 40, umbral 5, descuento 10 %)

## Archivos modificados o agregados entre `hoja-1` y `final`

| Estado | Ruta del archivo | Módulo / Rol |
| :---: | :--- | :--- |
| `A` | `codigo/src/main/java/com/ejemplo/tienda/facturacion/FacturacionListener.java` | Módulo `facturacion` (suscriptor Q2) |
| `A` | `codigo/src/main/java/com/ejemplo/tienda/notificaciones/NotificacionesListener.java` | Módulo `notificaciones` (suscriptor Q2) |
| `A` | `codigo/src/main/java/com/ejemplo/tienda/pedidos/internal/Muestra2Runner.java` | Ejecución de muestra Q2 (`@Profile("muestra2")`) |
| `A` | `codigo/src/main/resources/application-muestra2.properties` | Configuración limpia de perfil `muestra2` |
| `A` | `docs/adr/002-idempotencia-pedidos.md` | ADR de idempotencia en pedidos |
| `A` | `docs/adr/0002-absorcion-cambio-q2.md` | ADR de evaluación y absorción de Q2 |
| `A` | `evidencia.txt` | Evidencia literal de pruebas, muestras y Git |

## 1. ¿El cambio entró por extensión o por modificación?

El cambio entró **100 % por extensión**. Como demuestra la tabla de `git diff --name-status hoja-1 final`, todos los archivos tienen marca `A` (agregados) y ninguno marca `M` (modificados) sobre `catalogo` o `pedidos`. La extensibilidad se logró aprovechando el contrato público `PedidoConfirmado` que el módulo `pedidos` ya publicaba al confirmar una orden. Los nuevos módulos `facturacion` y `notificaciones` se suscriben a dicho evento sin que `pedidos` conozca su existencia ni importe sus clases, preservando intactas tanto la lógica de negocio como las pruebas automatizadas de la Hoja 1.

## 2. ¿Qué parte de la arquitectura facilitó o estorbó el cambio?

La frontera modular impuesta por **Spring Modulith** y validada por `ModularityTests` facilitó enormemente el cambio: al existir un evento de dominio limpio (`PedidoConfirmado`) en el paquete raíz de `pedidos`, agregar dos consumidores independientes tomó minutos y garantizó en tiempo de prueba que ninguno accediera a clases de `pedidos.internal`. Por otro lado, la configuración de perfiles sin contexto web (`web-application-type=none`) exigió ajustar `@TransactionalEventListener` hacia `@EventListener` con `@Order(1)` y `@Order(2)` para asegurar la ejecución determinista de facturación antes de notificación sin depender de transacciones externas en el runner de muestra.

## 3. ¿En qué escenario concreto esta arquitectura dejaría de funcionar?

Esta arquitectura dejaría de funcionar en un escenario de **alta concurrencia distribuida con múltiples réplicas y fallos de red hacia proveedores externos reales** (por ejemplo, DIAN o pasarelas SMTP/SMS). Actualmente, los listeners corren en memoria dentro del mismo proceso (`@EventListener`) y el consecutivo de factura vive en un `AtomicInteger` volátil: si el proceso cae tras confirmar el pedido o se despliegan dos instancias paralelas, se perderían eventos no persistidos en un *outbox* transaccional y se duplicarían consecutivos `F-XXXX`. En ese punto sería necesario migrar hacia eventos persistentes (`spring-modulith-events` con tabla `EVENT_PUBLICATION`) o un bróker de mensajería con bloqueo distribuido.
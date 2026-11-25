# ADR-0002: Absorción del cambio Q2 mediante módulos desacoplados orientados a eventos

* **Estado:** Aceptado
* **Fecha:** 2026-11-25
* **Autores:** Paula Miranda, Sergio Rodríguez
* **Relacionado con:** ADR-0001 (Arquitectura Monolito Modular con Spring Modulith)

## Contexto y problema

El requisito **Q2 (Variante Fila 1)** exige que, cada vez que un pedido pase a estado `CONFIRMADO`, el sistema realice dos acciones adicionales sin alterar el comportamiento existente de los módulos `catalogo` ni `pedidos`:
1. Emitir una factura con consecutivo interno (`F-0001`, `F-0002`, ...) o registrar fallo controlado para el cliente `C-999` sin consumir consecutivo.
2. Enviar un aviso de notificación al cliente incluso cuando la facturación falle.

## Decisión

Se crearon dos nuevos módulos de primer nivel dentro de `com.ejemplo.tienda`:
* **`facturacion` (`FacturacionListener`):** Escucha el evento público `PedidoConfirmado` (`@EventListener`, `@Order(1)`), verifica si `clienteId` es `C-999` para registrar el fallo sin incrementar el `AtomicInteger` del consecutivo, o genera la factura `F-XXXX`.
* **`notificaciones` (`NotificacionesListener`):** Escucha el mismo evento `PedidoConfirmado` (`@EventListener`, `@Order(2)`) de forma independiente al resultado de `facturacion`, garantizando que el cliente siempre reciba su aviso.

Ningún archivo existente de `catalogo` ni de `pedidos` fue modificado, ni se tocaron las pruebas unitarias e integradas de la Hoja 1.

## Evaluación de la predicción registrada en ADR-0001

* **¿Qué se cumplió?** En el ADR-0001 se predijo que un monolito modular respaldado por eventos de dominio y verificaciones automáticas (`ModularityTests`) permitiría absorber requisitos nuevos de integración/extensión creando módulos nuevos suscribiéndose a eventos ya expuestos (`PedidoConfirmado`), logrando **cero modificaciones** en los módulos núcleo. Esto se cumplió al 100 %: `git diff --name-status hoja-1 final` confirma únicamente archivos agregados (`A`).
* **¿Qué sorpresa o ajuste surgió?** Al ejecutar la `muestra2` con perfil sin servidor web (`spring.main.web-application-type=none`), se observó que `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` requería un contexto transaccional activo explícito en el invocador de muestra para dispararse. Cambiar la suscripción a `@EventListener` síncrono ordenado (`@Order(1)` y `@Order(2)`) resolvió el despacho inmediato tras la publicación del evento dentro del mismo flujo de confirmación, manteniendo el desacoplamiento entre módulos y el aislamiento de fallos en `facturacion` frente a `notificaciones`.
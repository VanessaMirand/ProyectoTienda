# ADR-0001 Elección de arquitectura monolito modular

Variante: Q2 / Fila 1
Fecha: 2026-11-25 13:00
Estado: aceptada

## Contexto
La tienda gestiona el producto Mouse (productoId 1, precio 45000, stock inicial 40, umbral 5, descuento 10%) y debe registrar pedidos consecutivos validando stock, descuentos y consultas. Además, el atributo prioritario Q2 exige extensibilidad para agregar dos reacciones nuevas al confirmar un pedido (facturación con consecutivo F-0001 que puede fallar en C-999 sin revertir el pedido, y aviso al cliente) sin reabrir ni acoplar el flujo principal de registro de pedidos. Para la persistencia se optó por H2 en memoria para garantizar arranques limpios deterministas sin depender de contenedores externos.

## Alternativas consideradas
- **Arquitectura en capas:** descartada porque en Q2 obligaría a inyectar directamente los servicios de facturación y notificación dentro de `OrderService`, acoplando el registro del pedido (Mouse, umbral 5) con reacciones secundarias y haciendo que un fallo en C-999 arriesgue la transacción principal.
- **Arquitectura hexagonal:** descartada porque, aunque aísla el dominio del Mouse y el cálculo de descuento (10% desde 5 unidades), exige crear múltiples puertos y adaptadores adicionales cuando nuestro punto de partida ya provee comunicación desacoplada por eventos internos entre módulos.
- **Arquitectura basada en eventos (distribuida con RabbitMQ):** descartada porque introduce consistencia eventual sobre un broker externo y complejidad para garantizar el orden estricto de los consecutivos de pedido (1, 2, 3) y de factura (`F-0001`, `F-0002`) sobre un stock pequeño de 40 unidades de Mouse, cuando el monolito modular resuelve la extensibilidad localmente mediante eventos de aplicación.

## Decisión
Adoptar la arquitectura de monolito modular con Spring Modulith (módulos `catalogo` y `pedidos`), usando H2 en memoria y publicando eventos internos de dominio (`PedidoConfirmado`) para desacoplar las reacciones futuras.

## Consecuencias
**Se gana:** Encapsulamiento estricto por módulos verificado con `ModularityTests`, transacciones ACID para descontar el stock de 40 unidades de Mouse sin sobreventa, y capacidad de suscribir nuevos listeners al evento `PedidoConfirmado` para absorber Q2 sin modificar la lógica central de pedidos.
**Se pierde:** No es posible escalar de forma independiente los futuros módulos de facturación y notificaciones ni desplegarlos en procesos separados, pues comparten la misma JVM y base de datos en memoria.
**Predicción del costo de la Hoja 2:** Se espera crear los paquetes/módulos `facturacion` (con servicio y manejador de consecutivo `F-0001`) y `notificaciones` (listener de aviso al cliente), suscribiéndolos al evento público `PedidoConfirmado` ya existente en `pedidos`. No se espera modificar la clase que calcula el total (`CalculadotaTotal`), ni la clase que registra el pedido (`OrderService`), ni las pruebas de negocio de la Hoja 1 (`ReglasDeNegocioTest`, `OrderFlowTests`).
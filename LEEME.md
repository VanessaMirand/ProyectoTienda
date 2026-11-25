# Proyecto Arquitectura de Software — Tienda (Monolito Modular)

* **Estudiantes:** Paula Miranda, Sergio Rodríguez
* **Universidad de Caldas** — Ingeniería Informática
* **Variante:** Q2 / Fila 1

## Requisitos previos

* **JDK 21 o superior** (probado con OpenJDK 27).
* **Apache Maven 3.9+** (`mvn` disponible en terminal).

## Estructura del repositorio

* `codigo/`: Código fuente del monolito modular (`catalogo`, `pedidos`, `facturacion`, `notificaciones`) y pruebas de arquitectura (`ModularityTests`).
* `docs/adr/`: Registros de Decisiones de Arquitectura (`0001-arquitectura-monolito-modular.md`, `002-idempotencia-pedidos.md`, `0002-absorcion-cambio-q2.md`).
* `evidencia.txt`: Salidas literales de las pruebas modulares, muestras y comandos Git.
* `informe.md`: Evaluación arquitectónica de la absorción del cambio Q2.

## Cómo ejecutar las pruebas

Ubicarse dentro de la carpeta `codigo/` y ejecutar:

```bash
cd codigo
mvn test
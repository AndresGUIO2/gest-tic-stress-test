# GestTICSTest — Pruebas de rendimiento para GestionaTIC

Pruebas de rendimiento implementadas con Gatling para la API GestionaTIC. Este documento describe los tipos de prueba disponibles, cómo ejecutarlas y cómo interpretar los reportes generados.

## Propósito

Centralizar la ejecución y la interpretación de pruebas de rendimiento (endpoints, escalabilidad, paramétricas y estrés) para facilitar la validación de comportamiento y la identificación de cuellos de botella.

## Estructura del proyecto

- `src/test/scala/gestionatic`: simulaciones Gatling (scenarios y feeders).
- `ejecutar_pruebas.ps1`: script PowerShell interactivo para ejecutar las pruebas.
- `pom.xml`: configuración de Maven y perfiles (`endpoints`, `scalability`, `stress`, `bdd`).
- `target/gatling`: reportes HTML generados por Gatling tras cada ejecución.
- `target/surefire-reports`: reportes y logs de pruebas auxiliares.

## Ejecuciones registradas (ejemplos)

- `scalabilitytest-01user-20251130221634488`
- `scalabilitytest-10users-20251130221800028`
- `scalabilitytest-100users-20251130221706661`
- `scalabilitytestparametric-20251130221552402`
- `stresstest-breakpoint-20251130221843431`

## Tipos de prueba y objetivo

### Pruebas de Endpoints (carga ligera)

- Objetivo: validar que cada endpoint responde correctamente bajo cargas esperadas.
- Qué mide: latencia media, tasa de éxito de peticiones y tiempos de respuesta básicos.
- Uso: detectar regresiones funcionales o de rendimiento en puntos concretos del API.

### Pruebas de Escalabilidad

- Objetivo: evaluar comportamiento al aumentar usuarios concurrentes (por ejemplo: 1, 10, 100).
- Qué mide: degradación de latencias, variación del throughput y comportamiento de errores ante carga creciente.
- Métricas clave: throughput (req/s), percentiles (p50/p90/p95/p99), error rate, tiempo medio y máximo de respuesta.

### Pruebas Paramétricas

- Objetivo: ejecutar escenarios con parámetros definidos (número de usuarios, duración) sin modificar la simulación.
- Uso: análisis “what-if” para distintos puntos de operación.

### Pruebas de Estrés

- Objetivo: forzar el sistema hasta degradación o fallo para identificar punto de ruptura y problemas de recuperación.
- Consideración: ejecutar en entornos controlados con monitoreo de infraestructura (CPU, memoria, red, BD).

### Flujos end-to-end / BDD (opcional)

- Objetivo: validar flujos compuestos (por ejemplo: login → generar QR → validar) y medir su rendimiento bajo carga.

## Cómo ejecutar

### Script PowerShell (recomendado)

Ejecutar desde la carpeta del proyecto:

```powershell
.\ejecutar_pruebas.ps1
```

El script proporciona un menú para ejecutar pruebas de endpoints, escalabilidad (1/10/100 usuarios), estrés, todas las pruebas o pruebas paramétricas.

### Maven (ejemplos)

- Ejecutar una simulación específica:

```powershell
mvn gatling:test -Dgatling.simulationClass=gestionatic.ScalabilityTest_100Users
```

- Ejecutar por perfil (según `pom.xml`):

```powershell
mvn -Pscalability gatling:test
mvn -Pendpoints gatling:test
mvn -Pstress gatling:test
```

- Prueba paramétrica (ejemplo):

```powershell
mvn gatling:test -Dgatling.simulationClass=gestionatic.ScalabilityTestParametric -DUSERS=50 -DDURATION=120
```

## Ubicación de reportes

- Gatling: `target/gatling/<simulation-name-timestamp>/index.html` — abrir en navegador para ver el reporte.
- Logs de ejecución: `target/gatling/*/simulation.log`.
- Reportes auxiliares: `target/surefire-reports`.

## Interpretación rápida de repotes

- **Throughput (req/s):** capacidad observada del sistema.
- **Percentiles (p50/p90/p95/p99):** indican la experiencia de respuesta; percentiles altos muestran colas o latencias puntuales.
- **Error rate:** porcentaje de peticiones con código diferente al esperado; revisar 4xx/5xx.

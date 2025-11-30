package gestionatic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import gestionatic.Data._
import gestionatic.EndpointsAnalysis._

/**
 * =========================================================================
 * PRUEBA 1: GET - Consulta de Viajes de Estudiante
 * =========================================================================
 * Endpoint: GET /estudiantes/viajes
 * 
 * Justificación de alta transaccionalidad:
 * - Los estudiantes consultan frecuentemente su historial de viajes
 * - Se usa en la app móvil cada vez que abren la sección "Mis Viajes"
 * - Estimación: 500+ consultas/minuto en horas pico
 */
class Test01_GET_ConsultarViajesTest extends Simulation {

  val estudiantesFeeder = csv("estudiantes_test.csv").circular

  val escenario = scenario("GET - Consultar Viajes de Estudiante")
    .feed(estudiantesFeeder)
    .exec(consultarViajes)
    .pause(100.milliseconds, 500.milliseconds)

  // ===== CONFIGURACIÓN PARA ARQUITECTURA SERVERLESS =====
  // Modifica estos valores para simular diferentes niveles de concurrencia.
  // El objetivo es mantener tiempos de respuesta bajos (<2000ms) y % de errores mínimos (<5%),
  // sin preocuparse por CPU/RAM, ya que AWS Lambda escala automáticamente.

  val USUARIOS_CONCURRENTES = 1  // Cambiar a: 1, 10, 100, 500, 1000...
  val DURACION_RAMPA = 10        // Segundos para alcanzar el máximo de usuarios
  val DURACION_SOSTENIDA = 30    // Segundos manteniendo la carga

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds),
      constantUsersPerSec(USUARIOS_CONCURRENTES / 10.0).during(DURACION_SOSTENIDA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(2000),
    global.failedRequests.percent.lte(5)
  )
}

/**
 * =========================================================================
 * PRUEBA 2: GET - Obtener Rutas Disponibles
 * =========================================================================
 * Endpoint: GET /admin/rutas
 * 
 * Justificación de alta transaccionalidad:
 * - Dashboard de administración carga constantemente
 * - App de estudiantes consulta rutas disponibles
 * - Estimación: 200+ consultas/minuto
 */
class Test02_GET_ObtenerRutasTest extends Simulation {

  val escenario = scenario("GET - Obtener Rutas")
    .exec(obtenerRutas)
    .pause(100.milliseconds, 300.milliseconds)

  val USUARIOS_CONCURRENTES = 1
  val DURACION_RAMPA = 10
  val DURACION_SOSTENIDA = 30

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds),
      constantUsersPerSec(USUARIOS_CONCURRENTES / 10.0).during(DURACION_SOSTENIDA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(2000),
    global.failedRequests.percent.lte(5)
  )
}

/**
 * =========================================================================
 * PRUEBA 3: POST - Login de Usuario
 * =========================================================================
 * Endpoint: POST /auth/login
 * 
 * Justificación de alta transaccionalidad:
 * - CRÍTICO: Cada usuario debe autenticarse
 * - Se ejecuta al inicio de cada sesión
 * - Estimación: 1000+ logins/hora en horas pico
 */
class Test03_POST_LoginTest extends Simulation {

  val estudiantesFeeder = csv("estudiantes_test.csv").circular

  val escenario = scenario("POST - Login Usuario")
    .feed(estudiantesFeeder)
    .exec(loginUsuario)
    .pause(100.milliseconds, 500.milliseconds)

  val USUARIOS_CONCURRENTES = 1
  val DURACION_RAMPA = 10
  val DURACION_SOSTENIDA = 30

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds),
      constantUsersPerSec(USUARIOS_CONCURRENTES / 10.0).during(DURACION_SOSTENIDA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(3000),  // Login puede tomar más tiempo
    global.failedRequests.percent.lte(5)
  )
}

/**
 * =========================================================================
 * PRUEBA 4: POST - Generar Código QR
 * =========================================================================
 * Endpoint: POST /estudiantes/generar-qr
 * 
 * Justificación de alta transaccionalidad:
 * - CRÍTICO: Cada estudiante genera QR antes de abordar
 * - Se genera múltiples veces al día por estudiante
 * - Estimación: 800+ generaciones/hora en horas pico
 */
class Test04_POST_GenerarQRTest extends Simulation {

  val estudiantesFeeder = csv("estudiantes_test.csv").circular

  val escenario = scenario("POST - Generar QR")
    .feed(estudiantesFeeder)
    .exec(generarQR)
    .pause(100.milliseconds, 500.milliseconds)

  val USUARIOS_CONCURRENTES = 1
  val DURACION_RAMPA = 10
  val DURACION_SOSTENIDA = 30

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds),
      constantUsersPerSec(USUARIOS_CONCURRENTES / 10.0).during(DURACION_SOSTENIDA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(3000),
    global.failedRequests.percent.lte(5)
  )
}

/**
 * =========================================================================
 * PRUEBA 5: PUT - Actualizar Ruta
 * =========================================================================
 * Endpoint: PUT /admin/rutas
 * 
 * Justificación de alta transaccionalidad:
 * - Administradores actualizan capacidad en tiempo real
 * - Cambios de horarios y estados
 * - Estimación: 50+ actualizaciones/hora
 */
class Test05_PUT_ActualizarRutaTest extends Simulation {

  val capacidadFeeder = Iterator.continually(Map(
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length)),
    "capacidad" -> (30 + scala.util.Random.nextInt(20))
  ))

  val escenario = scenario("PUT - Actualizar Ruta")
    .feed(capacidadFeeder)
    .exec(actualizarRuta)
    .pause(200.milliseconds, 600.milliseconds)

  val USUARIOS_CONCURRENTES = 1
  val DURACION_RAMPA = 10
  val DURACION_SOSTENIDA = 30

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds),
      constantUsersPerSec(USUARIOS_CONCURRENTES / 10.0).during(DURACION_SOSTENIDA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(2000),
    global.failedRequests.percent.lte(10)
  )
}

/**
 * =========================================================================
 * PRUEBA 6: POST - Registrar Viaje Manual
 * =========================================================================
 * Endpoint: POST /conductores/registrar-viaje
 * 
 * Justificación de alta transaccionalidad:
 * - Conductores registran viajes cuando QR falla
 * - Backup crítico del sistema
 * - Estimación: 100+ registros/hora
 */
class Test06_POST_RegistrarViajeTest extends Simulation {

  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length))
  ))

  val escenario = scenario("POST - Registrar Viaje Manual")
    .feed(estudiantesFeeder)
    .feed(conductorFeeder)
    .exec(registrarViaje)
    .pause(200.milliseconds, 500.milliseconds)

  val USUARIOS_CONCURRENTES = 1
  val DURACION_RAMPA = 10
  val DURACION_SOSTENIDA = 30

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds),
      constantUsersPerSec(USUARIOS_CONCURRENTES / 10.0).during(DURACION_SOSTENIDA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(2000),
    global.failedRequests.percent.lte(10)
  )
}

/**
 * =========================================================================
 * PRUEBA 7: DELETE - Eliminar Ruta (con creación previa)
 * =========================================================================
 * Endpoint: DELETE /admin/rutas
 * 
 * Justificación de alta transaccionalidad:
 * - Eliminación de rutas obsoletas o canceladas
 * - Limpieza de datos de prueba
 * - Estimación: 20+ eliminaciones/día
 */
class Test07_DELETE_EliminarRutaTest extends Simulation {

  val randomIdFeeder = Iterator.continually(Map(
    "randomId" -> java.util.UUID.randomUUID().toString.take(8)
  ))

  val escenario = scenario("DELETE - Eliminar Ruta")
    .feed(randomIdFeeder)
    // Primero crear una ruta temporal
    .exec(crearRuta)
    .pause(500.milliseconds)
    // Luego eliminarla
    .doIf(session => session.contains("id_ruta_temp")) {
      exec(eliminarRuta)
    }
    .pause(200.milliseconds, 500.milliseconds)

  val USUARIOS_CONCURRENTES = 1
  val DURACION_RAMPA = 10
  val DURACION_SOSTENIDA = 30

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(3000),
    global.failedRequests.percent.lte(20)
  )
}

/**
 * =========================================================================
 * PRUEBA 8: POST - Validar QR (Invalidación/Soft Delete)
 * =========================================================================
 * Endpoint: POST /conductores/validar-qr
 * 
 * Justificación de alta transaccionalidad:
 * - CRÍTICO: Cada abordaje requiere validación de QR
 * - Invalida el QR para evitar reutilización
 * - Estimación: 600+ validaciones/hora en horas pico
 */
class Test08_POST_ValidarQRTest extends Simulation {

  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length))
  ))

  val escenario = scenario("POST - Validar QR")
    .feed(estudiantesFeeder)
    .feed(conductorFeeder)
    // Primero generar QR
    .exec(generarQR)
    .pause(300.milliseconds)
    // Luego validarlo (si se generó correctamente)
    .doIf(session => session.contains("qr_data")) {
      exec(validarQR)
    }
    .pause(200.milliseconds, 500.milliseconds)

  val USUARIOS_CONCURRENTES = 1
  val DURACION_RAMPA = 10
  val DURACION_SOSTENIDA = 30

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(3000),
    global.failedRequests.percent.lte(20)
  )
}

/**
 * =========================================================================
 * PRUEBA 9: FLUJO COMPLETO ESTUDIANTE
 * =========================================================================
 * Flujo: Login → Generar QR → Consultar Viajes
 * 
 * Justificación: Simula el uso real de la app por un estudiante
 */
class Test09_FlujoEstudianteTest extends Simulation {

  val estudiantesFeeder = csv("estudiantes_test.csv").circular

  val escenario = scenario("Flujo Completo Estudiante")
    .feed(estudiantesFeeder)
    .exec(
      // 1. Login
      loginUsuario
    )
    .pause(500.milliseconds, 1.second)
    .exec(
      // 2. Generar QR para abordar
      generarQR
    )
    .pause(1.second, 2.seconds)
    .exec(
      // 3. Consultar historial de viajes
      consultarViajes
    )
    .pause(500.milliseconds)

  val USUARIOS_CONCURRENTES = 1
  val DURACION_RAMPA = 15

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(5000),
    global.failedRequests.percent.lte(10)
  )
}

/**
 * =========================================================================
 * PRUEBA 10: FLUJO COMPLETO CONDUCTOR
 * =========================================================================
 * Flujo: Generar QR (estudiante) → Validar QR (conductor) → Ver estado
 * 
 * Justificación: Simula la interacción conductor-estudiante
 */
class Test10_FlujoConductorTest extends Simulation {

  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length))
  ))

  val escenario = scenario("Flujo Completo Conductor")
    .feed(estudiantesFeeder)
    .feed(conductorFeeder)
    .exec(
      // 1. Estudiante genera QR
      generarQR
    )
    .pause(1.second, 2.seconds)
    .doIf(session => session.contains("qr_data")) {
      exec(
        // 2. Conductor valida QR
        validarQR
      )
    }
    .pause(500.milliseconds, 1.second)
    .exec(
      // 3. Consultar rutas (verificar estado)
      obtenerRutas
    )
    .pause(500.milliseconds)

  val USUARIOS_CONCURRENTES = 1
  val DURACION_RAMPA = 15

  setUp(
    escenario.inject(
      nothingFor(2.seconds),
      rampUsers(USUARIOS_CONCURRENTES).during(DURACION_RAMPA.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.percentile3.lte(5000),
    global.failedRequests.percent.lte(20)
  )
}

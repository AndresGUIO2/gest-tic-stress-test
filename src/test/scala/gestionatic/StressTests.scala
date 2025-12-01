package gestionatic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import gestionatic.Data._
import gestionatic.EndpointsAnalysis._

/**
 * =========================================================================
 * PRUEBAS DE ESTRÉS
 * =========================================================================
 * Objetivo: Incrementar gradualmente la carga para identificar puntos de falla
 * y evaluación de recuperación. Estas pruebas deben ejecutarse en entornos
 * de staging o controlados; monitorice recursos (CPU, memoria, red, BD) y
 * detenga la ejecución si el uso de recursos compromete el entorno (por
 * ejemplo, consumo aproximado > 90%).
 *
 * Instrucciones de ejecución: utilizar la simulación `StressTest_Progressive`
 * y ajustar parámetros (por ejemplo `MAX_USUARIOS`, duración) según el
 * entorno de prueba. Registrar logs y métricas para análisis posterior.
 */

/**
 * PRUEBA DE ESTRÉS PROGRESIVA
 * Incrementar usuarios gradualmente para encontrar el límite
 */
class StressTest_Progressive extends Simulation {
  
  // ===== AJUSTAR SEGÚN TU MÁQUINA =====
  val MAX_USUARIOS = 500       // Máximo de usuarios concurrentes
  val DURACION_TOTAL = 180     // Duración total en segundos (3 minutos)
  val PASOS = 5                // Número de escalones de carga
  
  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length)),
    "capacidad" -> (30 + scala.util.Random.nextInt(20)),
    "randomId" -> java.util.UUID.randomUUID().toString.take(8)
  ))

  val escenarioEstres = scenario("Prueba de Estrés Progresiva")
    .feed(estudiantesFeeder)
    .feed(conductorFeeder)
    .randomSwitch(
      30.0 -> exec(loginUsuario),
      25.0 -> exec(consultarViajes),
      20.0 -> exec(generarQR),
      10.0 -> exec(obtenerRutas),
      10.0 -> exec(registrarViaje),
      5.0  -> exec(actualizarRuta)
    )
    .pause(50.milliseconds, 200.milliseconds)

  setUp(
    escenarioEstres.inject(
      // Calentamiento
      nothingFor(5.seconds),
      // Escalón 1: 20% de carga
      rampUsersPerSec(1).to(MAX_USUARIOS * 0.2).during(15.seconds),
      constantUsersPerSec(MAX_USUARIOS * 0.2).during(30.seconds),
      // Escalón 2: 40% de carga
      rampUsersPerSec(MAX_USUARIOS * 0.2).to(MAX_USUARIOS * 0.4).during(15.seconds),
      constantUsersPerSec(MAX_USUARIOS * 0.4).during(30.seconds),
      // Escalón 3: 60% de carga
      rampUsersPerSec(MAX_USUARIOS * 0.4).to(MAX_USUARIOS * 0.6).during(15.seconds),
      constantUsersPerSec(MAX_USUARIOS * 0.6).during(30.seconds),
      // Escalón 4: 80% de carga
      rampUsersPerSec(MAX_USUARIOS * 0.6).to(MAX_USUARIOS * 0.8).during(15.seconds),
      constantUsersPerSec(MAX_USUARIOS * 0.8).during(30.seconds),
      // Escalón 5: 100% de carga (máximo estrés)
      rampUsersPerSec(MAX_USUARIOS * 0.8).to(MAX_USUARIOS).during(15.seconds),
      constantUsersPerSec(MAX_USUARIOS).during(30.seconds)
    ).protocols(httpConf)
  )
}

/**
 * PRUEBA DE PICO (SPIKE TEST)
 * Simula un pico repentino de carga
 */
class StressTest_Spike extends Simulation {
  
  val USUARIOS_PICO = 200
  
  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length)),
    "capacidad" -> (30 + scala.util.Random.nextInt(20)),
    "randomId" -> java.util.UUID.randomUUID().toString.take(8)
  ))

  val escenarioPico = scenario("Prueba de Pico (Spike)")
    .feed(estudiantesFeeder)
    .feed(conductorFeeder)
    .randomSwitch(
      30.0 -> exec(loginUsuario),
      25.0 -> exec(consultarViajes),
      20.0 -> exec(generarQR),
      10.0 -> exec(obtenerRutas),
      10.0 -> exec(registrarViaje),
      5.0  -> exec(actualizarRuta)
    )
    .pause(50.milliseconds, 150.milliseconds)

  setUp(
    escenarioPico.inject(
      // Carga normal inicial
      constantUsersPerSec(10).during(20.seconds),
      // SPIKE! Pico repentino
      atOnceUsers(USUARIOS_PICO),
      // Mantener carga alta
      constantUsersPerSec(USUARIOS_PICO).during(30.seconds),
      // Vuelta a la normalidad
      rampUsersPerSec(USUARIOS_PICO).to(10).during(10.seconds),
      constantUsersPerSec(10).during(20.seconds)
    ).protocols(httpConf)
  )
}

/**
 * PRUEBA DE RESISTENCIA (SOAK TEST)
 * Mantiene carga constante durante tiempo prolongado
 */
class StressTest_Soak extends Simulation {
  
  val USUARIOS_CONSTANTES = 50
  val DURACION_MINUTOS = 10  // Aumentar para pruebas más largas
  
  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length)),
    "capacidad" -> (30 + scala.util.Random.nextInt(20)),
    "randomId" -> java.util.UUID.randomUUID().toString.take(8)
  ))

  val escenarioResistencia = scenario("Prueba de Resistencia (Soak)")
    .feed(estudiantesFeeder)
    .feed(conductorFeeder)
    .randomSwitch(
      30.0 -> exec(loginUsuario),
      25.0 -> exec(consultarViajes),
      20.0 -> exec(generarQR),
      10.0 -> exec(obtenerRutas),
      10.0 -> exec(registrarViaje),
      5.0  -> exec(actualizarRuta)
    )
    .pause(100.milliseconds, 300.milliseconds)

  setUp(
    escenarioResistencia.inject(
      rampUsersPerSec(1).to(USUARIOS_CONSTANTES).during(30.seconds),
      constantUsersPerSec(USUARIOS_CONSTANTES).during(DURACION_MINUTOS.minutes)
    ).protocols(httpConf)
  )
}

/**
 * PRUEBA DE CAPACIDAD MÁXIMA
 * Encuentra el punto de quiebre del sistema
 */
class StressTest_Breakpoint extends Simulation {
  
  // Incrementa hasta encontrar el límite
  val USUARIOS_INICIO = 10
  val USUARIOS_FINAL = 1000
  val INCREMENTO_DURACION = 120  // segundos
  
  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length)),
    "capacidad" -> (30 + scala.util.Random.nextInt(20)),
    "randomId" -> java.util.UUID.randomUUID().toString.take(8)
  ))

  val escenarioCapacidad = scenario("Prueba de Punto de Quiebre")
    .feed(estudiantesFeeder)
    .feed(conductorFeeder)
    .randomSwitch(
      30.0 -> exec(loginUsuario),
      25.0 -> exec(consultarViajes),
      20.0 -> exec(generarQR),
      10.0 -> exec(obtenerRutas),
      10.0 -> exec(registrarViaje),
      5.0  -> exec(actualizarRuta)
    )
    .pause(50.milliseconds, 150.milliseconds)

  setUp(
    escenarioCapacidad.inject(
      rampUsersPerSec(USUARIOS_INICIO).to(USUARIOS_FINAL).during(INCREMENTO_DURACION.seconds)
    ).protocols(httpConf)
  ).assertions(
    // Registrar cuando el sistema falla
    global.failedRequests.percent.lte(50)  // Máximo 50% de fallos
  )
}

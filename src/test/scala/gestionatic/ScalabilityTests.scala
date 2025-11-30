package gestionatic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import gestionatic.Data._
import gestionatic.EndpointsAnalysis._

/**
 * =========================================================================
 * PRUEBAS DE ESCALABILIDAD - GestionaTIC API
 * =========================================================================
 * 
 * Simulaciones con diferentes niveles de carga:
 * - Iteración 1: 1 usuario (baseline)
 * - Iteración 2: 10 usuarios (carga ligera)
 * - Iteración 3: 100 usuarios (carga normal)
 * - Iteración 4: 500+ usuarios (carga alta - hacia 90% RAM)
 * 
 * INSTRUCCIONES PARA EJECUTAR:
 * 
 * Para ejecutar con diferente cantidad de usuarios, puedes:
 * 
 * 1. Usar variable de entorno:
 *    mvn gatling:test -Dgatling.simulationClass=gestionatic.ScalabilityTest -DUSERS=10
 * 
 * 2. Modificar directamente el valor USUARIOS en este archivo
 * 
 * CÓMO TOMAR EVIDENCIAS:
 * 1. Abrir Administrador de Tareas (Ctrl+Shift+Esc)
 * 2. Ir a pestaña "Rendimiento"
 * 3. Capturar pantalla ANTES y DURANTE cada prueba
 * 4. Registrar: Uso CPU%, Memoria RAM%, Tiempo respuesta
 */

class ScalabilityTest_01User extends Simulation {
  
  val USUARIOS = 1
  val DURACION = 30 // segundos
  
  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length)),
    "capacidad" -> (30 + scala.util.Random.nextInt(20)),
    "randomId" -> java.util.UUID.randomUUID().toString.take(8)
  ))

  val escenarioMixto = scenario(s"Escalabilidad - $USUARIOS Usuario(s)")
    .feed(estudiantesFeeder)
    .feed(conductorFeeder)
    .randomSwitch(
      30.0 -> exec(loginUsuario),      // 30% login
      25.0 -> exec(consultarViajes),   // 25% consultar viajes
      20.0 -> exec(generarQR),         // 20% generar QR
      10.0 -> exec(obtenerRutas),      // 10% obtener rutas
      10.0 -> exec(registrarViaje),    // 10% registrar viaje
      5.0  -> exec(actualizarRuta)     // 5% actualizar ruta
    )
    .pause(100.milliseconds, 500.milliseconds)

  setUp(
    escenarioMixto.inject(
      constantUsersPerSec(USUARIOS).during(DURACION.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.mean.lte(2000),
    global.failedRequests.percent.lte(10)
  )
}

class ScalabilityTest_10Users extends Simulation {
  
  val USUARIOS = 10
  val DURACION = 30
  
  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length)),
    "capacidad" -> (30 + scala.util.Random.nextInt(20)),
    "randomId" -> java.util.UUID.randomUUID().toString.take(8)
  ))

  val escenarioMixto = scenario(s"Escalabilidad - $USUARIOS Usuarios")
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
    .pause(100.milliseconds, 500.milliseconds)

  setUp(
    escenarioMixto.inject(
      rampUsersPerSec(1).to(USUARIOS).during(10.seconds),
      constantUsersPerSec(USUARIOS).during(DURACION.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.mean.lte(3000),
    global.failedRequests.percent.lte(15)
  )
}

class ScalabilityTest_100Users extends Simulation {
  
  val USUARIOS = 100
  val DURACION = 30
  
  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length)),
    "capacidad" -> (30 + scala.util.Random.nextInt(20)),
    "randomId" -> java.util.UUID.randomUUID().toString.take(8)
  ))

  val escenarioMixto = scenario(s"Escalabilidad - $USUARIOS Usuarios")
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
    .pause(100.milliseconds, 500.milliseconds)

  setUp(
    escenarioMixto.inject(
      rampUsersPerSec(1).to(USUARIOS).during(20.seconds),
      constantUsersPerSec(USUARIOS).during(DURACION.seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.mean.lte(5000),
    global.failedRequests.percent.lte(20)
  )
}

/**
 * =========================================================================
 * PRUEBA PARAMETRIZABLE - Uso general
 * =========================================================================
 * 
 * Ejecutar con diferentes valores:
 * mvn gatling:test -Dgatling.simulationClass=gestionatic.ScalabilityTestParametric -DUSERS=50 -DDURATION=60
 */
class ScalabilityTestParametric extends Simulation {
  
  // Parámetros configurables via línea de comandos
  val USUARIOS = Integer.getInteger("USERS", 1).toInt
  val DURACION = Integer.getInteger("DURATION", 30).toInt
  val RAMPA = Integer.getInteger("RAMP", 10).toInt
  
  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  val conductorFeeder = Iterator.continually(Map(
    "id_conductor" -> java.util.UUID.randomUUID().toString,
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length)),
    "capacidad" -> (30 + scala.util.Random.nextInt(20)),
    "randomId" -> java.util.UUID.randomUUID().toString.take(8)
  ))

  val escenarioMixto = scenario(s"Escalabilidad Paramétrica - $USUARIOS Usuarios")
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
    .pause(100.milliseconds, 500.milliseconds)

  setUp(
    escenarioMixto.inject(
      rampUsersPerSec(1).to(USUARIOS).during(RAMPA.seconds),
      constantUsersPerSec(USUARIOS).during(DURACION.seconds)
    ).protocols(httpConf)
  )
}

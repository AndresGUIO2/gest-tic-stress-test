package gestionatic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import gestionatic.Data._

/**
 * =========================================================================
 * ANÁLISIS DE ENDPOINTS CON ALTA TRANSACCIONALIDAD - GestionaTIC API
 * =========================================================================
 * 
 * Basado en el análisis de la colección Postman, se identificaron los 10 
 * endpoints más expuestos a alta transaccionalidad:
 * 
 * 📥 GET (2 endpoints):
 * 1. GET /estudiantes/viajes - Consulta frecuente de viajes por estudiante
 * 2. GET /admin/rutas - Consulta de rutas disponibles (dashboard admin)
 * 
 * 📤 POST (2 endpoints):
 * 3. POST /auth/login - Login de usuarios (muy frecuente)
 * 4. POST /estudiantes/generar-qr - Generación de QR por estudiante
 * 
 * 🔄 PUT (2 endpoints):
 * 5. PUT /admin/rutas - Actualización de rutas
 * 6. POST /conductores/registrar-viaje - Registro de viaje (funciona como update de saldo)
 * 
 * ❌ DELETE (2 endpoints):
 * 7. DELETE /admin/rutas - Eliminación de rutas
 * 8. POST /conductores/validar-qr - Invalidación de QR (funciona como soft delete)
 * 
 * 📊 MIXTOS (2 escenarios):
 * 9. Flujo completo estudiante: Login → Generar QR → Consultar viajes
 * 10. Flujo conductor: Login → Validar QR → Registrar viaje
 */

object EndpointsAnalysis {

  // ==================== CONFIGURACIÓN HTTP ====================
  val httpConf = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .shareConnections

  // ==================== REQUESTS INDIVIDUALES ====================

  // 1. GET - Consultar Viajes de Estudiante
  val consultarViajes = http("GET_ConsultarViajes")
    // La ruta publicada espera el id como path parameter: /estudiantes/{id_estudiante}/viajes
    .get("/estudiantes/${id_estudiante}/viajes")
    .check(status.in(200, 400, 404))

  // 2. GET - Obtener Rutas
  val obtenerRutas = http("GET_ObtenerRutas")
    .get("/admin/rutas")
    .check(status.in(200, 401, 403))

  // 3. POST - Login Usuario
  val loginUsuario = http("POST_Login")
    .post("/auth/login")
    .body(StringBody("""{"correo": "${correo}", "id_cognito": "${id_cognito}"}"""))
    .check(status.in(200, 400, 401, 404))

  // 4. POST - Generar QR
  val generarQR = http("POST_GenerarQR")
    .post("/estudiantes/generar-qr")
    .body(StringBody("""{"id_estudiante": "${id_estudiante}"}"""))
    // Puede devolver 200 o 201 dependiendo de implementación (creación del QR)
    .check(status.in(200, 201, 400, 404))
    .check(jsonPath("$.qr_data").optional.saveAs("qr_data"))

  // 5. PUT - Actualizar Ruta
  val actualizarRuta = http("PUT_ActualizarRuta")
    .put("/admin/rutas")
    .body(StringBody("""{
      "id_ruta": "${id_ruta}",
      "capacidad": ${capacidad},
      "estado": "activa"
    }"""))
    .check(status.in(200, 400, 403, 404))

  // 6. POST - Registrar Viaje Manual
  val registrarViaje = http("POST_RegistrarViaje")
    .post("/conductores/registrar-viaje")
    .body(StringBody("""{
      "id_estudiante": "${id_estudiante}",
      "id_conductor": "${id_conductor}",
      "id_ruta": "${id_ruta}"
    }"""))
    // Puede devolver 201 cuando el viaje es creado correctamente
    .check(status.in(200, 201, 400, 403, 404))

  // 7. DELETE - Eliminar Ruta
  val eliminarRuta = http("DELETE_EliminarRuta")
    .delete("/admin/rutas")
    .body(StringBody("""{"id_ruta": "${id_ruta_temp}"}"""))
    .check(status.in(200, 400, 403, 404))

  // 8. POST - Validar QR (funciona como invalidación/soft delete del QR)
  val validarQR = http("POST_ValidarQR")
    .post("/conductores/validar-qr")
    .body(StringBody("""{
      "qr_data": ${qr_data},
      "id_conductor": "${id_conductor}",
      "id_ruta": "${id_ruta}"
    }"""))
    .check(status.in(200, 400, 403, 404))

  // ==================== ENDPOINTS ADICIONALES PARA SETUP ====================

  // Crear Ruta (para pruebas que necesitan datos)
  val crearRuta = http("POST_CrearRuta")
    .post("/admin/rutas")
    .body(StringBody("""{
      "nombre": "Ruta Test ${randomId}",
      "origen": "Centro",
      "destino": "Universidad",
      "municipio": "Bogotá",
      "capacidad": 40,
      "horarios": ["07:00", "08:00", "17:00"]
    }"""))
    .check(status.in(200, 201, 400))
    .check(jsonPath("$.id_ruta").optional.saveAs("id_ruta_temp"))

  // Consultar Logs de Auditoría
  val consultarLogs = http("GET_ConsultarLogs")
    .get("/auditoria/logs")
    .queryParam("usuario_id", "${id_usuario}")
    .check(status.in(200, 400, 401))

  // Consultar Métricas
  val consultarMetricas = http("GET_ConsultarMetricas")
    .get("/reportes/metricas")
    .queryParam("tipo", "institucion")
    .queryParam("institucion", "Universidad Nacional")
    .check(status.in(200, 400))
}

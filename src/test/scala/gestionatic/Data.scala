package gestionatic
import io.gatling.core.Predef._

object Data {
  // URL base de la API GestionaTIC
  val baseUrl = "https://rjd8ahonmd.execute-api.us-east-1.amazonaws.com/Prod"
  
  // IDs de rutas disponibles en el sistema
  val rutaIds = Seq(
    "60145795-87cb-471d-96dd-69c44f120a7a",
    "340fcf85-733d-4bec-9f3b-a18214d90344"
  )
  
  // Municipios de prueba
  val municipios = Seq("Bogotá", "Medellín", "Cali", "Barranquilla")
  
  // Instituciones de prueba
  val instituciones = Seq(
    "Universidad Nacional",
    "Universidad de Antioquia", 
    "Universidad del Valle",
    "Universidad del Norte"
  )
  
  // Feeders para datos dinámicos
  val estudiantesFeeder = csv("estudiantes_test.csv").circular
  
  // Feeder para rutas
  val rutasFeeder = Iterator.continually(Map(
    "id_ruta" -> rutaIds(scala.util.Random.nextInt(rutaIds.length))
  ))
  
  // Headers comunes
  val jsonHeaders = Map(
    "Content-Type" -> "application/json",
    "Accept" -> "application/json"
  )
}

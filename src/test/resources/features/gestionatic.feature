# =========================================================================
# ESCENARIOS BDD PARA GESTIONATIC - LENGUAJE GHERKIN
# =========================================================================
# 
# Feature: Gestión de Transporte Estudiantil
#   Como sistema de transporte universitario
#   Quiero gestionar viajes y códigos QR de estudiantes
#   Para facilitar el control de acceso al servicio de transporte
#
# =========================================================================
# ESCENARIO 1: Login Exitoso de Estudiante
# =========================================================================
#
# @login @estudiante @positivo
# Scenario: Login exitoso de un estudiante registrado
#   Given un estudiante registrado con correo "test001@test.com"
#   And con id_cognito "cognito-test-001"
#   When el estudiante envía una solicitud de login
#   Then el sistema responde con código 200
#   And el tiempo de respuesta es menor a 2 segundos
#   And el sistema retorna los datos del usuario
#
# =========================================================================
# ESCENARIO 2: Generación de Código QR
# =========================================================================
#
# @qr @estudiante @positivo
# Scenario: Generación exitosa de código QR para abordar
#   Given un estudiante autenticado con id "90e9c04f-9e1e-4ad8-a76a-84c3645c78d1"
#   And el estudiante tiene saldo de viajes disponible
#   And el estudiante está asignado a una ruta activa
#   When el estudiante solicita generar un código QR
#   Then el sistema genera un QR único con formato UUID
#   And el QR incluye timestamp de generación
#   And el QR tiene expiración de 2 horas
#   And el tiempo de respuesta es menor a 3 segundos
#
# =========================================================================
# ESCENARIO 3: Validación de QR por Conductor
# =========================================================================
#
# @qr @conductor @validacion @positivo
# Scenario: Validación exitosa de QR al abordar el vehículo
#   Given un conductor autenticado en la ruta "60145795-87cb-471d-96dd-69c44f120a7a"
#   And un estudiante con QR válido generado hace menos de 2 horas
#   And el estudiante está asignado a la misma ruta del conductor
#   And el QR no ha sido usado previamente
#   When el conductor escanea y valida el código QR
#   Then el sistema descuenta 1 viaje del saldo del estudiante
#   And el sistema registra el viaje en el historial
#   And el QR queda invalidado para uso futuro
#   And el sistema responde con código 200
#
# =========================================================================
# ESCENARIO 4: Consulta de Historial de Viajes
# =========================================================================
#
# @viajes @estudiante @consulta @positivo
# Scenario: Consulta exitosa del historial de viajes
#   Given un estudiante autenticado con id "c25374f9-d428-454f-91c4-bf694c1a721f"
#   And el estudiante tiene viajes registrados en el sistema
#   When el estudiante consulta su historial de viajes
#   Then el sistema retorna la lista de viajes realizados
#   And cada viaje incluye fecha, hora, ruta y conductor
#   And el tiempo de respuesta es menor a 2 segundos
#   And los viajes están ordenados por fecha descendente
#
# =========================================================================
# ESCENARIO 5: Gestión de Rutas bajo Alta Carga
# =========================================================================
#
# @rutas @admin @carga @rendimiento
# Scenario Outline: Sistema mantiene rendimiento bajo carga concurrente
#   Given <usuarios> usuarios concurrentes accediendo al sistema
#   And cada usuario ejecuta operaciones mixtas (login, QR, consultas)
#   When los usuarios realizan solicitudes durante <duracion> segundos
#   Then el percentil 95 de tiempo de respuesta es menor a <max_tiempo> ms
#   And el porcentaje de errores es menor a <max_errores>%
#   # En arquitectura serverless, no se evalúa degradación de memoria ni CPU/RAM, solo rendimiento y errores
#
#   Examples:
#     | usuarios | duracion | max_tiempo | max_errores |
#     | 1        | 30       | 2000       | 5           |
#     | 10       | 30       | 3000       | 10          |
#     | 100      | 30       | 5000       | 15          |
#     | 500      | 60       | 8000       | 25          |
#
# =========================================================================

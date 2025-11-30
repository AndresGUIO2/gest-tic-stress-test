# =========================================================================
# SCRIPT DE EJECUCIÓN - PRUEBAS GESTIONATIC
# =========================================================================
# Ejecutar: .\ejecutar_pruebas.ps1
# =========================================================================

param(
    [string]$TipoPrueba = "todas",  # opciones: todas, endpoints, escalabilidad, estres, bdd
    [int]$Usuarios = 0              # para prueba paramétrica
)

$ProjectPath = "e:\Carpetas\Documentos\Trabajo\calidad\Gatling\GestTICSTest"
$ReportPath = "$ProjectPath\target\gatling"

Write-Host "==========================================================================" -ForegroundColor Cyan
Write-Host " PRUEBAS DE RENDIMIENTO - GESTIONATIC API" -ForegroundColor Cyan
Write-Host "==========================================================================" -ForegroundColor Cyan
Write-Host ""

# Cambiar al directorio del proyecto
Set-Location $ProjectPath

# Función para ejecutar prueba y mostrar resultado
function Ejecutar-Prueba {
    param([string]$NombreClase, [string]$Descripcion)
    
    Write-Host ""
    Write-Host ">>> Ejecutando: $Descripcion" -ForegroundColor Yellow
    Write-Host "    Clase: $NombreClase" -ForegroundColor Gray
    Write-Host ""
    
    & "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" gatling:test -Dgatling.simulationClass=$NombreClase
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ COMPLETADO: $Descripcion" -ForegroundColor Green
    } else {
        Write-Host "❌ ERROR: $Descripcion" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "Presione Enter para continuar..." -ForegroundColor Gray
    Read-Host
}

# Menú de opciones
function Mostrar-Menu {
    Write-Host ""
    Write-Host "Seleccione el tipo de prueba a ejecutar:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  1. Pruebas de Endpoints (10 pruebas individuales)" -ForegroundColor White
    Write-Host "  2. Pruebas de Escalabilidad (1, 10, 100 usuarios)" -ForegroundColor White
    Write-Host "  3. Pruebas de Estrés (hasta 90% recursos)" -ForegroundColor White
    # Opción 4 eliminada (BDD)
    Write-Host "  5. TODAS las pruebas" -ForegroundColor White
    Write-Host "  6. Prueba Paramétrica (usuarios personalizados)" -ForegroundColor White
    Write-Host "  0. Salir" -ForegroundColor White
    Write-Host ""
    
    $opcion = Read-Host "Opción"
    return $opcion
}

# Ejecutar según opción
function Ejecutar-Segun-Opcion {
    param([string]$Opcion)
    
    switch ($Opcion) {
        "1" {
            Write-Host "`n=== PRUEBAS DE ENDPOINTS ===" -ForegroundColor Magenta
            Ejecutar-Prueba "gestionatic.Test01_GET_ConsultarViajesTest" "Test 01: GET Consultar Viajes"
            Ejecutar-Prueba "gestionatic.Test02_GET_ObtenerRutasTest" "Test 02: GET Obtener Rutas"
            Ejecutar-Prueba "gestionatic.Test03_POST_LoginTest" "Test 03: POST Login"
            Ejecutar-Prueba "gestionatic.Test04_POST_GenerarQRTest" "Test 04: POST Generar QR"
            Ejecutar-Prueba "gestionatic.Test05_PUT_ActualizarRutaTest" "Test 05: PUT Actualizar Ruta"
            Ejecutar-Prueba "gestionatic.Test06_POST_RegistrarViajeTest" "Test 06: POST Registrar Viaje"
            Ejecutar-Prueba "gestionatic.Test07_DELETE_EliminarRutaTest" "Test 07: DELETE Eliminar Ruta"
            Ejecutar-Prueba "gestionatic.Test08_POST_ValidarQRTest" "Test 08: POST Validar QR"
            Ejecutar-Prueba "gestionatic.Test09_FlujoEstudianteTest" "Test 09: Flujo Estudiante"
            Ejecutar-Prueba "gestionatic.Test10_FlujoConductorTest" "Test 10: Flujo Conductor"
        }
        "2" {
            Write-Host "`n=== PRUEBAS DE ESCALABILIDAD ===" -ForegroundColor Magenta
            Ejecutar-Prueba "gestionatic.ScalabilityTest_01User" "Escalabilidad: 1 Usuario"
            Ejecutar-Prueba "gestionatic.ScalabilityTest_10Users" "Escalabilidad: 10 Usuarios"
            Ejecutar-Prueba "gestionatic.ScalabilityTest_100Users" "Escalabilidad: 100 Usuarios"
        }
        "3" {
            Write-Host "`n=== PRUEBAS DE ESTRÉS ===" -ForegroundColor Magenta
            Write-Host ""
            Write-Host "ADVERTENCIA: Estas pruebas pueden consumir muchos recursos." -ForegroundColor Red
            Write-Host "Monitoree el Administrador de Tareas (Ctrl+Shift+Esc)" -ForegroundColor Yellow
            Write-Host ""
            
            Ejecutar-Prueba "gestionatic.StressTest_Progressive" "Estrés: Progresivo"
            Ejecutar-Prueba "gestionatic.StressTest_Spike" "Estrés: Pico (Spike)"
            Ejecutar-Prueba "gestionatic.StressTest_Soak" "Estrés: Resistencia (Soak)"
        }
        # Opción 4 eliminada (BDD)
        "5" {
            Write-Host "`n=== EJECUTANDO TODAS LAS PRUEBAS ===" -ForegroundColor Magenta
            Ejecutar-Segun-Opcion "1"
            Ejecutar-Segun-Opcion "2"
            Ejecutar-Segun-Opcion "3"
        }
        "6" {
            $usuarios = Read-Host "Ingrese cantidad de usuarios"
            $duracion = Read-Host "Ingrese duración en segundos (default: 60)"
            if (-not $duracion) { $duracion = 60 }
            
            Write-Host "`n>>> Ejecutando prueba paramétrica con $usuarios usuarios por $duracion segundos" -ForegroundColor Yellow
            & "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" gatling:test -Dgatling.simulationClass=gestionatic.ScalabilityTestParametric "-DUSERS=$usuarios" "-DDURATION=$duracion"
        }
        "0" {
            Write-Host "Saliendo..." -ForegroundColor Gray
            exit
        }
        default {
            Write-Host "Opción no válida" -ForegroundColor Red
        }
    }
}

# Main loop
while ($true) {
    $opcion = Mostrar-Menu
    Ejecutar-Segun-Opcion $opcion
    
    Write-Host ""
    Write-Host "==========================================================================" -ForegroundColor Cyan
    Write-Host " REPORTES DISPONIBLES EN: $ReportPath" -ForegroundColor Cyan
    Write-Host "==========================================================================" -ForegroundColor Cyan
}

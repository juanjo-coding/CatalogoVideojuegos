import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.walk

fun main() {
    val carpetaDatos = Path.of("datos")
    val carpetaDatosIniciales = Path.of("datos", "datos_ini")
    val carpetaDatosFinales = Path.of("datos", "datos_fin")

    println("CREACION DE RUTAS PROYECTO\nCreando rutas...")

    if (!carpetaDatosIniciales.exists()) {
        println("Creación de ruta para DATOS_INI")
        carpetaDatosIniciales.createDirectories()
    }
    if (!carpetaDatosFinales.exists()) {
        println("Creación de ruta para DATOS_FIN")
        carpetaDatosFinales.createDirectories()
    }

    println("MOSTRANDO ESTRUCTURA DE DIRECTORIOS Y FICHEROS")

    val ficheroVideojuegos = Path.of("datos/datos_ini/videojuegos.csv")

    Files.walk(carpetaDatos).use { stream ->
        stream.sorted().forEach { path ->
            val profundidad = path.nameCount - carpetaDatos.nameCount
            val indentacion = " ".repeat(profundidad + 1)

            if (Files.isDirectory(path)) {
                println("$indentacion[DIR] ${path.fileName}")
            } else {
                println("$indentacion[FILE] ${path.fileName}")
            }
        }
    }
}


import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

fun main() {
    val carpetaDatos = Path.of("datos")
    val carpetaDatosIniciales = Path.of("datos", "datos_ini")
    val carpetaDatosFinales = Path.of("datos", "datos_fin")

    println("CREACION DE RUTAS PROYECTO\nCreando rutas...")

    if (!carpetaDatosIniciales.exists()) {
        println("Creacion de ruta para DATOS_INI")
        carpetaDatosIniciales.createDirectories()
    }
    if (!carpetaDatosFinales.exists()) {
        println("Creacion de ruta para DATOS_FIN")
        carpetaDatosFinales.createDirectories()
    }

    println("MOSTRANDO ESTRUCTURA DE DIRECTORIOS Y FICHEROS")

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

    val service = VideojuegoService()
    val repository = VideojuegoRepository()

    mostrarMenuPrincipal(service, repository)
}

fun mostrarMenuPrincipal(service: VideojuegoService, repository: VideojuegoRepository) {
    var opcion: Int

    do {
        println("\n===== CATALOGO DE VIDEOJUEGOS =====")
        println("1. Gestion CSV")
        println("2. Leer catalogo desde XML")
        println("0. Salir")
        println("====================================")
        print("Elige una opcion: ")

        opcion = readlnOrNull()?.toIntOrNull() ?: -1

        when (opcion) {
            1 -> {
                mostrarMenuCSV(service, repository)
            }

            2 -> {
                mostrarCatalogoXML(service, repository)
            }

            0 -> println("\nSaliendo del programa...")
            else -> println("\nOpcion no valida.")
        }
    } while (opcion != 0)
}

fun mostrarMenuCSV(service: VideojuegoService, repository: VideojuegoRepository) {
    var videojuegos = repository.leerTodos()
    var opcion: Int

    do {
        println("\n--- Gestion CSV ---")
        println("1. Listar todos los videojuegos")
        println("2. Buscar videojuego por ID")
        println("3. Anyadir nuevo videojuego")
        println("4. Modificar nota de un videojuego")
        println("5. Eliminar videojuego")
        println("0. Volver al menu principal")
        print("Elige una opcion: ")

        opcion = readlnOrNull()?.toIntOrNull() ?: -1

        when (opcion) {
            1 -> service.listarVideojuegos(videojuegos)
            2 -> {
                print("Introduce el ID del videojuego a buscar:")
                val id = readlnOrNull()?.toIntOrNull()
                if (id != null) {
                    service.buscarPorId(videojuegos, id)
                } else {
                    println("ERROR: El ID debe ser un numero entero.")
                }
            }

            3 -> {
                videojuegos = service.anyadirVideojuego(videojuegos)
                repository.guardarTodos(videojuegos)
            }

            4 -> {
                println("Introduzca el ID del videojuego cuya nota quieras modificar: ")
                val id = readlnOrNull()?.toIntOrNull()

                println("Introduce la nueva nota para el videojuego [0.0 - 10.0].")
                val nota = readlnOrNull()?.toDoubleOrNull()

                if (id != null && nota != null) {
                    videojuegos = service.modificarNota(videojuegos, id, nota)
                    repository.guardarTodos(videojuegos)
                } else {
                    println("ERROR: Los parametros introducidos no son validos.")
                }
            }

            5 -> {
                println("Introduce el ID del videojuego a eliminar:")
                val id = readlnOrNull()?.toIntOrNull()

                if (id != null) {
                    videojuegos = service.eliminarVideojuego(videojuegos, id)
                    repository.guardarTodos(videojuegos)
                } else {
                    println("ERROR: El ID introducido es invalido.")
                }
            }

            0 -> println("\nVolviendo al menu principal...")
            else -> println("\nOpcion no valida.")
        }
    } while (opcion != 0)
}

fun mostrarCatalogoXML(service: VideojuegoService, repository: VideojuegoRepository) {
    val rutaXML = Path.of("datos", "datos_ini", "videojuegos.xml")
    val videojuegosXML = repository.leerVideojuegosXML(rutaXML)

    if (videojuegosXML.isEmpty()) {
        println("No se han podido leer videojuegos del XML.")
        return
    }

    println("\n--- Videojuegos del fichero XML ---")
    service.listarVideojuegos(videojuegosXML)

    val catalogoActual = repository.leerTodos()
    var nuevos = 0

    for (juego in videojuegosXML) {
        val yaExiste = catalogoActual.find { it.id == juego.id } != null
        if (!yaExiste) {
            catalogoActual.add(juego)
            nuevos++
        }
    }

    println("\nVideojuegos nuevos integrados: $nuevos")
    repository.guardarTodos(catalogoActual)
}



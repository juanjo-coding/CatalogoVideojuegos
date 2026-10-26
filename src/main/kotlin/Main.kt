import java.nio.file.Files
import java.nio.file.Path

fun main() {
    val carpetaDatos = Path.of("datos")
    val carpetaDatosIniciales = Path.of("datos", "datos_ini")
    val carpetaDatosFinales = Path.of("datos", "datos_fin")

    println("CREACION DE RUTAS PROYECTO\nCreando rutas...")

    if (!Files.exists(carpetaDatosIniciales)) {
        println("Creacion de ruta para DATOS_INI")
        Files.createDirectories(carpetaDatosIniciales)
    }
    if (!Files.exists(carpetaDatosFinales)) {
        println("Creacion de ruta para DATOS_FIN")
        Files.createDirectories(carpetaDatosFinales)
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
        println("3. Leer catalogo desde JSON")
        println("4. Conversiones entre formatos")
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

            3 -> {
                mostrarCatalogoJSON(service, repository)
            }

            4 -> {
                mostrarMenuConversion(service, repository)
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
    val videojuegosXML = repository.leerXML(rutaXML)

    if (videojuegosXML.isEmpty()) {
        println("No se han podido leer videojuegos del XML.")
        return
    }

    println("\n--- Videojuegos del fichero XML ---")
    service.listarVideojuegos(videojuegosXML)

    val catalogoActual = repository.leerTodos()
    val nuevos = integrarCatalogo(catalogoActual, videojuegosXML)

    println("\nVideojuegos nuevos integrados: $nuevos")
    repository.guardarTodos(catalogoActual)
}

fun mostrarCatalogoJSON(service: VideojuegoService, repository: VideojuegoRepository) {
    val rutaJSON = Path.of("datos", "datos_ini", "videojuegos.json")
    val videojuegosJSON = repository.leerJSON(rutaJSON)

    if (videojuegosJSON.isEmpty()) {
        println("El JSON esta vacio.")
        return
    }

    println("\n--- Videojuegos del fichero JSON ---")
    service.listarVideojuegos(videojuegosJSON)

    val catalogoActual = repository.leerTodos()
    val nuevos = integrarCatalogo(catalogoActual, videojuegosJSON)

    println("\nVideojuegos nuevos integrados: $nuevos")
    repository.guardarTodos(catalogoActual)
}

fun integrarCatalogo(catalogoActual: MutableList<Videojuego>, nuevosVideojuegos: List<Videojuego>): Int {
    var nuevos = 0
    for (juego in nuevosVideojuegos) {
        val yaExiste = catalogoActual.find { it.id == juego.id } != null
        if (!yaExiste) {
            catalogoActual.add(juego)
            nuevos++
        }
    }
    return nuevos
}


fun mostrarMenuConversion(service: VideojuegoService, repository: VideojuegoRepository) {
    val origenCSV = Path.of("datos", "datos_ini", "videojuegos.csv")
    val origenXML = Path.of("datos", "datos_ini", "videojuegos.xml")
    val origenJSON = Path.of("datos", "datos_ini", "videojuegos.json")

    val destinoCSV = Path.of("datos", "datos_fin", "videojuegos.csv")
    val destinoXML = Path.of("datos", "datos_fin", "videojuegos.xml")
    val destinoJSON = Path.of("datos", "datos_fin", "videojuegos.json")

    var opcion: Int

    do {
        println("\n--- Conversiones ---")
        println("1. CSV -> JSON")
        println("2. CSV -> XML")
        println("3. JSON -> CSV")
        println("4. JSON -> XML")
        println("5. XML -> CSV")
        println("6. XML -> JSON")
        println("0. Volver")
        print("Elige una opcion: ")

        opcion = readlnOrNull()?.toIntOrNull() ?: -1

        when (opcion) {
            1 -> service.convertir(
                origenCSV,
                destinoJSON,
                { ruta -> repository.leerCSV(ruta) },
                { ruta, lista -> repository.escribirJSON(ruta, lista) }
            )

            2 -> service.convertir(
                origenCSV,
                destinoXML,
                { ruta -> repository.leerCSV(ruta) },
                { ruta, lista -> repository.escribirXML(ruta, lista) }
            )

            3 -> service.convertir(
                origenJSON,
                destinoCSV,
                { ruta -> repository.leerJSON(ruta) },
                { ruta, lista -> repository.escribirCSV(ruta, lista) }
            )

            4 -> service.convertir(
                origenJSON,
                destinoXML,
                { ruta -> repository.leerJSON(ruta) },
                { ruta, lista -> repository.escribirXML(ruta, lista) }
            )

            5 -> service.convertir(
                origenXML,
                destinoCSV,
                { ruta -> repository.leerXML(ruta) },
                { ruta, lista -> repository.escribirCSV(ruta, lista) }
            )

            6 -> service.convertir(
                origenXML,
                destinoJSON,
                { ruta -> repository.leerXML(ruta) },
                { ruta, lista -> repository.escribirJSON(ruta, lista) }
            )

            0 -> println("\nVolviendo al menu principal...")
            else -> println("\nOpcion no valida.")
        }
    } while (opcion != 0)
}




import java.nio.file.Files
import java.nio.file.Path

class VideojuegoService {

    fun listarVideojuegos(lista: List<Videojuego>) {
        if (lista.isEmpty()) {
            println("No hay videojuegos disponibles en el catalogo.")
        } else {
            println("\n--- Lista de Videojuegos ---")
            for (videojuego in lista) {
                println("ID: ${videojuego.id} | Titulo: ${videojuego.titulo} | Genero: ${videojuego.genero} | Anio: ${videojuego.anio} | Nota: ${videojuego.nota}")
            }
        }
    }

    fun buscarPorId(lista: List<Videojuego>, id: Int) {
        val videojuego = lista.find { it.id == id }

        if (videojuego != null) {
            println("\n--- Videojuego Encontrado ---")
            println("ID: ${videojuego.id}")
            println("Titulo: ${videojuego.titulo}")
            println("Genero: ${videojuego.genero}")
            println("Anio: ${videojuego.anio}")
            println("Nota: ${videojuego.nota}")
        } else {
            println("No existe ningun videojuego con el ID: $id")
        }
    }

    fun anyadirVideojuego(lista: MutableList<Videojuego>): MutableList<Videojuego> {
        val nuevoId = if (lista.isEmpty()) 1 else (lista.maxOf { it.id } + 1)
        println("ID asignado automaticamente: $nuevoId")

        print("Introduce el titulo: ")
        val titulo = readlnOrNull()?.trim() ?: ""

        print("Introduce el genero: ")
        val genero = readlnOrNull()?.trim() ?: ""

        print("Introduce el anio: ")
        val anio = readlnOrNull()?.toIntOrNull()

        print("Introduce la nota [0.0, 10.0]: ")
        val nota = readlnOrNull()?.toDoubleOrNull()

        if (titulo.isEmpty() || genero.isEmpty() || anio == null || nota == null) {
            println("ERROR: El formato introducido es invalido. Vuelva a intentarlo.")
            return lista
        }

        if (nota !in 0.0..10.0) {
            println("ERROR: La nota debe estar entre el 0.0 y el 10.0.")
            return lista
        }

        val nuevoVideojuego = Videojuego(nuevoId, titulo, genero, anio, nota)
        lista.add(nuevoVideojuego)
        println("Videojuego '${nuevoVideojuego.titulo}' anyadido con exito al catalogo.")
        return lista
    }

    fun modificarNota(lista: MutableList<Videojuego>, id: Int, nuevaNota: Double): MutableList<Videojuego> {
        if (nuevaNota !in 0.0..10.0) {
            println("ERROR: La nota debe estar entre 0.0 y 10.0.")
            return lista
        }

        var encontrado = false
        for (i in 0 until lista.size) {
            if (lista[i].id == id) {
                lista[i] = lista[i].copy(nota = nuevaNota)
                println("Nota del videojuego ${lista[i].titulo} actualizada a $nuevaNota.")
                encontrado = true
                break
            }
        }

        if (!encontrado) {
            println("ERROR: No existe ningun videojuego con el ID: $id")
        }

        return lista
    }

    fun eliminarVideojuego(lista: MutableList<Videojuego>, id: Int): MutableList<Videojuego> {
        val videojuego = lista.find { it.id == id }

        if (videojuego == null) {
            println("ERROR: No existe ningun videojuego con el ID: $id")
            return lista
        }

        print("Estas seguro de que deseas eliminar '${videojuego.titulo}'? (s/n): ")
        val confirmacion = readlnOrNull()?.trim()?.lowercase() ?: ""

        if (confirmacion == "s" || confirmacion == "si") {
            lista.remove(videojuego)
            println("Videojuego eliminado correctamente.")
        } else {
            println("Operacion cancelada. No se ha eliminado el videojuego.")
        }

        return lista
    }

    fun convertir(
        origen: Path,
        destino: Path,
        leer: (Path) -> List<Videojuego>,
        escribir: (Path, List<Videojuego>) -> Unit
    ) {
        if (!Files.exists(origen)) {
            println("Error: El fichero origen no existe en la ruta: $origen")
            return
        }

        try {
            val videojuegos = leer(origen)
            if (videojuegos.isEmpty()) {
                println("Aviso: No se han encontrado registros para convertir en: $origen")
                return
            }

            escribir(destino, videojuegos)

            println("\n--- Conversion realizada con exito ---")
            println("Fichero generado: $destino")
            println("Numero de registros convertidos: ${videojuegos.size}")
        } catch (e: Exception) {
            println("Error durante la conversion: ${e.message}")
        }
    }

    fun importarFicheroBinario(repository: VideojuegoRepository) {
        val rutaCSV = Path.of("datos", "datos_ini", "videojuegos.csv")
        val lista = repository.leerCSV(rutaCSV)
        if (lista.isEmpty()) {
            println("No se han encontrado datos en: $rutaCSV")
            return
        }
        repository.importarABinario(lista)
        println("Datos importados al fichero binario con exito.")
    }

    fun leerFicheroBinario(repository: VideojuegoRepository) {
        val lista = repository.leerBinario()
        if (lista.isEmpty()) {
            println("El fichero binario esta vacio o no existe.")
        } else {
            println("\n--- Catalogo desde Fichero BIN ---")
            listarVideojuegos(lista)
        }
    }

    fun anyadirRegistroBinario(repository: VideojuegoRepository) {
        var id: Int? = null
        while (id == null) {
            print("Introduce el ID: ")
            val num = readlnOrNull()?.trim()?.toIntOrNull()
            if (num == null) {
                println("ERROR: El ID debe ser un numero entero.")
            } else if (repository.buscarPorIdBinario(num) != null) {
                println("ERROR: El ID $num ya existe en el fichero binario.")
            } else {
                id = num
            }
        }

        print("Introduce el titulo: ")
        val titulo = readlnOrNull()?.trim() ?: ""

        print("Introduce el genero: ")
        val genero = readlnOrNull()?.trim() ?: ""

        var anio: Int? = null
        while (anio == null) {
            print("Introduce el anio: ")
            val num = readlnOrNull()?.trim()?.toIntOrNull()
            if (num == null) {
                println("ERROR: El anio debe ser un numero entero.")
            } else {
                anio = num
            }
        }

        var nota: Double? = null
        while (nota == null) {
            print("Introduce la nota [0.0 - 10.0]: ")
            val num = readlnOrNull()?.trim()?.toDoubleOrNull()
            if (num == null || num !in 0.0..10.0) {
                println("ERROR: La nota debe estar entre 0.0 y 10.0.")
            } else {
                nota = num
            }
        }

        val nuevo = Videojuego(id, titulo, genero, anio, nota)
        repository.anadirBinario(nuevo)
    }

    fun modificarRegistroBinario(repository: VideojuegoRepository) {
        var id: Int? = null
        while (id == null) {
            print("Introduce el ID del videojuego a modificar: ")
            val num = readlnOrNull()?.trim()?.toIntOrNull()
            if (num == null) {
                println("ERROR: El ID debe ser un numero entero.")
            } else {
                id = num
            }
        }

        val videojuego = repository.buscarPorIdBinario(id)
        if (videojuego == null) {
            println("No existe ningun videojuego con el ID: $id")
            return
        }

        println("Videojuego encontrado: ${videojuego.titulo}")

        var nuevaNota: Double? = null
        while (nuevaNota == null) {
            print("Introduce la nueva nota [0.0 - 10.0]: ")
            val num = readlnOrNull()?.trim()?.toDoubleOrNull()
            if (num == null || num !in 0.0..10.0) {
                println("ERROR: La nota debe estar entre 0.0 y 10.0.")
            } else {
                nuevaNota = num
            }
        }

        repository.modificarNotaBinario(id, nuevaNota)
    }

    fun eliminarRegistroBinario(repository: VideojuegoRepository) {
        var id: Int? = null
        while (id == null) {
            print("Introduce el ID del videojuego a eliminar: ")
            val num = readlnOrNull()?.trim()?.toIntOrNull()
            if (num == null) {
                println("ERROR: El ID debe ser un numero entero.")
            } else {
                id = num
            }
        }

        val videojuego = repository.buscarPorIdBinario(id)
        if (videojuego == null) {
            println("No existe ningun videojuego con el ID: $id")
            return
        }

        println("Videojuego encontrado: ${videojuego.titulo}")
        print("Estas seguro de que deseas eliminar '${videojuego.titulo}'? (s/n): ")
        val confirmacion = readlnOrNull()?.trim()?.lowercase() ?: ""

        if (confirmacion == "s" || confirmacion == "si") {
            repository.eliminarBinario(id)
        } else {
            println("Operacion cancelada. No se ha eliminado el videojuego.")
        }
    }
}
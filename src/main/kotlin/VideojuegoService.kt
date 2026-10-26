import java.nio.file.Files
import java.nio.file.Path

class VideojuegoService {

    fun listarVideojuegos(lista: List<Videojuego>) {
        if (lista.isEmpty()) {
            println("No hay videojuegos disponibles en el catalogo.")
        } else {
            println("\n--- Lista de Videojuegos ---")
            for (juego in lista) {
                println("ID: ${juego.id} | Titulo: ${juego.titulo} | Genero: ${juego.genero} | Anio: ${juego.anio} | Nota: ${juego.nota}")
            }
        }
    }

    fun buscarPorId(lista: List<Videojuego>, id: Int) {
        val juego = lista.find { it.id == id }

        if (juego != null) {
            println("\n--- Videojuego Encontrado ---")
            println("ID: ${juego.id}")
            println("Titulo: ${juego.titulo}")
            println("Genero: ${juego.genero}")
            println("Anio: ${juego.anio}")
            println("Nota: ${juego.nota}")
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
}
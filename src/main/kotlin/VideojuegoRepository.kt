import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class VideojuegoRepository(private val rutaCSV: Path = Path.of("datos", "datos_ini", "videojuegos.csv")) {

    fun leerTodos(): MutableList<Videojuego> {
        var videojuegos: MutableList<Videojuego> = mutableListOf()

        if (!Files.isReadable(rutaCSV)) {
            println("ERROR: No se puede leer el fichero en la ruta: $rutaCSV")
            return videojuegos
        }

        val reader = csvReader {
            delimiter = ';'
        }

        val filas: List<List<String>> = reader.readAll(rutaCSV.toFile())

        videojuegos = filas.mapNotNull { columnas ->
            if (columnas.size >= 5) {
                try {
                    val id = columnas[0].trim().toInt()
                    val titulo = columnas[1].trim()
                    val genero = columnas[2].trim()
                    val anio = columnas[3].trim().toInt()
                    val nota = columnas[4].trim().toDouble()
                    Videojuego(id, titulo, genero, anio, nota)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }.toMutableList()

        return videojuegos
    }

    fun guardarTodos(videojuegos: List<Videojuego>) {
        try {
            val fichero: File = rutaCSV.toFile()

            val filas: List<List<String>> = videojuegos.map { v ->
                listOf(
                    v.id.toString(),
                    v.titulo,
                    v.genero,
                    v.anio.toString(),
                    v.nota.toString()
                )
            }

            csvWriter {
                delimiter = ';'
            }.writeAll(filas, fichero)

            println("--- Fichero CSV actualizado con exito en: $fichero")
        } catch (e: Exception) {
            println("Error al escribir el fichero CSV: ${e.message}")
        }
    }

    fun leerVideojuegosXML(ruta: Path): List<Videojuego> {
        var contenedor = VideojuegosXML(emptyList())

        if (!Files.isReadable(ruta)) {
            println("ERROR: No se puede leer el fichero de la ruta: $ruta")
        } else {
            val fichero = ruta.toFile()
            val xmlMapper = XmlMapper().registerKotlinModule()

            contenedor = xmlMapper.readValue(fichero)
            println("--- Informacion leida con exito de: $ruta")
        }
        return contenedor.listaVideojuegos
    }
}
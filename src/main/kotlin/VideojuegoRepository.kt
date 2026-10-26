import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class VideojuegoRepository(private val rutaCSV: Path = Path.of("datos", "datos_ini", "videojuegos.csv")) {

    fun leerCSV(ruta: Path): List<Videojuego> {
        if (!Files.isReadable(ruta)) {
            println("ERROR: No se puede leer el fichero en la ruta: $ruta")
            return emptyList()
        }

        val reader = csvReader {
            delimiter = ';'
        }

        val filas: List<List<String>> = reader.readAll(ruta.toFile())

        return filas.mapNotNull { columnas ->
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
        }
    }

    fun escribirCSV(ruta: Path, videojuegos: List<Videojuego>) {
        try {
            val fichero: File = ruta.toFile()
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

            println("--- Fichero CSV guardado con exito en: $fichero")
        } catch (e: Exception) {
            println("Error al escribir el fichero CSV: ${e.message}")
        }
    }

    fun leerTodos(): MutableList<Videojuego> {
        return leerCSV(rutaCSV).toMutableList()
    }

    fun guardarTodos(videojuegos: List<Videojuego>) {
        escribirCSV(rutaCSV, videojuegos)
    }

    fun leerXML(ruta: Path): List<Videojuego> {
        var contenedor = VideojuegosXML(emptyList())

        if (!Files.isReadable(ruta)) {
            println("ERROR: No se puede leer el fichero de la ruta: $ruta")
            return emptyList()
        }

        try {
            val fichero = ruta.toFile()
            val xmlMapper = XmlMapper().registerKotlinModule()
            contenedor = xmlMapper.readValue(fichero)
            println("--- Informacion leida con exito de: $ruta")
        } catch (e: Exception) {
            println("Error al leer XML: ${e.message}")
        }

        return contenedor.listaVideojuegos
    }

    fun escribirXML(ruta: Path, videojuegos: List<Videojuego>) {
        try {
            val fichero = ruta.toFile()
            val contenedor = VideojuegosXML(videojuegos)
            val xmlMapper = XmlMapper().registerKotlinModule()
            val xmlString = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(contenedor)
            fichero.writeText(xmlString)
            println("--- Informacion guardada en XML: $fichero")
        } catch (e: Exception) {
            println("Error al guardar XML: ${e.message}")
        }
    }

    fun leerJSON(ruta: Path): List<Videojuego> {
        if (!Files.isReadable(ruta)) {
            println("ERROR: No se puede leer el fichero de la ruta: $ruta")
            return emptyList()
        }

        return try {
            val jsonString = Files.readString(ruta)
            val videojuegos = Json.decodeFromString<List<Videojuego>>(jsonString)
            println("--- Informacion leida con exito de: $ruta")
            videojuegos
        } catch (e: Exception) {
            println("Error al leer JSON: ${e.message}")
            emptyList()
        }
    }

    fun escribirJSON(ruta: Path, videojuegos: List<Videojuego>) {
        try {
            val jsonConfigurador = Json { prettyPrint = true }
            val jsonString = jsonConfigurador.encodeToString(videojuegos)
            Files.writeString(ruta, jsonString)
            println("--- Informacion guardada en JSON: $ruta")
        } catch (e: Exception) {
            println("Error al guardar JSON: ${e.message}")
        }
    }
}
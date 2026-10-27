import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val TAMANO_ID = Int.SIZE_BYTES
const val TAMANO_TITULO = 40
const val TAMANO_GENERO = 20
const val TAMANO_ANIO = Int.SIZE_BYTES
const val TAMANO_NOTA = Double.SIZE_BYTES
const val TAMANO_REGISTRO = TAMANO_ID + TAMANO_TITULO + TAMANO_GENERO + TAMANO_ANIO + TAMANO_NOTA

class VideojuegoRepository(
    private val rutaCSV: Path = Path.of("datos", "datos_ini", "videojuegos.csv"),
    val rutaBIN: Path = Path.of("datos", "datos_ini", "videojuegos.bin")
) {

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
        if (!Files.isReadable(ruta)) {
            println("ERROR: No se puede leer el fichero de la ruta: $ruta")
            return emptyList()
        }

        val contenedor = try {
            val fichero = ruta.toFile()
            val xmlMapper = XmlMapper().registerKotlinModule()
            val resultado: VideojuegosXML = xmlMapper.readValue(fichero)
            println("--- Informacion leida con exito de: $ruta")
            resultado
        } catch (e: Exception) {
            println("Error al leer XML: ${e.message}")
            VideojuegosXML(emptyList())
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

    fun vaciarCrearBinario(ruta: Path = rutaBIN) {
        try {
            if (ruta.parent != null && !Files.exists(ruta.parent)) {
                Files.createDirectories(ruta.parent)
            }
            FileChannel.open(
                ruta,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            ).close()
            println("--- El fichero '${ruta.fileName}' se ha creado y esta vacio.")
        } catch (e: Exception) {
            println("Error al vaciar o crear el fichero: ${e.message}")
        }
    }

    fun anadirBinario(videojuego: Videojuego, ruta: Path = rutaBIN) {
        try {
            if (ruta.parent != null && !Files.exists(ruta.parent)) {
                Files.createDirectories(ruta.parent)
            }
            FileChannel.open(
                ruta,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            ).use { canal ->
                val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)

                buffer.putInt(videojuego.id)

                val tituloBytes = videojuego.titulo
                    .padEnd(TAMANO_TITULO, ' ')
                    .toByteArray(Charsets.ISO_8859_1)
                buffer.put(tituloBytes, 0, TAMANO_TITULO)

                val generoBytes = videojuego.genero
                    .padEnd(TAMANO_GENERO, ' ')
                    .toByteArray(Charsets.ISO_8859_1)
                buffer.put(generoBytes, 0, TAMANO_GENERO)

                buffer.putInt(videojuego.anio)
                buffer.putDouble(videojuego.nota)

                buffer.flip()
                while (buffer.hasRemaining()) {
                    canal.write(buffer)
                }
                println("- Videojuego '${videojuego.titulo.trim()}' anyadido correctamente.")
            }
        } catch (e: Exception) {
            println("Error al anyadir el videojuego: ${e.message}")
        }
    }

    fun leerBinario(ruta: Path = rutaBIN): List<Videojuego> {
        val videojuegos = mutableListOf<Videojuego>()

        if (!Files.isReadable(ruta)) return emptyList()

        try {
            FileChannel.open(ruta, StandardOpenOption.READ).use { canal ->
                val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)

                while (canal.read(buffer) > 0) {
                    buffer.flip()

                    val id = buffer.getInt()

                    val tituloBytes = ByteArray(TAMANO_TITULO)
                    buffer.get(tituloBytes)
                    val titulo = String(tituloBytes, Charsets.ISO_8859_1).trim()

                    val generoBytes = ByteArray(TAMANO_GENERO)
                    buffer.get(generoBytes)
                    val genero = String(generoBytes, Charsets.ISO_8859_1).trim()

                    val anio = buffer.getInt()
                    val nota = buffer.getDouble()

                    videojuegos.add(Videojuego(id, titulo, genero, anio, nota))
                    buffer.clear()
                }
            }
        } catch (e: Exception) {
            println("Error al leer el fichero: ${e.message}")
        }
        return videojuegos
    }

    fun buscarPorIdBinario(idVideojuego: Int, ruta: Path = rutaBIN): Videojuego? {
        val lista = leerBinario(ruta)
        for (v in lista) {
            if (v.id == idVideojuego) {
                return v
            }
        }
        return null
    }

    fun modificarNotaBinario(idVideojuego: Int, nuevaNota: Double, ruta: Path = rutaBIN) {
        try {
            FileChannel.open(ruta, StandardOpenOption.READ, StandardOpenOption.WRITE).use { canal ->
                val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)
                var encontrado = false

                while (canal.read(buffer) > 0 && !encontrado) {
                    val posicionActual = canal.position()
                    buffer.flip()

                    val id = buffer.getInt()
                    if (id == idVideojuego) {
                        encontrado = true
                        val inicioRegistro = posicionActual - TAMANO_REGISTRO
                        val desplazamientoNota = TAMANO_ID + TAMANO_TITULO + TAMANO_GENERO + TAMANO_ANIO
                        val posicionNota = inicioRegistro + desplazamientoNota

                        canal.position(posicionNota)

                        val bufferNota = ByteBuffer.allocate(TAMANO_NOTA)
                        bufferNota.putDouble(nuevaNota)
                        bufferNota.flip()

                        while (bufferNota.hasRemaining()) {
                            canal.write(bufferNota)
                        }
                    }
                    buffer.clear()
                }

                if (encontrado) {
                    println("\n--- Nota del videojuego con ID $idVideojuego modificada correctamente a $nuevaNota")
                } else {
                    println("No se encontro ningun videojuego con el ID: $idVideojuego")
                }
            }
        } catch (e: Exception) {
            println("Error al modificar el registro: ${e.message}")
        }
    }

    fun eliminarBinario(idVideojuego: Int, ruta: Path = rutaBIN) {
        val pathTemporal = Path.of(ruta.toString() + ".tmp")
        var videojuegoEncontrado = false

        try {
            FileChannel.open(ruta, StandardOpenOption.READ).use { canalLectura ->
                FileChannel.open(
                    pathTemporal,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                ).use { canalEscritura ->
                    val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)

                    while (canalLectura.read(buffer) > 0) {
                        buffer.flip()
                        val id = buffer.getInt()

                        if (id == idVideojuego) {
                            videojuegoEncontrado = true
                        } else {
                            buffer.rewind()
                            canalEscritura.write(buffer)
                        }
                        buffer.clear()
                    }
                }
            }

            if (videojuegoEncontrado) {
                Files.move(pathTemporal, ruta, StandardCopyOption.REPLACE_EXISTING)
                println("\n**** Videojuego con ID $idVideojuego eliminado con exito.")
            } else {
                Files.deleteIfExists(pathTemporal)
                println("No se encontro el videojuego con ID: $idVideojuego")
            }
        } catch (e: Exception) {
            println("Error durante la eliminacion: ${e.message}")
        }
    }

    fun importarABinario(videojuegos: List<Videojuego>, ruta: Path = rutaBIN) {
        vaciarCrearBinario(ruta)
        for (v in videojuegos) {
            anadirBinario(v, ruta)
        }
    }
}
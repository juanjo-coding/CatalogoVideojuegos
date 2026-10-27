# Catálogo de Videojuegos

Gestión de videojuegos en consola (Kotlin) con CSV, JSON, XML y CRUD en fichero binario.

## 1. Estructura de datos

```kotlin
data class Videojuego(
    val id: Int,
    val titulo: String,
    val genero: String,
    val anio: Int,
    val nota: Double
)
```

- **id**: 4 bytes
- **titulo**: 40 bytes
- **genero**: 20 bytes
- **anio**: 4 bytes
- **nota**: 8 bytes
- **Total registro**: 76 bytes

## 2. Ejecución

- **Requisitos**: JDK 17 o superior.
- **Carpeta de datos**: `datos/` (el programa gestiona ahí los ficheros y crea sus subcarpetas automáticamente).
- **Arrancar**: Ejecutar `Main.kt`.

## 3. Funcionalidades

- **Gestión CSV**: Listar, buscar por ID, añadir, modificar nota y eliminar videojuegos.
- **Lectura XML y JSON**: Visualización e integración de registros en el catálogo.
- **Conversiones de formato**: Transformación directa entre CSV, JSON y XML (salida en `datos/datos_fin/`).
- **CRUD Fichero Binario (BIN)**:
    - Importar catálogo a fichero binario.
    - Leer registros del fichero binario.
    - Añadir nuevo registro.
    - Modificar registro por ID.
    - Eliminar registro por ID.

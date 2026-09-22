# Módulo de fotos y gestión de medios

## Decisión de diseño: se guardan URLs, no archivos binarios

La API **almacena la dirección (URL) de una imagen ya alojada en un
servicio externo**. No recibe, no guarda y no sirve archivos binarios.

En la base de datos, la tabla `fotos` tiene una columna `url` de tipo
`VARCHAR(500)`. No hay ninguna columna `BLOB`, ni carpeta de subidas, ni
endpoint que acepte `multipart/form-data`.

**Consecuencia práctica:** el cliente sube la imagen a donde quiera
(Cloudinary, S3, Imgur, un hosting propio) y recién después le manda a
esta API la URL resultante. Si el servicio externo borra la imagen, acá
queda una URL rota: la API valida el formato de la dirección, no que la
imagen exista.

### Qué haría falta para aceptar archivos

Si la consigna exigiera subir archivos en lugar de URLs, habría que:

1. Agregar un endpoint que reciba `multipart/form-data`
   (`@RequestParam MultipartFile`).
2. Definir dónde se guarda el binario: disco del servidor, un bucket
   externo, o una columna `@Lob` en MySQL (esta última desaconsejada,
   hace crecer la base y complica los backups).
3. Validar tipo de contenido real y tamaño máximo del archivo.
4. Publicar un endpoint que devuelva el binario, o generar URLs firmadas.

Mientras tanto, esta implementación cubre el requisito de "adjuntar
fotos" tratándolas como referencias.

## Organización del módulo

La gestión de imágenes vive aislada en sus propias clases:

| Clase | Rol |
|---|---|
| `FotoController` | Endpoints REST bajo `/api/productos/{productoId}/fotos`. Devuelve siempre `ResponseEntity`. |
| `FotoService` | Reglas de negocio: validación, límite, portada, alta y baja. |
| `FotoRepository` | Acceso a datos de `Foto`, siempre acotado al producto dueño. |
| `AgregarFotosDTO` | Cuerpo del alta de fotos. |
| `FotoRespuestaDTO` | Foto devuelta al cliente: `id`, `url` y `esPortada`. |

`ProductoService` no valida fotos: delega en `FotoService.reemplazarFotos()`
cuando se crea o se actualiza un producto. Así la lógica de imágenes
queda en un solo lugar.

### Excepciones propias

Ambas extienden `ApiException`, así las responde el
`GlobalExceptionHandler` que ya existía **sin tener que agregarle un
`@ExceptionHandler` nuevo**:

| Excepción | Estado | Cuándo |
|---|---|---|
| `FormatoFotoInvalidoException` | 400 | La URL no pasa el patrón de imagen, está vacía o supera los 500 caracteres. |
| `LimiteFotosExcedidoException` | 409 | El producto llegaría a más de 10 fotos. |

## Validaciones

### Formato de la URL (expresión regular)

```
^https?://[^\s/?#]+(?:/[^\s?#]*)?\.(?:jpg|jpeg|png)(?:\?[^\s#]*)?$
```

Exige protocolo `http` o `https`, un host, y que el recurso termine en
`.jpg`, `.jpeg` o `.png` (sin distinguir mayúsculas). Admite un query
string opcional, porque casi todas las CDN agregan parámetros de versión
o de recorte: `https://cdn.ejemplo.com/foto.jpg?v=2` es válida.

Quedan afuera, por ejemplo, `animacion.gif`, `foto` (sin extensión),
`ftp://host/foto.jpg` y cualquier texto que no sea una URL.

### Resto de las reglas

| Regla | Respuesta |
|---|---|
| La lista no puede venir vacía ni nula | 400 `El producto debe tener al menos una foto` |
| La URL no puede estar en blanco | 400 `La URL de la foto es obligatoria` |
| La URL no puede superar los 500 caracteres | 400 |
| No se puede repetir una URL en el mismo pedido | 400 `La foto ... está repetida` |
| No se puede cargar una URL que el producto ya tiene | 409 |
| Un producto no puede superar las 10 fotos | 409 |
| Un producto no puede quedarse sin fotos | 400 |

Las URLs se guardan con `trim()` aplicado.

## Imagen de portada

`Foto.esPortada` marca la imagen principal del producto. El invariante
es: **si el producto tiene fotos, exactamente una es la portada**.

- Al crear el producto, la primera foto de la lista queda como portada.
- Las fotos que se agregan después no le sacan la marca a la actual.
- Si se borra la portada, la primera foto que queda toma su lugar.
- `PATCH .../fotos/{fotoId}/portada` mueve la marca a la foto elegida.

## Endpoints

Todos cuelgan de `/api/productos/{productoId}/fotos`. Las operaciones de
escritura necesitan el token JWT y que el usuario sea el dueño del
producto.

| Método | Ruta | Éxito | Devuelve |
|---|---|---|---|
| `GET` | `/` | 200 | Lista de fotos |
| `POST` | `/` | 200 | Lista de fotos ya actualizada |
| `PATCH` | `/{fotoId}/portada?usuarioId=` | 200 | Lista de fotos ya actualizada |
| `DELETE` | `/{fotoId}?usuarioId=` | 204 | Sin cuerpo |

### Agregar fotos

```json
POST /api/productos/1/fotos

{
  "usuarioId": 1,
  "fotos": ["https://cdn.ejemplo.com/remera-espalda.jpg"]
}
```

Suma las fotos a las que el producto ya tiene, sin tocar el resto de los
datos. Errores posibles: **400** formato inválido, **403** si no es el
dueño, **404** si no existe el producto, **409** si la URL ya está
cargada o si se pasa del máximo.

### Eliminar una foto

```
DELETE /api/productos/1/fotos/3?usuarioId=1
```

**400** si es la única foto que queda, **403** si no es el dueño, **404**
si esa foto no pertenece al producto.

### Formato de las fotos en la respuesta

```json
[
  { "id": 1, "url": "https://cdn.ejemplo.com/a.jpg", "esPortada": true },
  { "id": 2, "url": "https://cdn.ejemplo.com/b.png", "esPortada": false }
]
```

El `id` es lo que permite borrar o destacar una foto puntual. En el alta
de un producto (`ProductoCrearDTO`) y en `AgregarFotosDTO` las fotos se
mandan como una lista de strings, porque todavía no tienen id.

## Relación JPA

```
Producto  1 ──────< N  Foto
```

- `Producto.fotos`: `@OneToMany(mappedBy = "producto", cascade = ALL,
  orphanRemoval = true, fetch = LAZY)`. La lista arranca inicializada,
  así nunca hay que chequear `null`.
- `Foto.producto`: `@ManyToOne(fetch = LAZY)` sobre la columna
  `producto_id`, que es `NOT NULL`.
- `orphanRemoval = true` borra de la base la foto que se saca de la
  lista; por eso `eliminarFoto` no necesita un `delete` explícito.
- `Producto.agregarFoto()` y `Producto.eliminarFoto()` mantienen
  sincronizados los dos lados de la relación. Conviene usarlos siempre
  en lugar de tocar la lista directamente.
- Las consultas de `ProductoRepository` usan
  `@EntityGraph(attributePaths = "fotos")` para traer las fotos en la
  misma consulta y evitar el N+1 al listar productos.
- `FotoRepository` siempre acota por producto
  (`findByIdAndProductoId`, `countByProductoId`), de modo que no se
  pueda tocar por id una foto que pertenece a otro producto.

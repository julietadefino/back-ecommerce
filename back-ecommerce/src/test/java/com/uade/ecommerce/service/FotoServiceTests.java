package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.exception.FormatoFotoInvalidoException;
import com.uade.ecommerce.exception.LimiteFotosExcedidoException;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.model.Foto;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.FotoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas del módulo de fotos: validación de URLs por expresión
 * regular, alta y baja individual, límite por producto y manejo de la
 * imagen de portada.
 */
@ExtendWith(MockitoExtension.class)
class FotoServiceTests {

    private static final Long ID_PRODUCTO = 1L;
    private static final Long ID_DUENIO = 10L;
    private static final Long ID_OTRO_USUARIO = 99L;

    private static final String URL_UNO =
            "https://cdn.ejemplo.com/remera-frente.jpg";
    private static final String URL_DOS =
            "https://cdn.ejemplo.com/remera-espalda.jpg";

    @Mock
    private FotoRepository fotoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private FotoService fotoService;

    private Usuario duenio;
    private Categoria categoria;

    @BeforeEach
    void prepararDatos() {
        duenio = new Usuario();
        duenio.setId(ID_DUENIO);
        duenio.setNombreUsuario("juan");

        categoria = new Categoria();
        categoria.setId(5L);
        categoria.setNombre("Indumentaria");
    }

    // ---------- Validación por expresión regular ----------

    @Test
    void aceptaLasExtensionesDeImagenPermitidas() {
        List<String> validas = List.of(
                "https://cdn.ejemplo.com/foto.jpg",
                "https://cdn.ejemplo.com/foto.jpeg",
                "https://cdn.ejemplo.com/foto.png",
                "http://cdn.ejemplo.com/foto.PNG",
                "https://cdn.ejemplo.com/ruta/larga/foto.JPG"
        );

        for (String url : validas) {
            assertEquals(url, fotoService.validarUrl(url));
        }
    }

    @Test
    void aceptaLaUrlConParametrosDeConsulta() {
        String url = "https://cdn.ejemplo.com/foto.jpg?v=2&w=800";

        assertEquals(url, fotoService.validarUrl(url));
    }

    @Test
    void rechazaLaExtensionQueNoEsDeImagen() {
        FormatoFotoInvalidoException error = assertThrows(
                FormatoFotoInvalidoException.class,
                () -> fotoService.validarUrl(
                        "https://cdn.ejemplo.com/animacion.gif"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertTrue(error.getMessage().contains(".jpg"));
    }

    @Test
    void rechazaElArchivoSinExtension() {
        assertThrows(
                FormatoFotoInvalidoException.class,
                () -> fotoService.validarUrl(
                        "https://cdn.ejemplo.com/foto"
                )
        );
    }

    @Test
    void rechazaElTextoQueNoEsUnaUrl() {
        assertThrows(
                FormatoFotoInvalidoException.class,
                () -> fotoService.validarUrl("no-es-una-url")
        );
    }

    @Test
    void rechazaLaUrlQueNoUsaHttpNiHttps() {
        assertThrows(
                FormatoFotoInvalidoException.class,
                () -> fotoService.validarUrl(
                        "ftp://cdn.ejemplo.com/foto.jpg"
                )
        );
    }

    @Test
    void rechazaLaUrlEnBlanco() {
        FormatoFotoInvalidoException error = assertThrows(
                FormatoFotoInvalidoException.class,
                () -> fotoService.validarUrl("   ")
        );

        assertEquals(
                "La URL de la foto es obligatoria",
                error.getMessage()
        );
    }

    @Test
    void rechazaLaUrlMasLargaQueElMaximoDeLaColumna() {
        String larga = "https://cdn.ejemplo.com/"
                + "a".repeat(Foto.MAX_LONGITUD_URL)
                + ".jpg";

        FormatoFotoInvalidoException error = assertThrows(
                FormatoFotoInvalidoException.class,
                () -> fotoService.validarUrl(larga)
        );

        assertTrue(error.getMessage().contains("500"));
    }

    @Test
    void normalizaLosEspaciosAlrededorDeLaUrl() {
        assertEquals(
                URL_UNO,
                fotoService.validarUrl("   " + URL_UNO + "   ")
        );
    }

    @Test
    void rechazaLaListaDeFotosVacia() {
        ApiException error = assertThrows(
                ApiException.class,
                () -> fotoService.validarUrls(List.of())
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(
                "El producto debe tener al menos una foto",
                error.getMessage()
        );
    }

    @Test
    void rechazaLaListaDeFotosNula() {
        assertThrows(
                ApiException.class,
                () -> fotoService.validarUrls(null)
        );
    }

    @Test
    void rechazaLasFotosRepetidasEnElMismoPedido() {
        ApiException error = assertThrows(
                ApiException.class,
                () -> fotoService.validarUrls(
                        Arrays.asList(URL_UNO, URL_UNO)
                )
        );

        assertTrue(error.getMessage().contains("repetida"));
    }

    // ---------- Reemplazo de fotos y portada ----------

    @Test
    void marcaLaPrimeraFotoComoPortadaAlReemplazar() {
        Producto producto = productoExistenteCon();

        fotoService.reemplazarFotos(
                producto,
                List.of(URL_UNO, URL_DOS)
        );

        assertEquals(2, producto.getFotos().size());
        assertTrue(producto.getFotos().get(0).isEsPortada());
        assertFalse(producto.getFotos().get(1).isEsPortada());
    }

    @Test
    void rechazaReemplazarConMasFotosQueElLimite() {
        Producto producto = productoExistenteCon();

        List<String> muchas = new ArrayList<>();

        for (int i = 0; i <= FotoService.MAX_FOTOS_POR_PRODUCTO; i++) {
            muchas.add("https://cdn.ejemplo.com/foto" + i + ".jpg");
        }

        LimiteFotosExcedidoException error = assertThrows(
                LimiteFotosExcedidoException.class,
                () -> fotoService.reemplazarFotos(producto, muchas)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    // ---------- Alta de fotos individuales ----------

    @Test
    void agregaLaFotoSinPisarLasQueYaTenia() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararAlta(producto, 1L);

        Producto actualizado = fotoService.agregarFotos(
                ID_PRODUCTO,
                ID_DUENIO,
                List.of(URL_DOS)
        );

        List<String> urls = actualizado.getFotos()
                .stream()
                .map(Foto::getUrl)
                .toList();

        assertEquals(List.of(URL_UNO, URL_DOS), urls);
    }

    @Test
    void laFotoAgregadaNoLeSacaLaPortadaALaPrimera() {
        Producto producto = productoExistenteCon(URL_UNO);
        producto.getFotos().get(0).setEsPortada(true);
        prepararAlta(producto, 1L);

        Producto actualizado = fotoService.agregarFotos(
                ID_PRODUCTO,
                ID_DUENIO,
                List.of(URL_DOS)
        );

        assertTrue(actualizado.getFotos().get(0).isEsPortada());
        assertFalse(actualizado.getFotos().get(1).isEsPortada());
    }

    @Test
    void dejaLaRelacionBidireccionalSincronizada() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararAlta(producto, 1L);

        Producto actualizado = fotoService.agregarFotos(
                ID_PRODUCTO,
                ID_DUENIO,
                List.of(URL_DOS)
        );

        Foto agregada = actualizado.getFotos().get(1);

        assertSame(actualizado, agregada.getProducto());
    }

    @Test
    void rechazaAgregarUnaFotoQueElProductoYaTiene() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararBusqueda(producto);
        when(fotoRepository.existsByProductoIdAndUrl(
                eq(ID_PRODUCTO), anyString()
        )).thenReturn(true);

        ApiException error = assertThrows(
                ApiException.class,
                () -> fotoService.agregarFotos(
                        ID_PRODUCTO,
                        ID_DUENIO,
                        List.of(URL_UNO)
                )
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void rechazaAgregarCuandoSeSuperaElLimite() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararBusqueda(producto);
        when(fotoRepository.existsByProductoIdAndUrl(
                eq(ID_PRODUCTO), anyString()
        )).thenReturn(false);
        when(fotoRepository.countByProductoId(ID_PRODUCTO))
                .thenReturn((long) FotoService.MAX_FOTOS_POR_PRODUCTO);

        LimiteFotosExcedidoException error = assertThrows(
                LimiteFotosExcedidoException.class,
                () -> fotoService.agregarFotos(
                        ID_PRODUCTO,
                        ID_DUENIO,
                        List.of(URL_DOS)
                )
        );

        assertTrue(error.getMessage().contains("10"));
    }

    @Test
    void rechazaQueOtroUsuarioAgregueFotos() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararBusqueda(producto);

        ApiException error = assertThrows(
                ApiException.class,
                () -> fotoService.agregarFotos(
                        ID_PRODUCTO,
                        ID_OTRO_USUARIO,
                        List.of(URL_DOS)
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    // ---------- Baja de fotos individuales ----------

    @Test
    void eliminaSolamenteLaFotoIndicada() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        prepararBusqueda(producto);
        prepararFoto(producto, 2L);
        prepararGuardado();

        fotoService.eliminarFoto(ID_PRODUCTO, 2L, ID_DUENIO);

        assertEquals(1, producto.getFotos().size());
        assertEquals(URL_UNO, producto.getFotos().get(0).getUrl());
    }

    @Test
    void promueveUnaNuevaPortadaAlBorrarLaQueLoEra() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        producto.getFotos().get(0).setEsPortada(true);
        prepararBusqueda(producto);
        prepararFoto(producto, 1L);
        prepararGuardado();

        fotoService.eliminarFoto(ID_PRODUCTO, 1L, ID_DUENIO);

        assertEquals(1, producto.getFotos().size());
        assertTrue(producto.getFotos().get(0).isEsPortada());
    }

    @Test
    void rechazaEliminarLaUnicaFotoDelProducto() {
        Producto producto = productoExistenteCon(URL_UNO);
        prepararBusqueda(producto);
        prepararFoto(producto, 1L);

        ApiException error = assertThrows(
                ApiException.class,
                () -> fotoService.eliminarFoto(
                        ID_PRODUCTO,
                        1L,
                        ID_DUENIO
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(1, producto.getFotos().size());
    }

    @Test
    void devuelve404SiLaFotoNoEsDeEseProducto() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        prepararBusqueda(producto);
        when(fotoRepository.findByIdAndProductoId(777L, ID_PRODUCTO))
                .thenReturn(Optional.empty());

        ApiException error = assertThrows(
                ApiException.class,
                () -> fotoService.eliminarFoto(
                        ID_PRODUCTO,
                        777L,
                        ID_DUENIO
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void rechazaQueOtroUsuarioElimineUnaFoto() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        prepararBusqueda(producto);

        ApiException error = assertThrows(
                ApiException.class,
                () -> fotoService.eliminarFoto(
                        ID_PRODUCTO,
                        1L,
                        ID_OTRO_USUARIO
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertEquals(2, producto.getFotos().size());
    }

    // ---------- Portada ----------

    @Test
    void destacaLaFotoElegidaYLeSacaLaMarcaALaAnterior() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        producto.getFotos().get(0).setEsPortada(true);
        prepararBusqueda(producto);
        prepararFoto(producto, 2L);
        prepararGuardado();

        Producto actualizado = fotoService.marcarPortada(
                ID_PRODUCTO,
                2L,
                ID_DUENIO
        );

        assertFalse(actualizado.getFotos().get(0).isEsPortada());
        assertTrue(actualizado.getFotos().get(1).isEsPortada());
    }

    @Test
    void rechazaQueOtroUsuarioCambieLaPortada() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        prepararBusqueda(producto);

        ApiException error = assertThrows(
                ApiException.class,
                () -> fotoService.marcarPortada(
                        ID_PRODUCTO,
                        2L,
                        ID_OTRO_USUARIO
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    // ---------- Listado ----------

    @Test
    void listaLasFotosDelProducto() {
        Producto producto = productoExistenteCon(URL_UNO, URL_DOS);
        prepararBusqueda(producto);
        when(fotoRepository.findByProductoIdOrderByIdAsc(ID_PRODUCTO))
                .thenReturn(producto.getFotos());

        List<Foto> fotos = fotoService.listar(ID_PRODUCTO);

        assertEquals(2, fotos.size());
        assertEquals(URL_UNO, fotos.get(0).getUrl());
    }

    @Test
    void devuelve404AlListarUnProductoInexistente() {
        when(productoRepository.findById(ID_PRODUCTO))
                .thenReturn(Optional.empty());

        ApiException error = assertThrows(
                ApiException.class,
                () -> fotoService.listar(ID_PRODUCTO)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    // ---------- Ayudantes ----------

    private Producto productoExistenteCon(String... urls) {
        Producto producto = new Producto();
        producto.setId(ID_PRODUCTO);
        producto.setNombre("Remera");
        producto.setDescripcion("Remera de algodón");
        producto.setPrecio(new BigDecimal("1500.00"));
        producto.setStock(10);
        producto.setUsuario(duenio);
        producto.setCategoria(categoria);
        producto.setFotos(new ArrayList<>());

        long id = 1L;

        for (String url : urls) {
            Foto foto = new Foto();
            foto.setId(id++);
            foto.setUrl(url);
            producto.agregarFoto(foto);
        }

        return producto;
    }

    private void prepararBusqueda(Producto producto) {
        when(productoRepository.findById(ID_PRODUCTO))
                .thenReturn(Optional.of(producto));
    }

    private void prepararGuardado() {
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void prepararFoto(Producto producto, Long fotoId) {
        Foto foto = producto.getFotos().stream()
                .filter(actual -> fotoId.equals(actual.getId()))
                .findFirst()
                .orElseThrow();

        when(fotoRepository.findByIdAndProductoId(fotoId, ID_PRODUCTO))
                .thenReturn(Optional.of(foto));
    }

    private void prepararAlta(Producto producto, long fotosActuales) {
        prepararBusqueda(producto);
        prepararGuardado();
        when(fotoRepository.existsByProductoIdAndUrl(
                eq(ID_PRODUCTO), anyString()
        )).thenReturn(false);
        when(fotoRepository.countByProductoId(ID_PRODUCTO))
                .thenReturn(fotosActuales);
    }
}

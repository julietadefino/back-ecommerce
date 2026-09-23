package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.CategoriaRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ProductoServiceTests {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Categoria categoria;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setNombre("Electrónica");
        categoria = categoriaRepository.save(categoria);

        usuario = new Usuario();
        usuario.setNombreUsuario("juanp");
        usuario.setMail("juan@mail.com");
        usuario.setContrasenia("1234");
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario = usuarioRepository.save(usuario);
    }

    private Producto nuevoProducto(String nombre, Double precio, Integer stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion("Descripción de " + nombre);
        producto.setPrecio(BigDecimal.valueOf(precio));
        producto.setStock(stock);
        return producto;
    }

    @Test
    void creaUnProductoValidoCorrectamente() {
        Producto guardado = productoService.crear(
                nuevoProducto("Mouse", 1500.0, 10),
                categoria.getId(),
                usuario.getId(),
                List.of("http://img/mouse.png")
        );

        assertNotNull(guardado.getId());
        assertEquals("Mouse", guardado.getNombre());
        assertEquals(categoria.getId(), guardado.getCategoria().getId());
        assertEquals(usuario.getId(), guardado.getUsuario().getId());
    }

    @Test
    void noPermiteCrearUnProductoConPrecioNegativo() {
        Producto producto = nuevoProducto("Teclado", -100.0, 5);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        producto,
                        categoria.getId(),
                        usuario.getId(),
                        List.of("http://img/teclado.png")
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void noPermiteCrearUnProductoConStockNegativo() {
        Producto producto = nuevoProducto("Monitor", 500.0, -1);

        assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        producto,
                        categoria.getId(),
                        usuario.getId(),
                        List.of("http://img/monitor.png")
                )
        );
    }

    @Test
    void noPermiteCrearUnProductoSinCategoria() {
        Producto producto = nuevoProducto("Auriculares", 800.0, 3);

        assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        producto,
                        null,
                        usuario.getId(),
                        List.of("http://img/auriculares.png")
                )
        );
    }

    @Test
    void noPermiteCrearUnProductoConUsuarioInexistente() {
        Producto producto = nuevoProducto("Webcam", 700.0, 4);

        assertThrows(
                ApiException.class,
                () -> productoService.crear(
                        producto,
                        categoria.getId(),
                        9999L,
                        List.of("http://img/webcam.png")
                )
        );
    }

    // ---------- Filtros ----------

    @Test
    void filtraPorCategoria() {
        Categoria otraCategoria = new Categoria();
        otraCategoria.setNombre("Hogar");
        otraCategoria = categoriaRepository.save(otraCategoria);

        productoService.crear(nuevoProducto("Mouse", 100.0, 5), categoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u1.jpg"));
        productoService.crear(nuevoProducto("Silla", 100.0, 5), otraCategoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u2.jpg"));

        List<Producto> resultado = productoService.buscar(categoria.getId(), null, null);

        assertEquals(1, resultado.size());
        assertEquals("Mouse", resultado.get(0).getNombre());
    }

    @Test
    void filtraPorNombreParcialSinImportarMayusculas() {
        productoService.crear(nuevoProducto("Mouse Inalámbrico", 100.0, 5), categoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u1.jpg"));
        productoService.crear(nuevoProducto("Teclado", 100.0, 5), categoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u1.jpg"));

        List<Producto> resultado = productoService.buscar(null, "mouse", null);

        assertEquals(1, resultado.size());
        assertEquals("Mouse Inalámbrico", resultado.get(0).getNombre());
    }

    @Test
    void filtraPorDisponibilidadDeStock() {
        productoService.crear(nuevoProducto("Con Stock", 100.0, 5), categoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u1.jpg"));
        productoService.crear(nuevoProducto("Sin Stock", 100.0, 0), categoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u2.jpg"));

        List<Producto> conStock = productoService.buscar(null, null, true);
        List<Producto> sinStock = productoService.buscar(null, null, false);

        assertEquals(1, conStock.size());
        assertEquals("Con Stock", conStock.get(0).getNombre());

        assertEquals(1, sinStock.size());
        assertEquals("Sin Stock", sinStock.get(0).getNombre());
    }

    @Test
    void buscaTodosOrdenadosAlfabeticamente() {
        productoService.crear(nuevoProducto("Zapatillas", 100.0, 5), categoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u1.jpg"));
        productoService.crear(nuevoProducto("Auriculares", 100.0, 5), categoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u2.jpg"));
        productoService.crear(nuevoProducto("Monitor", 100.0, 5), categoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u3.jpg"));

        List<Producto> resultado = productoService.buscar(null, null, null);

        List<String> nombres = resultado.stream().map(Producto::getNombre).toList();

        assertEquals(List.of("Auriculares", "Monitor", "Zapatillas"), nombres);
    }

    @Test
    void combinaFiltroDeCategoriaYNombre() {
        Categoria otraCategoria = new Categoria();
        otraCategoria.setNombre("Deportes");
        otraCategoria = categoriaRepository.save(otraCategoria);

        productoService.crear(nuevoProducto("Mouse Gamer", 100.0, 5), categoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u1.jpg"));
        productoService.crear(nuevoProducto("Mouse Pad", 100.0, 5), otraCategoria.getId(), usuario.getId(), List.of("https://img.ejemplo.com/u2.jpg"));

        List<Producto> resultado = productoService.buscar(categoria.getId(), "mouse", null);

        assertEquals(1, resultado.size());
        assertEquals("Mouse Gamer", resultado.get(0).getNombre());
    }

    // ---------- Actualizacion ----------

    @Test
    void actualizaUnProductoCorrectamente() {
        Categoria otraCategoria = new Categoria();
        otraCategoria.setNombre("Deportes");
        otraCategoria = categoriaRepository.save(otraCategoria);

        Producto creado = productoService.crear(
                nuevoProducto("Original", 100.0, 5),
                categoria.getId(),
                usuario.getId(),
                List.of("https://img.ejemplo.com/u1.jpg")
        );

        Producto nuevosDatos = nuevoProducto("Actualizado", 200.0, 10);

        Producto actualizado = productoService.actualizar(
                creado.getId(),
                usuario.getId(),
                nuevosDatos,
                otraCategoria.getId(),
                List.of("https://img.ejemplo.com/u2.jpg")
        );

        assertEquals("Actualizado", actualizado.getNombre());
        assertEquals(BigDecimal.valueOf(200.0),actualizado.getPrecio());
        assertEquals(10, actualizado.getStock());
        assertEquals(otraCategoria.getId(), actualizado.getCategoria().getId());
    }

    // ---------- Permisos ----------

    private Usuario nuevoUsuario(String nombreUsuario, String mail) {
        Usuario u = new Usuario();
        u.setNombreUsuario(nombreUsuario);
        u.setMail(mail);
        u.setContrasenia("1234");
        u.setNombre("Test");
        u.setApellido("User");
        return usuarioRepository.save(u);
    }

    @Test
    void noPermiteActualizarUnProductoDeOtroUsuario() {
        Usuario otroUsuario = nuevoUsuario("maria", "maria@mail.com");

        Producto creado = productoService.crear(
                nuevoProducto("Original", 100.0, 5),
                categoria.getId(),
                usuario.getId(),
                List.of("https://img.ejemplo.com/u1.jpg")
        );

        ApiException ex = assertThrows(
                ApiException.class,
                () -> productoService.actualizar(
                        creado.getId(),
                        otroUsuario.getId(),
                        nuevoProducto("Hackeado", 1.0, 1),
                        categoria.getId(),
                        List.of("https://img.ejemplo.com/u2.jpg")
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void noPermiteActualizarStockDeUnProductoDeOtroUsuario() {
        Usuario otroUsuario = nuevoUsuario("maria2", "maria2@mail.com");

        Producto creado = productoService.crear(
                nuevoProducto("Original", 100.0, 5),
                categoria.getId(),
                usuario.getId(),
                List.of("https://img.ejemplo.com/u1.jpg")
        );

        ApiException ex = assertThrows(
                ApiException.class,
                () -> productoService.actualizarStock(
                        creado.getId(),
                        otroUsuario.getId(),
                        20
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }
}

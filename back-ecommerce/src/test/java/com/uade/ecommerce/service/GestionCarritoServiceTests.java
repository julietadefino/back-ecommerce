package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.model.ItemCarrito;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.ItemCarritoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionCarritoServiceTests {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoService carritoService;

    private Usuario usuario;
    private Carrito carrito;
    private Producto producto;

    @BeforeEach
    void inicializar() {
        usuario = new Usuario();
        usuario.setId(1L);

        carrito = new Carrito();
        carrito.setId(10L);
        carrito.setUsuario(usuario);
        carrito.setItems(new ArrayList<>());

        producto = new Producto();
        producto.setId(5L);
        producto.setNombre("Teclado mecanico");
        producto.setStock(8);
    }

    private ItemCarrito crearItem(Long id, int cantidad) {
        ItemCarrito item = new ItemCarrito();
        item.setId(id);
        item.setCarrito(carrito);
        item.setProducto(producto);
        item.setCantidad(cantidad);

        carrito.getItems().add(item);

        return item;
    }

    private void simularCarritoDelUsuario() {
        when(carritoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(carrito));
    }

    private void simularGuardadoDelCarrito() {
        when(carritoRepository.save(carrito)).thenReturn(carrito);
    }

    @Test
    void acumulaLaCantidadCuandoElProductoYaEstaEnElCarrito() {
        ItemCarrito existente = crearItem(100L, 2);

        simularCarritoDelUsuario();
        simularGuardadoDelCarrito();
        when(productoRepository.findById(5L))
                .thenReturn(Optional.of(producto));
        when(itemCarritoRepository.findByCarritoIdAndProductoId(10L, 5L))
                .thenReturn(Optional.of(existente));

        Carrito resultado =
                carritoService.agregarProducto(1L, 5L, 3);

        assertEquals(1, resultado.getItems().size());
        assertEquals(5, existente.getCantidad());
        verify(itemCarritoRepository).save(existente);
    }

    @Test
    void agregaUnItemNuevoCuandoElProductoNoEstaEnElCarrito() {
        simularCarritoDelUsuario();
        simularGuardadoDelCarrito();
        when(productoRepository.findById(5L))
                .thenReturn(Optional.of(producto));
        when(itemCarritoRepository.findByCarritoIdAndProductoId(10L, 5L))
                .thenReturn(Optional.empty());

        Carrito resultado =
                carritoService.agregarProducto(1L, 5L, 4);

        assertEquals(1, resultado.getItems().size());
        assertEquals(4, resultado.getItems().get(0).getCantidad());
        assertEquals(producto, resultado.getItems().get(0).getProducto());
    }

    @Test
    void rechazaLaAcumulacionQueSuperaElStockDisponible() {
        ItemCarrito existente = crearItem(100L, 6);

        simularCarritoDelUsuario();
        when(productoRepository.findById(5L))
                .thenReturn(Optional.of(producto));
        when(itemCarritoRepository.findByCarritoIdAndProductoId(10L, 5L))
                .thenReturn(Optional.of(existente));

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> carritoService.agregarProducto(1L, 5L, 5)
        );

        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        assertEquals(6, existente.getCantidad());
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    void actualizaLaCantidadDeUnItemDelCarrito() {
        ItemCarrito item = crearItem(100L, 2);

        simularCarritoDelUsuario();
        simularGuardadoDelCarrito();
        when(itemCarritoRepository.findById(100L))
                .thenReturn(Optional.of(item));

        Carrito resultado =
                carritoService.actualizarCantidadItem(1L, 100L, 6);

        assertEquals(6, item.getCantidad());
        assertEquals(1, resultado.getItems().size());
        verify(itemCarritoRepository).save(item);
        verify(itemCarritoRepository, never()).delete(item);
    }

    @Test
    void eliminaElItemCuandoLaCantidadActualizadaEsCero() {
        ItemCarrito item = crearItem(100L, 3);

        simularCarritoDelUsuario();
        simularGuardadoDelCarrito();
        when(itemCarritoRepository.findById(100L))
                .thenReturn(Optional.of(item));

        Carrito resultado =
                carritoService.actualizarCantidadItem(1L, 100L, 0);

        assertTrue(resultado.getItems().isEmpty());
        verify(itemCarritoRepository).delete(item);
        verify(itemCarritoRepository, never()).save(item);
    }

    @Test
    void rechazaLaActualizacionConCantidadNegativa() {
        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> carritoService.actualizarCantidadItem(1L, 100L, -1)
        );

        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        verify(itemCarritoRepository, never()).save(any(ItemCarrito.class));
    }

    @Test
    void rechazaLaActualizacionSinCantidad() {
        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> carritoService.actualizarCantidadItem(1L, 100L, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        verify(itemCarritoRepository, never()).save(any(ItemCarrito.class));
    }

    @Test
    void rechazaLaActualizacionQueSuperaElStockDisponible() {
        ItemCarrito item = crearItem(100L, 2);

        simularCarritoDelUsuario();
        when(itemCarritoRepository.findById(100L))
                .thenReturn(Optional.of(item));

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> carritoService.actualizarCantidadItem(1L, 100L, 9)
        );

        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        assertEquals(2, item.getCantidad());
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    void rechazaLaActualizacionDeUnItemDeOtroCarrito() {
        Carrito otroCarrito = new Carrito();
        otroCarrito.setId(99L);
        otroCarrito.setItems(new ArrayList<>());

        ItemCarrito itemAjeno = new ItemCarrito();
        itemAjeno.setId(200L);
        itemAjeno.setCarrito(otroCarrito);
        itemAjeno.setProducto(producto);
        itemAjeno.setCantidad(1);

        simularCarritoDelUsuario();
        when(itemCarritoRepository.findById(200L))
                .thenReturn(Optional.of(itemAjeno));

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> carritoService.actualizarCantidadItem(1L, 200L, 2)
        );

        assertEquals(HttpStatus.FORBIDDEN, excepcion.getStatus());
        assertEquals(1, itemAjeno.getCantidad());
    }

    @Test
    void devuelve404CuandoElItemNoExiste() {
        simularCarritoDelUsuario();
        when(itemCarritoRepository.findById(300L))
                .thenReturn(Optional.empty());

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> carritoService.actualizarCantidadItem(1L, 300L, 2)
        );

        assertEquals(HttpStatus.NOT_FOUND, excepcion.getStatus());
    }

    @Test
    void eliminaUnItemDelCarrito() {
        ItemCarrito item = crearItem(100L, 2);
        ItemCarrito otroItem = crearItem(101L, 1);

        simularCarritoDelUsuario();
        simularGuardadoDelCarrito();
        when(itemCarritoRepository.findById(100L))
                .thenReturn(Optional.of(item));

        Carrito resultado = carritoService.eliminarItem(1L, 100L);

        assertEquals(List.of(otroItem), resultado.getItems());
        verify(itemCarritoRepository).delete(item);
    }

    @Test
    void vaciaTodosLosItemsDelCarrito() {
        crearItem(100L, 2);
        crearItem(101L, 4);

        simularCarritoDelUsuario();
        simularGuardadoDelCarrito();

        Carrito resultado = carritoService.vaciar(1L);

        assertTrue(resultado.getItems().isEmpty());
    }

    @Test
    void vaciarNoFallaCuandoElCarritoNoTieneItems() {
        carrito.setItems(null);

        simularCarritoDelUsuario();
        simularGuardadoDelCarrito();

        Carrito resultado = carritoService.vaciar(1L);

        assertTrue(resultado.getItems().isEmpty());
    }
}

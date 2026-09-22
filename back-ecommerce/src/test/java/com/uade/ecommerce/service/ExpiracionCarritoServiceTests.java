package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ItemCarritoNoEncontradoException;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.model.ItemCarrito;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.ItemCarritoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
class ExpiracionCarritoServiceTests {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoService carritoService;

    @Test
    void vaciaLosCarritosSinActividadEnLasUltimas24Horas() {
        Carrito carrito = crearCarritoConItem(25L);

        when(carritoRepository.findByUltimaActividadBefore(any()))
                .thenReturn(List.of(carrito));
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        carritoService.limpiarCarritosInactivos();

        assertTrue(carrito.getItems().isEmpty());
        verify(carritoRepository).save(carrito);
    }

    @Test
    void noTocaLosCarritosConActividadReciente() {
        Carrito carrito = crearCarritoConItem(1L);

        when(carritoRepository.findByUltimaActividadBefore(any()))
                .thenReturn(List.of());

        carritoService.limpiarCarritosInactivos();

        assertEquals(1, carrito.getItems().size());
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    void registraActividadAlAgregarUnProducto() {
        Carrito carrito = new Carrito();
        carrito.setId(10L);
        carrito.setItems(new ArrayList<>());
        carrito.setUltimaActividad(LocalDateTime.now().minusHours(5));

        Producto producto = new Producto();
        producto.setId(5L);
        producto.setStock(8);

        when(carritoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(carrito));
        when(carritoRepository.save(carrito)).thenReturn(carrito);
        when(productoRepository.findById(5L))
                .thenReturn(Optional.of(producto));
        when(itemCarritoRepository.findByCarritoIdAndProductoId(10L, 5L))
                .thenReturn(Optional.empty());

        carritoService.agregarProducto(1L, 5L, 2);

        assertTrue(carrito.getUltimaActividad()
                .isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void lanzaItemCarritoNoEncontradoExceptionCuandoElItemNoExiste() {
        Carrito carrito = new Carrito();
        carrito.setId(10L);
        carrito.setItems(new ArrayList<>());

        when(carritoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ItemCarritoNoEncontradoException.class,
                () -> carritoService.actualizarCantidadItem(1L, 999L, 2)
        );
    }

    private Carrito crearCarritoConItem(long horasDesdeUltimaActividad) {
        Carrito carrito = new Carrito();
        carrito.setId(10L);
        carrito.setItems(new ArrayList<>());
        carrito.setUltimaActividad(
                LocalDateTime.now().minusHours(horasDesdeUltimaActividad)
        );

        ItemCarrito item = new ItemCarrito();
        item.setId(100L);
        item.setCarrito(carrito);
        item.setCantidad(2);

        carrito.getItems().add(item);

        return carrito;
    }
}
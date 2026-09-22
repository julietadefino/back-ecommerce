package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.exception.ItemCarritoNoEncontradoException;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.model.ItemCarrito;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.ItemCarritoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Carrito getByUsuario(Long usuarioId) {
        Carrito carrito = buscarCarrito(usuarioId);
        carrito.registrarActividad();
        return carrito;
    }

    public Carrito agregarProducto(
            Long usuarioId,
            Long productoId,
            Integer cantidad
    ) {
        if (productoId == null) {
            throw ApiException.badRequest(
                    "El ID del producto es obligatorio"
            );
        }

        if (cantidad == null || cantidad <= 0) {
            throw ApiException.badRequest(
                    "La cantidad debe ser mayor que cero"
            );
        }

        Carrito carrito = buscarCarrito(usuarioId);

        Producto producto = productoRepository
                .findById(productoId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Producto no encontrado"
                        )
                );

        if (producto.getStock() <= 0) {
            throw ApiException.badRequest(
                    "El producto no tiene stock"
            );
        }

        ItemCarrito itemExistente =
                itemCarritoRepository
                        .findByCarritoIdAndProductoId(
                                carrito.getId(),
                                productoId
                        )
                        .orElse(null);

        if (itemExistente != null) {
            int nuevaCantidad =
                    itemExistente.getCantidad() + cantidad;

            validarStock(producto, nuevaCantidad);

            itemExistente.setCantidad(nuevaCantidad);
            itemCarritoRepository.save(itemExistente);
        } else {
            validarStock(producto, cantidad);

            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);

            obtenerItems(carrito).add(nuevoItem);
        }

        carrito.registrarActividad();
        return carritoRepository.save(carrito);
    }

    public Carrito eliminarItem(
            Long usuarioId,
            Long itemId
    ) {
        if (itemId == null) {
            throw ApiException.badRequest(
                    "El ID del ítem es obligatorio"
            );
        }

        Carrito carrito = buscarCarrito(usuarioId);

        ItemCarrito item = buscarItemDelCarrito(carrito, itemId);

        return quitarItem(carrito, item);
    }

    public Carrito actualizarCantidadItem(
            Long usuarioId,
            Long itemId,
            Integer cantidad
    ) {
        if (itemId == null) {
            throw ApiException.badRequest(
                    "El ID del ítem es obligatorio"
            );
        }

        if (cantidad == null) {
            throw ApiException.badRequest(
                    "La cantidad es obligatoria"
            );
        }

        if (cantidad < 0) {
            throw ApiException.badRequest(
                    "La cantidad no puede ser negativa"
            );
        }

        Carrito carrito = buscarCarrito(usuarioId);

        ItemCarrito item = buscarItemDelCarrito(carrito, itemId);

        if (cantidad == 0) {
            return quitarItem(carrito, item);
        }

        validarStock(item.getProducto(), cantidad);

        item.setCantidad(cantidad);
        itemCarritoRepository.save(item);

        carrito.registrarActividad();
        return carritoRepository.save(carrito);
    }

    public Carrito vaciar(Long usuarioId) {
        Carrito carrito = buscarCarrito(usuarioId);

        obtenerItems(carrito).clear();

        carrito.registrarActividad();
        return carritoRepository.save(carrito);
    }

        public BigDecimal checkout(Long usuarioId) {
        Carrito carrito = buscarCarrito(usuarioId);
        List<ItemCarrito> items = carrito.getItems();

        if (items == null || items.isEmpty()) {
            throw ApiException.badRequest(
                    "No se puede realizar el checkout " +
                    "porque el carrito está vacío"
            );
        }

        /*
         * Primero se valida el stock de todos los productos.
         * Si uno no tiene stock suficiente, la transacción
         * se cancela sin modificar ningún producto.
         */
        for (ItemCarrito item : items) {
            validarStock(
                    item.getProducto(),
                    item.getCantidad()
            );
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ItemCarrito item : items) {
            Producto producto = item.getProducto();

            total = total.add(
                    producto.getPrecio()
                            .multiply(BigDecimal.valueOf(item.getCantidad()))
            );

            producto.setStock(
                    producto.getStock()
                    - item.getCantidad()
            );

            productoRepository.save(producto);
        }

        carrito.getItems().clear();
        carrito.registrarActividad();
        carritoRepository.save(carrito);

        return total;
    }

    /**
     * Vacía los carritos cuya última actividad supera las 24 horas.
     * Corre automáticamente todos los días a las 03:00.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void limpiarCarritosInactivos() {
        LocalDateTime limite = LocalDateTime.now().minusHours(24);

        for (Carrito carrito
                : carritoRepository.findByUltimaActividadBefore(limite)) {
            List<ItemCarrito> items = carrito.getItems();

            if (items == null || items.isEmpty()) {
                continue;
            }

            items.clear();
            carrito.registrarActividad();
            carritoRepository.save(carrito);
        }
    }

    private List<ItemCarrito> obtenerItems(Carrito carrito) {
        if (carrito.getItems() == null) {
            carrito.setItems(new ArrayList<>());
        }

        return carrito.getItems();
    }

    private ItemCarrito buscarItemDelCarrito(
            Carrito carrito,
            Long itemId
    ) {
        ItemCarrito item = itemCarritoRepository
                .findById(itemId)
                .orElseThrow(() ->
                        new ItemCarritoNoEncontradoException(
                                "Ítem del carrito no encontrado"
                        )
                );

        if (item.getCarrito() == null
                || !item.getCarrito().getId()
                        .equals(carrito.getId())) {
            throw ApiException.forbidden(
                    "El ítem no pertenece al carrito del usuario"
            );
        }

        return item;
    }

    private Carrito quitarItem(
            Carrito carrito,
            ItemCarrito item
    ) {
        obtenerItems(carrito).remove(item);
        itemCarritoRepository.delete(item);

        carrito.registrarActividad();
        return carritoRepository.save(carrito);
    }

    private Carrito buscarCarrito(Long usuarioId) {
        if (usuarioId == null) {
            throw ApiException.badRequest(
                    "El ID del usuario es obligatorio"
            );
        }

        return carritoRepository
                .findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Carrito no encontrado para el usuario"
                        )
                );
    }

    private void validarStock(
            Producto producto,
            Integer cantidad
    ) {
        if (producto.getStock() < cantidad) {
            throw ApiException.badRequest(
                    "Stock insuficiente para el producto: "
                    + producto.getNombre()
            );
        }
    }
}
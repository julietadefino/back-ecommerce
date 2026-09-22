package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.CarritoVacioException;
import com.uade.ecommerce.exception.StockInsuficienteException;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.model.DetallePedido;
import com.uade.ecommerce.model.ItemCarrito;
import com.uade.ecommerce.model.OrdenCompra;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.OrdenRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrdenService {

    private final CarritoRepository carritoRepository;
    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;

    public OrdenService(
            CarritoRepository carritoRepository,
            OrdenRepository ordenRepository,
            ProductoRepository productoRepository
    ) {
        this.carritoRepository = carritoRepository;
        this.ordenRepository = ordenRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public OrdenCompra checkout(Long usuarioId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new CarritoVacioException(
                        "No existe un carrito para el usuario"
                ));

        List<ItemCarrito> items = carrito.getItems();
        if (items == null || items.isEmpty()) {
            throw new CarritoVacioException(
                    "No se puede realizar el checkout porque el carrito está vacío"
            );
        }

        Map<Long, Integer> cantidadesPorProducto = new LinkedHashMap<>();
        for (ItemCarrito item : items) {
            cantidadesPorProducto.merge(
                    item.getProducto().getId(),
                    item.getCantidad(),
                    Integer::sum
            );
        }

        Map<Long, Producto> productosDisponibles = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : cantidadesPorProducto.entrySet()) {
            Producto producto = productoRepository
                    .findByIdAndStockGreaterThanEqual(entry.getKey(), entry.getValue())
                    .orElseThrow(() -> new StockInsuficienteException(
                            "Stock insuficiente para el producto: " + entry.getKey()
                    ));
            productosDisponibles.put(entry.getKey(), producto);
        }

        OrdenCompra orden = new OrdenCompra();
        orden.setUsuario(carrito.getUsuario());
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setCodigoSeguimiento(generarCodigoSeguimiento());

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : items) {
            Producto producto = productosDisponibles.get(item.getProducto().getId());
            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotal);
            orden.agregarDetalle(detalle);

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
            total = total.add(subtotal);
        }

        orden.setTotal(total);
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return ordenRepository.save(orden);
    }

    @Transactional(readOnly = true)
    public List<OrdenCompra> obtenerHistorial(Long usuarioId) {
        return ordenRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    private String generarCodigoSeguimiento() {
        String codigo;
        do {
            codigo = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();
        } while (ordenRepository.existsByCodigoSeguimiento(codigo));
        return codigo;
    }
}

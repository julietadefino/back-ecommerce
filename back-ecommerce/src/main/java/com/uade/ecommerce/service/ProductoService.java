package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.CategoriaRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import com.uade.ecommerce.repository.ItemCarritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uade.ecommerce.exception.ProductoInvalidoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private FotoService fotoService;

    public List<Producto> getAll() {
        return productoRepository.findAllByOrderByNombreAsc();
    }

    public Optional<Producto> getById(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> getByCategoria(Long categoriaId) {
        return productoRepository
                .findByCategoriaIdOrderByNombreAsc(categoriaId);
    }


    public List<Producto> buscar(
            Long categoriaId,
            String nombre,
            Boolean conStock
    ) {
        String nombreFiltro = (nombre == null || nombre.isBlank())
                ? null
                : nombre.trim();

        return productoRepository.buscar(
                categoriaId,
                nombreFiltro,
                conStock
        );
    }
    
    public Page<Producto> buscarPaginado(
            Long categoriaId,
            String nombre,
            Boolean conStock,
            BigDecimal precioMin,
            BigDecimal precioMax,
            Pageable pageable
    ) {
        String nombreFiltro = (nombre == null || nombre.isBlank())
                ? null
                : nombre.trim();

        if (precioMin != null && precioMin.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductoInvalidoException("El precio minimo no puede ser negativo");
        }
        if (precioMax != null && precioMax.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductoInvalidoException("El precio maximo no puede ser negativo");
        }
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new ProductoInvalidoException("El precio minimo no puede ser mayor al precio maximo");
        }

        return productoRepository.buscarPaginado(
                categoriaId,
                nombreFiltro,
                conStock,
                precioMin,
                precioMax,
                pageable
        );
    }

    public Producto crear(
            Producto producto,
            Long categoriaId,
            Long usuarioId,
            List<String> urlsFotos
    ) {
        validarProducto(producto);
        validarIds(categoriaId, usuarioId);

        Categoria categoria = categoriaRepository
                .findById(categoriaId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Categoría no encontrada"
                        )
                );

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Usuario no encontrado"
                        )
                );

        producto.setId(null);
        producto.setNombre(producto.getNombre().trim());
        producto.setCategoria(categoria);
        producto.setUsuario(usuario);
        fotoService.reemplazarFotos(producto, urlsFotos);

        return productoRepository.save(producto);
    }

    public Producto actualizar(
            Long productoId,
            Long usuarioId,
            Producto nuevosDatos,
            Long categoriaId,
            List<String> urlsFotos
    ) {
        Producto producto = buscarProducto(productoId);

        validarPropietario(producto, usuarioId);
        validarProducto(nuevosDatos);
        validarIds(categoriaId, usuarioId);

        Categoria categoria = categoriaRepository
                .findById(categoriaId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Categoría no encontrada"
                        )
                );

        producto.setNombre(
                nuevosDatos.getNombre().trim()
        );
        producto.setDescripcion(
                nuevosDatos.getDescripcion().trim()
        );
        producto.setPrecio(nuevosDatos.getPrecio());
        producto.setStock(nuevosDatos.getStock());
        producto.setCategoria(categoria);

        fotoService.reemplazarFotos(producto, urlsFotos);

        return productoRepository.save(producto);
    }

    public Producto actualizarStock(
            Long productoId,
            Long usuarioId,
            Integer nuevoStock
    ) {
        if (nuevoStock == null || nuevoStock < 0) {
            throw new ProductoInvalidoException(
                    "El stock no puede ser negativo"
            );
        }

        Producto producto = buscarProducto(productoId);
        validarPropietario(producto, usuarioId);

        producto.setStock(nuevoStock);

        return productoRepository.save(producto);
    }

    public void eliminar(
            Long productoId,
            Long usuarioId
    ) {
        Producto producto = buscarProducto(productoId);
        validarPropietario(producto, usuarioId);

        itemCarritoRepository.deleteByProductoId(productoId);
        productoRepository.delete(producto);
    }

    private Producto buscarProducto(Long productoId) {
        if (productoId == null) {
            throw new ProductoInvalidoException(
                    "El ID del producto es obligatorio"
            );
        }

        return productoRepository.findById(productoId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Producto no encontrado"
                        )
                );
    }

    private void validarPropietario(
            Producto producto,
            Long usuarioId
    ) {
        if (usuarioId == null) {
            throw new ProductoInvalidoException(
                    "El ID del usuario es obligatorio"
            );
        }

        if (!producto.getUsuario().getId().equals(usuarioId)) {
            throw ApiException.forbidden(
                    "El usuario no puede modificar este producto"
            );
        }
    }

    private void validarProducto(Producto producto) {
        if (producto.getNombre() == null ||
                producto.getNombre().isBlank()) {
            throw new ProductoInvalidoException(
                    "El nombre del producto es obligatorio"
            );
        }

        if (producto.getDescripcion() == null ||
                producto.getDescripcion().isBlank()) {
            throw new ProductoInvalidoException(
                    "La descripción del producto es obligatoria"
            );
        }

        if (producto.getPrecio() == null ||
                producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductoInvalidoException(
                    "El precio no puede ser negativo"
            );
        }

        if (producto.getStock() == null ||
                producto.getStock() < 0) {
            throw new ProductoInvalidoException(
                    "El stock no puede ser negativo"
            );
        }
    }

    private void validarIds(
            Long categoriaId,
            Long usuarioId
    ) {
        if (categoriaId == null) {
            throw new ProductoInvalidoException(
                    "La categoría es obligatoria"
            );
        }

        if (usuarioId == null) {
            throw new ProductoInvalidoException(
                    "El usuario es obligatorio"
            );
        }
    }

            }

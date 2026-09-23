package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.exception.FormatoFotoInvalidoException;
import com.uade.ecommerce.exception.LimiteFotosExcedidoException;
import com.uade.ecommerce.model.Foto;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.repository.FotoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Gestión de las fotos de un producto.
 *
 * Concentra todo lo referido a imágenes: validación de las URLs, alta y
 * baja individual, límite por producto y elección de la portada. El
 * sistema guarda direcciones de imágenes ya alojadas, no archivos
 * binarios. Ver docs/FOTOS.md.
 */
@Service
@Transactional
public class FotoService {

    /** Máximo de fotos que admite un producto. */
    public static final int MAX_FOTOS_POR_PRODUCTO = 10;

    /**
     * URL de imagen válida: http o https, con host, y terminada en una
     * extensión aceptada. Admite un query string opcional, porque casi
     * todas las CDN agregan parámetros de versión o de recorte.
     */
    private static final Pattern URL_IMAGEN = Pattern.compile(
            "^https?://[^\\s/?#]+(?:/[^\\s?#]*)?\\.(?:jpg|jpeg|png)(?:\\?[^\\s#]*)?$",
            Pattern.CASE_INSENSITIVE
    );

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Foto> listar(Long productoId) {
        buscarProducto(productoId);

        return fotoRepository.findByProductoIdOrderByIdAsc(productoId);
    }

    /**
     * Agrega fotos sin tocar el resto del producto. Rechaza las URLs
     * que el producto ya tiene y corta si se pasa del máximo.
     */
    public Producto agregarFotos(
            Long productoId,
            Long usuarioId,
            List<String> urlsFotos
    ) {
        Producto producto = buscarProducto(productoId);
        validarPropietario(producto, usuarioId);

        List<String> urls = validarUrls(urlsFotos);

        for (String url : urls) {
            if (fotoRepository.existsByProductoIdAndUrl(productoId, url)) {
                throw ApiException.conflict(
                        "El producto ya tiene la foto " + url
                );
            }
        }

        long total = fotoRepository.countByProductoId(productoId)
                + urls.size();

        if (total > MAX_FOTOS_POR_PRODUCTO) {
            throw new LimiteFotosExcedidoException(
                    "El producto no puede superar las "
                            + MAX_FOTOS_POR_PRODUCTO
                            + " fotos"
            );
        }

        for (String url : urls) {
            producto.agregarFoto(crearFoto(url));
        }

        asegurarPortada(producto);

        return productoRepository.save(producto);
    }

    /**
     * Elimina una única foto, siempre que no sea la última. Si la foto
     * borrada era la portada, la primera que queda toma su lugar.
     */
    public void eliminarFoto(
            Long productoId,
            Long fotoId,
            Long usuarioId
    ) {
        Producto producto = buscarProducto(productoId);
        validarPropietario(producto, usuarioId);

        Foto foto = buscarFotoDelProducto(productoId, fotoId);

        if (producto.getFotos().size() == 1) {
            throw ApiException.badRequest(
                    "El producto debe conservar al menos una foto"
            );
        }

        producto.eliminarFoto(foto);

        asegurarPortada(producto);

        productoRepository.save(producto);
    }

    /**
     * Destaca una foto como imagen principal del producto y le saca la
     * marca a la que la tenía.
     */
    public Producto marcarPortada(
            Long productoId,
            Long fotoId,
            Long usuarioId
    ) {
        Producto producto = buscarProducto(productoId);
        validarPropietario(producto, usuarioId);

        Foto portada = buscarFotoDelProducto(productoId, fotoId);

        for (Foto foto : producto.getFotos()) {
            foto.setEsPortada(foto.getId().equals(portada.getId()));
        }

        return productoRepository.save(producto);
    }

    /**
     * Deja el producto únicamente con las fotos indicadas. Lo usa
     * ProductoService al crear y al actualizar un producto; las
     * anteriores se borran por orphanRemoval.
     */
    public void reemplazarFotos(
            Producto producto,
            List<String> urlsFotos
    ) {
        List<String> urls = validarUrls(urlsFotos);

        if (urls.size() > MAX_FOTOS_POR_PRODUCTO) {
            throw new LimiteFotosExcedidoException(
                    "El producto no puede superar las "
                            + MAX_FOTOS_POR_PRODUCTO
                            + " fotos"
            );
        }

        List<Foto> nuevas = new ArrayList<>();

        for (String url : urls) {
            nuevas.add(crearFoto(url));
        }

        producto.getFotos().clear();

        for (Foto foto : nuevas) {
            producto.agregarFoto(foto);
        }

        asegurarPortada(producto);
    }

    /**
     * Valida que la lista traiga al menos una URL utilizable y devuelve
     * las URLs ya normalizadas, sin repetidos.
     */
    public List<String> validarUrls(List<String> urlsFotos) {
        if (urlsFotos == null || urlsFotos.isEmpty()) {
            throw ApiException.badRequest(
                    "El producto debe tener al menos una foto"
            );
        }

        List<String> urls = new ArrayList<>();

        for (String url : urlsFotos) {
            String normalizada = validarUrl(url);

            if (urls.contains(normalizada)) {
                throw ApiException.badRequest(
                        "La foto " + normalizada + " está repetida"
                );
            }

            urls.add(normalizada);
        }

        return urls;
    }

    /**
     * Controla una URL suelta contra el patrón de imagen y el largo
     * máximo de la columna.
     */
    public String validarUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new FormatoFotoInvalidoException(
                    "La URL de la foto es obligatoria"
            );
        }

        String normalizada = url.trim();

        if (normalizada.length() > Foto.MAX_LONGITUD_URL) {
            throw new FormatoFotoInvalidoException(
                    "La URL de la foto no puede superar los "
                            + Foto.MAX_LONGITUD_URL
                            + " caracteres"
            );
        }

        if (!URL_IMAGEN.matcher(normalizada).matches()) {
            throw new FormatoFotoInvalidoException(
                    "La URL debe ser http o https y terminar en "
                            + ".jpg, .jpeg o .png: "
                            + normalizada
            );
        }

        return normalizada;
    }

    private Producto buscarProducto(Long productoId) {
        if (productoId == null) {
            throw ApiException.badRequest(
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

    private Foto buscarFotoDelProducto(
            Long productoId,
            Long fotoId
    ) {
        if (fotoId == null) {
            throw ApiException.badRequest(
                    "El ID de la foto es obligatorio"
            );
        }

        return fotoRepository
                .findByIdAndProductoId(fotoId, productoId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Foto no encontrada en el producto"
                        )
                );
    }

    private void validarPropietario(
            Producto producto,
            Long usuarioId
    ) {
        if (usuarioId == null) {
            throw ApiException.badRequest(
                    "El ID del usuario es obligatorio"
            );
        }

        if (!producto.getUsuario().getId().equals(usuarioId)) {
            throw ApiException.forbidden(
                    "El usuario no puede modificar este producto"
            );
        }
    }

    /**
     * Garantiza que siempre haya exactamente una portada: si ninguna
     * foto está marcada, toma la primera de la lista.
     */
    private void asegurarPortada(Producto producto) {
        List<Foto> fotos = producto.getFotos();

        if (fotos.isEmpty()) {
            return;
        }

        boolean hayPortada = fotos.stream()
                .anyMatch(Foto::isEsPortada);

        if (!hayPortada) {
            fotos.get(0).setEsPortada(true);
        }
    }

    private Foto crearFoto(String url) {
        Foto foto = new Foto();
        foto.setUrl(url);

        return foto;
    }
}

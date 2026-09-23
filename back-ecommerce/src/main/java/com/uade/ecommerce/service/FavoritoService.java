package com.uade.ecommerce.service;

import com.uade.ecommerce.dto.FavoritoDTO;
import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.exception.ProductoYaEnFavoritosException;
import com.uade.ecommerce.model.Favorito;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.FavoritoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public FavoritoService(
            FavoritoRepository favoritoRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository
    ) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<FavoritoDTO> listarFavoritosPorUsuario(Long usuarioId) {
        validarUsuarioId(usuarioId);

        if (!usuarioRepository.existsById(usuarioId)) {
            throw ApiException.notFound("Usuario no encontrado");
        }

        return favoritoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(FavoritoDTO::fromEntity)
                .toList();
    }

    public FavoritoDTO agregarFavorito(Long usuarioId, Long productoId) {
        validarIds(usuarioId, productoId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        ApiException.notFound("Usuario no encontrado")
                );

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        ApiException.notFound("Producto no encontrado")
                );

        if (favoritoRepository.existsByUsuarioIdAndProductoId(
                usuarioId,
                productoId
        )) {
            throw new ProductoYaEnFavoritosException(
                    "El producto ya se encuentra en la lista de favoritos"
            );
        }

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setProducto(producto);

        Favorito guardado = favoritoRepository.save(favorito);
        return FavoritoDTO.fromEntity(guardado);
    }

    public void vaciarFavoritos(Long usuarioId) {
        validarUsuarioId(usuarioId);

        if (!usuarioRepository.existsById(usuarioId)) {
            throw ApiException.notFound("Usuario no encontrado");
        }

        favoritoRepository.deleteByUsuarioId(usuarioId);
    }

    private void validarUsuarioId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw ApiException.badRequest(
                    "El ID del usuario debe ser un número positivo"
            );
        }
    }

    private void validarIds(Long usuarioId, Long productoId) {
        validarUsuarioId(usuarioId);

        if (productoId == null || productoId <= 0) {
            throw ApiException.badRequest(
                    "El ID del producto debe ser un número positivo"
            );
        }
    }
}
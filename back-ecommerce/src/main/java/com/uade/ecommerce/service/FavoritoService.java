package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.exception.ProductoYaEnFavoritosException;
import com.uade.ecommerce.model.Favorito;
import com.uade.ecommerce.model.Producto;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.FavoritoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Favorito> listarFavoritosPorUsuario(Long usuarioId) {
        validarUsuarioId(usuarioId);
        return favoritoRepository.findByUsuarioId(usuarioId);
    }

    public Favorito agregarFavorito(Long usuarioId, Long productoId) {
        validarIds(usuarioId, productoId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> ApiException.notFound("Producto no encontrado"));

   
        if (favoritoRepository.existsByUsuarioIdAndProductoId(usuarioId, productoId)) {
            throw new ProductoYaEnFavoritosException("El producto ya se encuentra en la lista de favoritos");
        }

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setProducto(producto);

        return favoritoRepository.save(favorito);
    }

    public void vaciarFavoritos(Long usuarioId) {
        validarUsuarioId(usuarioId);
        
  
        if (!usuarioRepository.existsById(usuarioId)) {
            throw ApiException.notFound("Usuario no encontrado");
        }

        favoritoRepository.deleteByUsuarioId(usuarioId);
    }

    private void validarUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            throw ApiException.badRequest("El ID del usuario es obligatorio");
        }
    }

    private void validarIds(Long usuarioId, Long productoId) {
        if (usuarioId == null) {
            throw ApiException.badRequest("El ID del usuario es obligatorio");
        }
        if (productoId == null) {
            throw ApiException.badRequest("El ID del producto es obligatorio");
        }
    }
}

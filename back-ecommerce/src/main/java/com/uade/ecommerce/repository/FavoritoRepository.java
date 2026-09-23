package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Favorito;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    // Lista de los favoritos de un usuario mediante las relaciones de producto y fotos
    @EntityGraph(attributePaths = {"producto", "producto.fotos", "usuario"})
    List<Favorito> findByUsuarioId(Long usuarioId);

    // Validar registros duplicados o reduntantes
    boolean existsByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    // Vaciar lista de favoritos
    void deleteByUsuarioId(Long usuarioId);
}

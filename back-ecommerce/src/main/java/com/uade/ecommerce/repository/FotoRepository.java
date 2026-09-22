package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {

    List<Foto> findByProductoIdOrderByIdAsc(Long productoId);

    Optional<Foto> findByIdAndProductoId(Long id, Long productoId);

    long countByProductoId(Long productoId);

    boolean existsByProductoIdAndUrl(Long productoId, String url);
}

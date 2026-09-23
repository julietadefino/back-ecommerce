package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Resenia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReseniaRepository extends JpaRepository<Resenia, Long> {

    List<Resenia> findByProductoId(Long productoId);

    boolean existsByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    @Query("SELECT COALESCE(AVG(r.calificacion), 0.0) FROM Resenia r WHERE r.productoId = :productoId")
    Double obtenerPromedioEstrellasPorProducto(@Param("productoId") Long productoId);
}
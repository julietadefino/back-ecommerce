package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.OrdenCompra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<OrdenCompra, Long> {

    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    List<OrdenCompra> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    boolean existsByCodigoSeguimiento(String codigoSeguimiento);
}

package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByOrderByNombreAsc();

    List<Producto> findByCategoriaIdOrderByNombreAsc(Long categoriaId);

    Optional<Producto> findByIdAndStockGreaterThanEqual(Long id, Integer stock);

    boolean existsByCategoriaId(Long categoriaId);
}
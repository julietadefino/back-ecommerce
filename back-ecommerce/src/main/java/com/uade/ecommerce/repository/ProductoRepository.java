package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @EntityGraph(attributePaths = "fotos")
    List<Producto> findAllByOrderByNombreAsc();

    @EntityGraph(attributePaths = "fotos")
    List<Producto> findByCategoriaIdOrderByNombreAsc(Long categoriaId);


    Optional<Producto> findByIdAndStockGreaterThanEqual(Long id, Integer stock);

    @EntityGraph(attributePaths = "fotos")
    Optional<Producto> findById(Long id);


    boolean existsByCategoriaId(Long categoriaId);

    // Busqueda combinada: cada parametro es opcional (puede venir null).
    // Si un parametro es null, esa condicion se ignora y no filtra nada.
    // conStock=true  -> solo productos con stock > 0
    // conStock=false -> solo productos sin stock (stock <= 0)
    // conStock=null  -> no filtra por disponibilidad
    @Query("SELECT p FROM Producto p WHERE " +
            "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) " +
            "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:conStock IS NULL " +
            "     OR (:conStock = TRUE AND p.stock > 0) " +
            "     OR (:conStock = FALSE AND p.stock <= 0)) " +
            "ORDER BY p.nombre ASC")
    List<Producto> buscar(
            @Param("categoriaId") Long categoriaId,
            @Param("nombre") String nombre,
            @Param("conStock") Boolean conStock
    );
}

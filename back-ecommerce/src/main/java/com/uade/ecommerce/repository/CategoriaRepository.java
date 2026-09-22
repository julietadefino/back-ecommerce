package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByActivoTrueOrderByNombreAsc();

    Optional<Categoria> findByIdAndActivoTrue(Long id);

    Optional<Categoria> findByNombreAndActivoTrue(String nombre);

    Optional<Categoria> findByNombreIgnoreCaseAndActivoTrue(
            String nombre
    );

    boolean existsByNombreIgnoreCase(String nombre);
}
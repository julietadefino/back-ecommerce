// repository/CarritoRepository.java
package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByUsuarioId(Long usuarioId);
    List<Carrito> findByUltimaActividadBefore(LocalDateTime limite);
}
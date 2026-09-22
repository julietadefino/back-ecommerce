package com.uade.ecommerce.repository;

import com.uade.ecommerce.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByMail(String mail);

    boolean existsByMail(String mail);

    boolean existsByNombreUsuario(String nombreUsuario);
}

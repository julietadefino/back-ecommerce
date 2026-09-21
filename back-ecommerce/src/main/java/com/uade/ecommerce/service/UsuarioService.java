package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.model.Rol;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario registrar(Usuario usuario) {
        validarUsuario(usuario);

        String mailNormalizado = usuario.getMail()
                .trim()
                .toLowerCase();

        String nombreUsuarioNormalizado =
                usuario.getNombreUsuario().trim();

        if (usuarioRepository.existsByMail(mailNormalizado)) {
            throw ApiException.conflict(
                    "Ya existe un usuario registrado con ese mail"
            );
        }

        if (usuarioRepository.existsByNombreUsuario(
                nombreUsuarioNormalizado)) {
            throw ApiException.conflict(
                    "El nombre de usuario ya está en uso"
            );
        }

        usuario.setId(null);
        usuario.setMail(mailNormalizado);
        usuario.setNombreUsuario(nombreUsuarioNormalizado);
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setApellido(usuario.getApellido().trim());
        usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
        usuario.setRol(Rol.USUARIO);

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        Carrito carrito = new Carrito();
        carrito.setUsuario(usuarioGuardado);
        carrito.setItems(new ArrayList<>());

        carritoRepository.save(carrito);

        return usuarioGuardado;
    }

    public Usuario login(String mail, String contrasenia) {
        if (mail == null || contrasenia == null) {
            throw ApiException.badRequest(
                    "El mail y la contraseña son obligatorios"
            );
        }

        Usuario usuario = usuarioRepository
                .findByMail(mail.trim().toLowerCase())
                .orElseThrow(() ->
                        ApiException.unauthorized(
                                "Usuario o contraseña incorrectos"
                        )
                );

        if (!passwordEncoder.matches(contrasenia, usuario.getContrasenia())) {
            throw ApiException.unauthorized(
                    "Usuario o contraseña incorrectos"
            );
        } // Condicion del Password Encoder

        return usuario;
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario.getNombreUsuario() == null ||
                usuario.getNombreUsuario().isBlank()) {
            throw ApiException.badRequest(
                    "El nombre de usuario es obligatorio"
            );
        }

        if (usuario.getMail() == null ||
                usuario.getMail().isBlank()) {
            throw ApiException.badRequest(
                    "El mail es obligatorio"
            );
        }

        if (usuario.getContrasenia() == null ||
                usuario.getContrasenia().isBlank()) {
            throw ApiException.badRequest(
                    "La contraseña es obligatoria"
            );
        }

        if (usuario.getNombre() == null ||
                usuario.getNombre().isBlank()) {
            throw ApiException.badRequest(
                    "El nombre es obligatorio"
            );
        }

        if (usuario.getApellido() == null ||
                usuario.getApellido().isBlank()) {
            throw ApiException.badRequest(
                    "El apellido es obligatorio"
            );
        }
    }
}

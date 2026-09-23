package com.uade.ecommerce.service;

import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.exception.UsuarioDuplicadoException;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.model.Rol;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Transactional
public class UsuarioService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final Duration VENTANA_DE_INTENTOS = Duration.ofMinutes(15);

    private final ConcurrentMap<String, EstadoIntentos> intentosFallidos =
            new ConcurrentHashMap<>();

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
                .toLowerCase(Locale.ROOT);

        String nombreUsuarioNormalizado =
                usuario.getNombreUsuario().trim();

        String claveMail = claveMail(mailNormalizado);
        String claveNombreUsuario = claveNombreUsuario(nombreUsuarioNormalizado);
        verificarBloqueo(claveMail);
        verificarBloqueo(claveNombreUsuario);

        if (usuarioRepository.existsByMail(mailNormalizado)) {
            registrarIntentoFallido(claveMail);
            throw new UsuarioDuplicadoException(
                    "Ya existe un usuario registrado con ese mail");
        }

        if (usuarioRepository.existsByNombreUsuario(
                nombreUsuarioNormalizado)) {
            registrarIntentoFallido(claveMail);
            registrarIntentoFallido(claveNombreUsuario);
            throw new UsuarioDuplicadoException(
                    "El nombre de usuario ya está en uso");
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

        reiniciarIntentos(claveMail, claveNombreUsuario);

        return usuarioGuardado;
    }

    public Usuario login(String mail, String contrasenia) {
        if (mail == null || contrasenia == null) {
            throw ApiException.badRequest(
                    "El mail y la contraseña son obligatorios"
            );
        }

        String mailNormalizado = mail.trim().toLowerCase(Locale.ROOT);
        String claveMail = claveMail(mailNormalizado);
        verificarBloqueo(claveMail);

        Usuario usuario = usuarioRepository
                .findByMail(mailNormalizado)
                .orElse(null);

        if (usuario == null ||
                !passwordEncoder.matches(contrasenia, usuario.getContrasenia())) {
            registrarIntentoFallido(claveMail);
            throw ApiException.unauthorized(
                    "Usuario o contraseña incorrectos"
            );
        } // Condicion del Password Encoder

        reiniciarIntentos(claveMail);

        return usuario;
    }

    private void verificarBloqueo(String clave) {
        Instant ahora = Instant.now();
        EstadoIntentos estado = intentosFallidos.get(clave);

        if (estado == null) {
            return;
        }

        if (!ahora.isBefore(estado.venceEn())) {
            intentosFallidos.remove(clave, estado);
            return;
        }

        if (estado.bloqueado()) {
            lanzarCuentaBloqueada();
        }
    }

    private void registrarIntentoFallido(String clave) {
        Instant ahora = Instant.now();
        EstadoIntentos estadoActualizado = intentosFallidos.compute(clave, (identificador, estado) -> {
            if (estado == null || !ahora.isBefore(estado.venceEn())) {
                return new EstadoIntentos(1, ahora.plus(VENTANA_DE_INTENTOS), false);
            }

            int intentos = estado.intentos() + 1;
            boolean bloqueado = intentos >= MAX_INTENTOS_FALLIDOS;
            return new EstadoIntentos(intentos, estado.venceEn(), bloqueado);
        });

        if (estadoActualizado.bloqueado()) {
            lanzarCuentaBloqueada();
        }
    }

    private void reiniciarIntentos(String... claves) {
        for (String clave : claves) {
            intentosFallidos.remove(clave);
        }
    }

    private String claveMail(String mail) {
        return "mail:" + mail;
    }

    private String claveNombreUsuario(String nombreUsuario) {
        return "usuario:" + nombreUsuario.toLowerCase(Locale.ROOT);
    }

    private void lanzarCuentaBloqueada() {
        throw new ApiException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Demasiados intentos fallidos. Intente nuevamente en 15 minutos"
        );
    }

    private record EstadoIntentos(
            int intentos,
            Instant venceEn,
            boolean bloqueado
    ) {
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

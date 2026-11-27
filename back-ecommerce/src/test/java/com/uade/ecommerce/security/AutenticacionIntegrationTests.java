package com.uade.ecommerce.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.ecommerce.model.Rol;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.auth.cookie-secure=false"
)
@ActiveProfiles("test")
class AutenticacionIntegrationTests {

    @Value("${local.server.port}")
    private int puerto;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final CookieManager cookies =
            new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    private final HttpClient cliente = HttpClient.newBuilder()
            .cookieHandler(cookies)
            .build();

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void registroLoginYAccesoConCookie() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        String mail = "test-" + sufijo + "@ejemplo.com";

        String registro = """
                {"nombreUsuario":"test-%s","mail":"%s",
                 "contrasenia":"secreto123",
                 "nombre":"Test","apellido":"Usuario"}
                """.formatted(sufijo, mail);

        assertEquals(
                401,
                enviar("GET", "/api/usuarios/me", null, null).statusCode()
        );

        HttpResponse<String> respuestaRegistro =
                postConCsrf("/api/usuarios/registro", registro);

        assertEquals(201, respuestaRegistro.statusCode());
        assertFalse(respuestaRegistro.body().contains("secreto123"));

        Usuario guardado =
                usuarioRepository.findByMail(mail).orElseThrow();

        assertNotEquals("secreto123", guardado.getContrasenia());
        assertTrue(passwordEncoder.matches(
                "secreto123",
                guardado.getContrasenia()
        ));
        assertEquals(Rol.USUARIO, guardado.getRol());

        assertEquals(
                409,
                postConCsrf("/api/usuarios/registro", registro).statusCode()
        );

        String loginIncorrecto = """
                {"mail":"%s","contrasenia":"incorrecta"}
                """.formatted(mail);

        assertEquals(
                401,
                postConCsrf("/api/usuarios/login", loginIncorrecto)
                        .statusCode()
        );

        String credenciales = """
                {"mail":"%s","contrasenia":"secreto123"}
                """.formatted(mail);

        HttpResponse<String> respuestaLogin =
                postConCsrf("/api/usuarios/login", credenciales);

        assertEquals(200, respuestaLogin.statusCode());

        JsonNode login = json.readTree(respuestaLogin.body());

        assertEquals(mail, login.path("usuario").path("mail").asText());
        assertFalse(login.has("token"));
        assertFalse(login.path("usuario").has("contrasenia"));

        String cookieLogin = respuestaLogin.headers()
                .allValues("Set-Cookie")
                .stream()
                .filter(valor -> valor.startsWith(
                        AuthCookieService.COOKIE_NAME + "="
                ))
                .findFirst()
                .orElseThrow();

        assertTrue(cookieLogin.contains("HttpOnly"));
        assertTrue(cookieLogin.contains("SameSite=Lax"));
        assertTrue(cookieLogin.contains("Path=/api"));
        assertTrue(cookieLogin.contains("Max-Age=3600"));

        // Una solicitud nueva recupera la sesión usando solo la cookie.
        HttpResponse<String> respuestaSesion =
                enviar("GET", "/api/usuarios/me", null, null);

        assertEquals(200, respuestaSesion.statusCode());

        JsonNode sesion = json.readTree(respuestaSesion.body());

        assertEquals(mail, sesion.path("usuario").path("mail").asText());
        assertEquals("USUARIO", sesion.path("access").asText());
        assertFalse(sesion.has("token"));

        // Un JWT alterado no autentica.
        assertEquals(
                401,
                enviar(
                        "GET",
                        "/api/usuarios/me",
                        null,
                        "token-invalido"
                ).statusCode()
        );

        HttpResponse<String> salida =
                postConCsrf("/api/usuarios/logout", null);

        assertEquals(204, salida.statusCode());
        assertTrue(salida.headers().allValues("Set-Cookie")
                .stream()
                .anyMatch(valor ->
                        valor.startsWith(
                                AuthCookieService.COOKIE_NAME + "="
                        ) && valor.contains("Max-Age=0")
                ));

        assertEquals(
                401,
                enviar("GET", "/api/usuarios/me", null, null).statusCode()
        );
    }

    @Test
    void registroSinCsrfEsRechazado() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        String mail = "csrf-" + sufijo + "@ejemplo.com";

        String registro = """
                {"nombreUsuario":"csrf-%s","mail":"%s",
                 "contrasenia":"secreto123",
                 "nombre":"Test","apellido":"Usuario"}
                """.formatted(sufijo, mail);

        HttpResponse<String> respuesta =
                enviar("POST", "/api/usuarios/registro", registro, null);

        assertTrue(
                respuesta.statusCode() >= 400
                        && respuesta.statusCode() < 500,
                "El registro sin CSRF debe ser rechazado"
        );

        assertTrue(usuarioRepository.findByMail(mail).isEmpty());
    }

    private HttpResponse<String> postConCsrf(
            String ruta,
            String cuerpo
    ) throws Exception {
        HttpResponse<String> respuestaCsrf =
                enviar("GET", "/api/usuarios/csrf", null, null);

        assertEquals(200, respuestaCsrf.statusCode());

        JsonNode csrf = json.readTree(respuestaCsrf.body());

        HttpRequest.Builder solicitud = HttpRequest.newBuilder()
                .uri(uri(ruta))
                .header(
                        csrf.path("headerName").asText(),
                        csrf.path("token").asText()
                );

        if (cuerpo != null) {
            solicitud.header("Content-Type", "application/json");
        }

        solicitud.POST(cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo));

        return cliente.send(
                solicitud.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private HttpResponse<String> enviar(
            String metodo,
            String ruta,
            String cuerpo,
            String token
    ) throws Exception {
        HttpRequest.Builder solicitud =
                HttpRequest.newBuilder().uri(uri(ruta));

        if (token != null) {
            solicitud.header("Authorization", "Bearer " + token);
        }

        if (cuerpo != null) {
            solicitud.header("Content-Type", "application/json");
        }

        solicitud.method(
                metodo,
                cuerpo == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(cuerpo)
        );

        return cliente.send(
                solicitud.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private URI uri(String ruta) {
        return URI.create("http://localhost:" + puerto + ruta);
    }
}
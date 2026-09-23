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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AutenticacionIntegrationTests {

    @Value("${local.server.port}")
    private int puerto;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void registroLoginYAccesoConJwt() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        String mail = "test-" + sufijo + "@ejemplo.com";
        String registro = """
                {"nombreUsuario":"test-%s","mail":"%s","contrasenia":"secreto123",
                 "nombre":"Test","apellido":"Usuario"}
                """.formatted(sufijo, mail);

        HttpResponse<String> sinToken = enviar("GET", "/api/usuarios", null, null);
        assertEquals(401, sinToken.statusCode());

        HttpResponse<String> respuestaRegistro = enviar("POST", "/api/usuarios/registro", registro, null);
        assertEquals(201, respuestaRegistro.statusCode());
        assertFalse(respuestaRegistro.body().contains("secreto123"));

        Usuario guardado = usuarioRepository.findByMail(mail).orElseThrow();
        assertNotEquals("secreto123", guardado.getContrasenia());
        assertTrue(passwordEncoder.matches("secreto123", guardado.getContrasenia()));
        assertEquals(Rol.USUARIO, guardado.getRol());

        assertEquals(409, enviar("POST", "/api/usuarios/registro", registro, null).statusCode());
        assertEquals(401, enviar("POST", "/api/usuarios/login",
                "{\"mail\":\"" + mail + "\",\"contrasenia\":\"incorrecta\"}", null).statusCode());

        HttpResponse<String> respuestaLogin = enviar("POST", "/api/usuarios/login",
                "{\"mail\":\"" + mail + "\",\"contrasenia\":\"secreto123\"}", null);
        assertEquals(200, respuestaLogin.statusCode());
        JsonNode login = json.readTree(respuestaLogin.body());
        assertEquals("Bearer", login.get("tipo").asText());
        assertEquals(mail, login.get("usuario").get("mail").asText());
        assertFalse(login.get("usuario").has("contrasenia"));

        String token = login.get("token").asText();
        assertEquals(200, enviar("GET", "/api/usuarios/" + guardado.getId(), null, token).statusCode());
        assertEquals(401, enviar("GET", "/api/usuarios/" + guardado.getId(), null, token + "alterado").statusCode());
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String cuerpo, String token) throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + ruta));
        if (token != null) {
            solicitud.header("Authorization", "Bearer " + token);
        }
        if (cuerpo != null) {
            solicitud.header("Content-Type", "application/json");
        }
        solicitud.method(metodo, cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo));
        return cliente.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }
}

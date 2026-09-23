package com.uade.ecommerce.validation;

import com.uade.ecommerce.controller.CarritoController;
import com.uade.ecommerce.controller.ProductoController;
import com.uade.ecommerce.controller.UsuarioController;
import com.uade.ecommerce.exception.GlobalExceptionHandler;
import com.uade.ecommerce.service.CarritoService;
import com.uade.ecommerce.service.ProductoService;
import com.uade.ecommerce.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ValidacionEntradaTests {

    private MockMvc mockMvc;
    private UsuarioService usuarioService;
    private ProductoService productoService;
    private CarritoService carritoService;

    @BeforeEach
    void configurarMockMvc() {
        usuarioService = mock(UsuarioService.class);
        productoService = mock(ProductoService.class);
        carritoService = mock(CarritoService.class);

        UsuarioController usuarioController = new UsuarioController();
        ProductoController productoController = new ProductoController();
        CarritoController carritoController = new CarritoController();

        ReflectionTestUtils.setField(usuarioController, "usuarioService", usuarioService);
        ReflectionTestUtils.setField(productoController, "productoService", productoService);
        ReflectionTestUtils.setField(carritoController, "carritoService", carritoService);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        usuarioController,
                        productoController,
                        carritoController
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void rechazaDatosObligatoriosFaltantes() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("La solicitud contiene datos inválidos"))
                .andExpect(jsonPath("$.errores.nombreUsuario")
                        .value("El nombre de usuario es obligatorio"))
                .andExpect(jsonPath("$.errores.mail")
                        .value("El mail es obligatorio"));

        verifyNoInteractions(usuarioService);
    }

    @Test
    void rechazaEmailInvalido() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreUsuario": "benjamin",
                                  "mail": "correo-invalido",
                                  "contrasenia": "secreto",
                                  "nombre": "Benjamin",
                                  "apellido": "Perez"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.mail")
                        .value("El formato del mail es inválido"));

        verifyNoInteractions(usuarioService);
    }

    @Test
    void rechazaPrecioNegativo() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Teclado",
                                  "descripcion": "Mecánico",
                                  "precio": -1,
                                  "stock": 5,
                                  "categoriaId": 1,
                                  "usuarioId": 1,
                                  "fotos": ["https://ejemplo.com/teclado.jpg"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.precio")
                        .value("El precio debe ser mayor a cero"));

        verifyNoInteractions(productoService);
    }

    @Test
    void rechazaCantidadInvalida() throws Exception {
        mockMvc.perform(post("/api/carritos/items")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 1,
                                  "productoId": 2,
                                  "cantidad": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cantidad")
                        .value("La cantidad debe ser mayor a cero"));

        verifyNoInteractions(carritoService);
    }
}

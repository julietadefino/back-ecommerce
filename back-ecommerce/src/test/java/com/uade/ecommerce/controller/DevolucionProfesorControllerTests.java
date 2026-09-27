package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.ProductoCrearDTO;
import com.uade.ecommerce.dto.ProductoRespuestaDTO;
import com.uade.ecommerce.dto.UsuarioRegistroDTO;
import com.uade.ecommerce.dto.UsuarioRespuestaDTO;
import com.uade.ecommerce.model.Carrito;
import com.uade.ecommerce.model.Usuario;
import com.uade.ecommerce.service.CarritoService;
import com.uade.ecommerce.service.ProductoService;
import com.uade.ecommerce.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DevolucionProfesorControllerTests {

    @Test
    void listarUsuariosDevuelve200ConDtos() throws Exception {
        UsuarioService service = mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController();
        ReflectionTestUtils.setField(controller, "usuarioService", service);
        when(service.listarRespuestas()).thenReturn(List.of(
                new UsuarioRespuestaDTO(1L, "ana", "ana@ejemplo.com", "Ana", "Gomez")
        ));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void registrarUsuarioEnviaDtoAlServicioYDevuelve201() throws Exception {
        UsuarioService service = mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController();
        ReflectionTestUtils.setField(controller, "usuarioService", service);
        when(service.registrar(any(UsuarioRegistroDTO.class))).thenReturn(
                new UsuarioRespuestaDTO(1L, "ana", "ana@ejemplo.com", "Ana", "Gomez")
        );

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(post("/api/usuarios/registro")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nombreUsuario":"ana","mail":"ana@ejemplo.com",
                                 "contrasenia":"Clave123!","nombre":"Ana","apellido":"Gomez"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).registrar(any(UsuarioRegistroDTO.class));
    }

    @Test
    void crearProductoEnviaDtoAlServicioYDevuelve201() throws Exception {
        ProductoService service = mock(ProductoService.class);
        ProductoController controller = new ProductoController();
        ReflectionTestUtils.setField(controller, "productoService", service);
        when(service.crear(any(ProductoCrearDTO.class))).thenReturn(new ProductoRespuestaDTO());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(post("/api/productos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nombre":"Mouse","descripcion":"Mouse optico",
                                 "precio":200,"stock":2,"categoriaId":1,"usuarioId":1,"fotos":[]}
                                """))
                .andExpect(status().isCreated());

        verify(service).crear(any(ProductoCrearDTO.class));
    }

    @Test
    void consultarCarritoDevuelve200YDto() throws Exception {
        CarritoService service = mock(CarritoService.class);
        CarritoController controller = new CarritoController();
        ReflectionTestUtils.setField(controller, "carritoService", service);
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Carrito carrito = new Carrito();
        carrito.setId(2L);
        carrito.setUsuario(usuario);
        when(service.getByUsuario(1L)).thenReturn(carrito);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(get("/api/carritos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(1));
    }
}

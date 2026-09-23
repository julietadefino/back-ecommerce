package com.uade.ecommerce.controller;

import com.uade.ecommerce.dto.CategoriaDTO;
import com.uade.ecommerce.exception.CategoriaNoEncontradaException;
import com.uade.ecommerce.exception.GlobalExceptionHandler;
import com.uade.ecommerce.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoriaControllerTests {

    private MockMvc mockMvc;
    private CategoriaService categoriaService;

    @BeforeEach
    void configurarMockMvc() {
        categoriaService = mock(CategoriaService.class);

        CategoriaController categoriaController =
                new CategoriaController();

        ReflectionTestUtils.setField(
                categoriaController,
                "categoriaService",
                categoriaService
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(categoriaController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void listaCategoriasConEstado200() throws Exception {
        when(categoriaService.getAll())
                .thenReturn(List.of(
                        new CategoriaDTO(
                                1L,
                                "Electrónica"
                        ),
                        new CategoriaDTO(
                                2L,
                                "Hogar"
                        )
                ));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(1))
                .andExpect(jsonPath("$[0].nombre")
                        .value("Electrónica"))
                .andExpect(jsonPath("$[1].nombre")
                        .value("Hogar"));
    }

    @Test
    void obtieneCategoriaPorIdConEstado200()
            throws Exception {
        when(categoriaService.getById(1L))
                .thenReturn(
                        new CategoriaDTO(
                                1L,
                                "Hogar"
                        )
                );

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.nombre")
                        .value("Hogar"));
    }

    @Test
    void devuelve404CuandoLaCategoriaNoExiste()
            throws Exception {
        when(categoriaService.getById(50L))
                .thenThrow(
                        new CategoriaNoEncontradaException(
                                "Categoría no encontrada"
                        )
                );

        mockMvc.perform(get("/api/categorias/50"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado")
                        .value(404))
                .andExpect(jsonPath("$.mensaje")
                        .value("Categoría no encontrada"));
    }

    @Test
    void buscaCategoriaPorNombreConEstado200()
            throws Exception {
        when(categoriaService.buscarPorNombre(
                "ropa",
                true
        )).thenReturn(
                new CategoriaDTO(3L, "Ropa")
        );

        mockMvc.perform(
                        get("/api/categorias/buscar")
                                .param("nombre", "ropa")
                                .param(
                                        "ignorarMayusculas",
                                        "true"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(3))
                .andExpect(jsonPath("$.nombre")
                        .value("Ropa"));

        verify(categoriaService)
                .buscarPorNombre("ropa", true);
    }

    @Test
    void creaCategoriaConEstado201()
            throws Exception {
        when(categoriaService.crear(
                any(CategoriaDTO.class)
        )).thenReturn(
                new CategoriaDTO(1L, "Hogar")
        );

        mockMvc.perform(
                        post("/api/categorias")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "nombre": "Hogar"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.nombre")
                        .value("Hogar"));
    }

    @Test
    void actualizaCategoriaConEstado200()
            throws Exception {
        when(categoriaService.actualizar(
                any(Long.class),
                any(CategoriaDTO.class)
        )).thenReturn(
                new CategoriaDTO(
                        1L,
                        "Electrónica"
                )
        );

        mockMvc.perform(
                        put("/api/categorias/1")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "nombre": "Electrónica"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.nombre")
                        .value("Electrónica"));
    }

    @Test
    void eliminaCategoriaConEstado204()
            throws Exception {
        mockMvc.perform(
                        delete("/api/categorias/1")
                )
                .andExpect(status().isNoContent());

        verify(categoriaService).delete(1L);
    }

    @Test
    void rechazaNombreVacioConEstado400()
            throws Exception {
        mockMvc.perform(
                        post("/api/categorias")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "nombre": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado")
                        .value(400))
                .andExpect(
                        jsonPath("$.errores.nombre")
                                .value(
                                        "El nombre de la categoría " +
                                        "es obligatorio"
                                )
                );
    }
}
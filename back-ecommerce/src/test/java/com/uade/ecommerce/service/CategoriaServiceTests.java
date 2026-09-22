package com.uade.ecommerce.service;

import com.uade.ecommerce.dto.CategoriaDTO;
import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.exception.CategoriaEnUsoException;
import com.uade.ecommerce.exception.CategoriaNoEncontradaException;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.repository.CategoriaRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTests {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria crearCategoria(
            Long id,
            String nombre,
            boolean activo
    ) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setActivo(activo);
        return categoria;
    }

    @Test
    void devuelveLasCategoriasActivasEnOrdenAlfabetico() {
        List<Categoria> categorias = List.of(
                crearCategoria(1L, "Electrónica", true),
                crearCategoria(2L, "Hogar", true),
                crearCategoria(3L, "Ropa", true)
        );

        when(categoriaRepository
                .findAllByActivoTrueOrderByNombreAsc())
                .thenReturn(categorias);

        List<CategoriaDTO> resultado =
                categoriaService.getAll();

        assertEquals(3, resultado.size());
        assertEquals(
                List.of("Electrónica", "Hogar", "Ropa"),
                resultado.stream()
                        .map(CategoriaDTO::getNombre)
                        .toList()
        );

        verify(categoriaRepository)
                .findAllByActivoTrueOrderByNombreAsc();
    }

    @Test
    void obtieneUnaCategoriaActivaPorId() {
        Categoria categoria =
                crearCategoria(1L, "Hogar", true);

        when(categoriaRepository
                .findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(categoria));

        CategoriaDTO resultado =
                categoriaService.getById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Hogar", resultado.getNombre());
    }

    @Test
    void creaUnaCategoriaNormalizandoElNombre() {
        CategoriaDTO nueva =
                new CategoriaDTO(null, "  Hogar  ");

        when(categoriaRepository
                .existsByNombreIgnoreCase("Hogar"))
                .thenReturn(false);

        when(categoriaRepository
                .save(any(Categoria.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0)
                );

        CategoriaDTO resultado =
                categoriaService.crear(nueva);

        assertEquals("Hogar", resultado.getNombre());

        verify(categoriaRepository)
                .existsByNombreIgnoreCase("Hogar");

        verify(categoriaRepository)
                .save(argThat(categoria ->
                        categoria.getId() == null
                                && categoria.isActivo()
                                && categoria.getNombre()
                                .equals("Hogar")
                ));
    }

    @Test
    void rechazaUnNombreVacio() {
        CategoriaDTO nueva =
                new CategoriaDTO(null, "   ");

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> categoriaService.crear(nueva)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                excepcion.getStatus()
        );

        assertEquals(
                "El nombre de la categoría es obligatorio",
                excepcion.getMessage()
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void rechazaUnaCategoriaRepetidaSinImportarMayusculas() {
        CategoriaDTO nueva =
                new CategoriaDTO(null, "electrónica");

        when(categoriaRepository
                .existsByNombreIgnoreCase("electrónica"))
                .thenReturn(true);

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> categoriaService.crear(nueva)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                excepcion.getStatus()
        );

        assertEquals(
                "Ya existe una categoría con ese nombre",
                excepcion.getMessage()
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void actualizaUnaCategoriaExistente() {
        Categoria existente =
                crearCategoria(
                        1L,
                        "Tecnología",
                        true
                );

        CategoriaDTO datos =
                new CategoriaDTO(
                        null,
                        "  Electrónica  "
                );

        when(categoriaRepository
                .findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(existente));

        when(categoriaRepository
                .existsByNombreIgnoreCase("Electrónica"))
                .thenReturn(false);

        when(categoriaRepository.save(existente))
                .thenReturn(existente);

        CategoriaDTO resultado =
                categoriaService.actualizar(1L, datos);

        assertEquals(
                "Electrónica",
                resultado.getNombre()
        );

        verify(categoriaRepository).save(existente);
    }

    @Test
    void rechazaUnNombreRepetidoAlActualizar() {
        Categoria existente =
                crearCategoria(
                        1L,
                        "Electrónica",
                        true
                );

        CategoriaDTO datos =
                new CategoriaDTO(null, "Ropa");

        when(categoriaRepository
                .findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(existente));

        when(categoriaRepository
                .existsByNombreIgnoreCase("Ropa"))
                .thenReturn(true);

        ApiException excepcion = assertThrows(
                ApiException.class,
                () -> categoriaService.actualizar(
                        1L,
                        datos
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                excepcion.getStatus()
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void buscaUnaCategoriaPorNombreExacto() {
        Categoria categoria =
                crearCategoria(1L, "Ropa", true);

        when(categoriaRepository
                .findByNombreAndActivoTrue("Ropa"))
                .thenReturn(Optional.of(categoria));

        CategoriaDTO resultado =
                categoriaService.buscarPorNombre(
                        "Ropa",
                        false
                );

        assertEquals("Ropa", resultado.getNombre());

        verify(categoriaRepository)
                .findByNombreAndActivoTrue("Ropa");
    }

    @Test
    void buscaUnaCategoriaIgnorandoMayusculas() {
        Categoria categoria =
                crearCategoria(1L, "Electrónica", true);

        when(categoriaRepository
                .findByNombreIgnoreCaseAndActivoTrue(
                        "electrónica"
                ))
                .thenReturn(Optional.of(categoria));

        CategoriaDTO resultado =
                categoriaService.buscarPorNombre(
                        "electrónica",
                        true
                );

        assertEquals(
                "Electrónica",
                resultado.getNombre()
        );

        verify(categoriaRepository)
                .findByNombreIgnoreCaseAndActivoTrue(
                        "electrónica"
                );
    }

    @Test
    void devuelveErrorCuandoLaCategoriaNoExiste() {
        when(categoriaRepository
                .findByIdAndActivoTrue(50L))
                .thenReturn(Optional.empty());

        CategoriaNoEncontradaException excepcion =
                assertThrows(
                        CategoriaNoEncontradaException.class,
                        () -> categoriaService.getById(50L)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                excepcion.getStatus()
        );

        assertEquals(
                "Categoría no encontrada",
                excepcion.getMessage()
        );
    }

    @Test
    void rechazaIdentificadoresInvalidos() {
        ApiException idCero = assertThrows(
                ApiException.class,
                () -> categoriaService.getById(0L)
        );

        CategoriaDTO datos =
                new CategoriaDTO(null, "Hogar");

        ApiException idNegativo = assertThrows(
                ApiException.class,
                () -> categoriaService.actualizar(
                        -1L,
                        datos
                )
        );

        ApiException idNulo = assertThrows(
                ApiException.class,
                () -> categoriaService.delete(null)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                idCero.getStatus()
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                idNegativo.getStatus()
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                idNulo.getStatus()
        );

        verifyNoInteractions(
                categoriaRepository,
                productoRepository
        );
    }

    @Test
    void eliminaLogicamenteUnaCategoriaSinProductos() {
        Categoria categoria =
                crearCategoria(1L, "Hogar", true);

        when(categoriaRepository
                .findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository
                .existsByCategoriaId(1L))
                .thenReturn(false);

        when(categoriaRepository.save(categoria))
                .thenReturn(categoria);

        categoriaService.delete(1L);

        assertFalse(categoria.isActivo());

        verify(categoriaRepository).save(categoria);
        verify(categoriaRepository, never())
                .delete(any(Categoria.class));
    }

    @Test
    void impideEliminarUnaCategoriaConProductosAsociados() {
        Categoria categoria =
                crearCategoria(
                        1L,
                        "Electrónica",
                        true
                );

        when(categoriaRepository
                .findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository
                .existsByCategoriaId(1L))
                .thenReturn(true);

        CategoriaEnUsoException excepcion =
                assertThrows(
                        CategoriaEnUsoException.class,
                        () -> categoriaService.delete(1L)
                );

        assertEquals(
                HttpStatus.CONFLICT,
                excepcion.getStatus()
        );

        assertEquals(
                "No se puede eliminar la categoría " +
                "porque tiene productos asociados",
                excepcion.getMessage()
        );

        assertTrue(categoria.isActivo());

        verify(categoriaRepository, never())
                .save(any(Categoria.class));

        verify(categoriaRepository, never())
                .delete(any(Categoria.class));
    }
}
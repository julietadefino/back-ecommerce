package com.uade.ecommerce.service;

import com.uade.ecommerce.dto.CategoriaDTO;
import com.uade.ecommerce.exception.ApiException;
import com.uade.ecommerce.exception.CategoriaEnUsoException;
import com.uade.ecommerce.exception.CategoriaNoEncontradaException;
import com.uade.ecommerce.model.Categoria;
import com.uade.ecommerce.repository.CategoriaRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<CategoriaDTO> getAll() {
        return categoriaRepository
                .findAllByActivoTrueOrderByNombreAsc()
                .stream()
                .map(CategoriaDTO::fromEntity)
                .toList();
    }

    public CategoriaDTO getById(Long id) {
        validarId(id);

        Categoria categoria = categoriaRepository
                .findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new CategoriaNoEncontradaException(
                                "Categoría no encontrada"
                        )
                );

        return CategoriaDTO.fromEntity(categoria);
    }

    public CategoriaDTO buscarPorNombre(
            String nombre,
            boolean ignorarMayusculas
    ) {
        String nombreNormalizado = validarNombre(nombre);

        Optional<Categoria> categoria;

        if (ignorarMayusculas) {
            categoria = categoriaRepository
                    .findByNombreIgnoreCaseAndActivoTrue(
                            nombreNormalizado
                    );
        } else {
            categoria = categoriaRepository
                    .findByNombreAndActivoTrue(
                            nombreNormalizado
                    );
        }

        return categoria
                .map(CategoriaDTO::fromEntity)
                .orElseThrow(() ->
                        new CategoriaNoEncontradaException(
                                "Categoría no encontrada"
                        )
                );
    }

    public CategoriaDTO crear(CategoriaDTO datos) {
        String nombre = validarNombre(datos.getNombre());

        if (categoriaRepository
                .existsByNombreIgnoreCase(nombre)) {
            throw ApiException.conflict(
                    "Ya existe una categoría con ese nombre"
            );
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setActivo(true);

        Categoria guardada =
                categoriaRepository.save(categoria);

        return CategoriaDTO.fromEntity(guardada);
    }

    public CategoriaDTO actualizar(
            Long id,
            CategoriaDTO datos
    ) {
        validarId(id);

        Categoria categoria = categoriaRepository
                .findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new CategoriaNoEncontradaException(
                                "Categoría no encontrada"
                        )
                );

        String nombreNormalizado =
                validarNombre(datos.getNombre());

        boolean cambioElNombre = !categoria.getNombre()
                .equalsIgnoreCase(nombreNormalizado);

        if (cambioElNombre
                && categoriaRepository
                .existsByNombreIgnoreCase(
                        nombreNormalizado
                )) {
            throw ApiException.conflict(
                    "Ya existe una categoría con ese nombre"
            );
        }

        categoria.setNombre(nombreNormalizado);

        Categoria actualizada =
                categoriaRepository.save(categoria);

        return CategoriaDTO.fromEntity(actualizada);
    }

    public void delete(Long id) {
        validarId(id);

        Categoria categoria = categoriaRepository
                .findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new CategoriaNoEncontradaException(
                                "Categoría no encontrada"
                        )
                );

        if (productoRepository.existsByCategoriaId(id)) {
            throw new CategoriaEnUsoException(
                    "No se puede eliminar la categoría " +
                    "porque tiene productos asociados"
            );
        }

        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw ApiException.badRequest(
                    "El nombre de la categoría es obligatorio"
            );
        }

        return nombre.trim();
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw ApiException.badRequest(
                    "El identificador de la categoría " +
                    "debe ser mayor a cero"
            );
        }
    }
}
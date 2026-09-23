package com.uade.ecommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(
            mappedBy = "producto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Foto> fotos = new ArrayList<>();

    /**
     * Agrega una foto manteniendo sincronizados los dos lados
     * de la relación bidireccional.
     */
    public void agregarFoto(Foto foto) {
        foto.setProducto(this);
        fotos.add(foto);
    }

    /**
     * Quita una foto del producto. Con orphanRemoval activo esto
     * alcanza para que Hibernate la borre de la base.
     */
    public void eliminarFoto(Foto foto) {
        fotos.remove(foto);
        foto.setProducto(null);
    }
}

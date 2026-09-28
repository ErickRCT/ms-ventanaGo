package com.ventanago.quincalleria.repository.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ventanago.pauta.repository.entity.PautaQuincalleria;
import com.ventanago.serie.repository.entity.Serie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "quincalleria")
public class Quincalleria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quincalleria_id")
    private Long quincalleriaId;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "unidad")
    private String unidad;

    @Column(name = "valor")
    private int valor;

    @OneToMany(mappedBy = "quincalleria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<PautaQuincalleria> pautaQuincallerias;

    @Column(name="ruta_imagen")
    private String rutaImagen;

    @ManyToOne
    @JoinColumn(name = "serie_id", referencedColumnName = "serie_id")
    private Serie serie;

}

package com.ventanago.perfileria.repository.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ventanago.pauta.repository.entity.PautaPerfil;
import com.ventanago.serie.repository.entity.Serie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perfil_id")
    private Long perfilId;

    @Column
    private String codigo;

    @Column
    private String descripcion;

    @Column
    private Double peso;

    @Column(name = "is_bastidor")
    private Boolean isBastidor;

    @ManyToOne
    @JoinColumn(name = "tipo_perfil_id", referencedColumnName = "tipo_perfil_id")
    private TipoPerfil tipoPerfil;

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<PautaPerfil> perfiles;

    @ManyToOne
    @JoinColumn(name = "serie_id", referencedColumnName = "serie_id")
    private Serie serie;

    @Column
    private boolean reforzado;

    @Column
    private String rutaImagen;

    @Column
    private String orientacion;







}

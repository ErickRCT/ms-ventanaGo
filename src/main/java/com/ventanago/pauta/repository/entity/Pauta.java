package com.ventanago.pauta.repository.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ventanago.serie.repository.entity.Serie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "pauta")
public class Pauta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pauta_id")
    private Long pautaId;

    private String nombre;

    private String descripcion;

    @Column(name = "peso_teorico_horizontal")
    private Double pesoTeoricoHorizontal;

    @Column(name = "peso_teorico_vertical")
    private Double pesoTeoricoVertical;

    @Column(name = "peso_teorico_reforzado_horizontal")
    private Double pesoTeoricoReforzadoHorizontal;

    @Column(name = "peso_teorico_reforzado_vertical")
    private Double pesoTeoricoReforzadoVertical;

    @Column(name = "vertical_reforzada")
    private int verticalReforzada;

    @Column(name = "horizontal_reforzada")
    private int horizontalReforzada;

    @Column(name = "is_reforzada")
    private Boolean isReforzada;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_pauta_id", referencedColumnName = "tipo_pauta_id")
    private TipoPauta tipoPauta;

    @OneToMany(mappedBy = "pauta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<PautaVidrio> vidrios;

    @OneToMany(mappedBy = "pauta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<PautaQuincalleria> quincallerias;

    @OneToMany(mappedBy = "pauta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<PautaPerfil> perfiles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "serie_id", referencedColumnName = "serie_id")
    private Serie serie;

}

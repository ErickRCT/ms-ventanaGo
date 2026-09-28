package com.ventanago.pauta.repository.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ventanago.quincalleria.repository.entity.Quincalleria;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pauta_quincalleria")
public class PautaQuincalleria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pauta_quincalleria_id")
    private Long pautaQuincalleriaId;

    @ManyToOne
    @JoinColumn(name = "pauta_id")
    @JsonIgnore
    private Pauta pauta;

    @ManyToOne
    @JoinColumn(name = "quincalleria_id")
    private Quincalleria quincalleria;

    @Column(name = "cantidad")
    private int cantidad;

    @Column(name = "variacion_h")
    private int variacionH;

    @Column(name = "variacion_v")
    private int variacionV;

}

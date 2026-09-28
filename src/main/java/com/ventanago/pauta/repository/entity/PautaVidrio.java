package com.ventanago.pauta.repository.entity;

import com.ventanago.vidrio.repository.entity.Vidrio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pauta_vidrio")
public class PautaVidrio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pauta_vidrio_id")
    private Long pautaVidrioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pauta_id")
    private Pauta pauta;

    private Long cantidad;

    @Column(name = "variacion_h")
    private Long variacionH;

    @Column(name = "variacion_v")
    private Long variacionV;

    private String formula;

    @ManyToOne
    @JoinColumn(name = "vidrio_id")
    private Vidrio vidrio;



}


package com.ventanago.pauta.repository.entity;

import com.ventanago.perfileria.repository.entity.Perfil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pauta_perfil")
public class PautaPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pauta_perfil_id")
    private Long pautaPerfilId;

    @ManyToOne
    @JoinColumn(name = "pauta_id")
    private Pauta pauta;

    @ManyToOne
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    private String corte;

    private char orientacion;

    private Long cantidad;

    private Long variacion;

    private boolean dividir;






}

package com.ventanago.perfileria.repository.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tipo_perfil")
public class TipoPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_perfil_id")
    private Long tipoPerfilId;

    @Column(name = "nombre")
    private String nombre;

}

package com.ventanago.vidrio.repository.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ventanago.pauta.repository.entity.PautaVidrio;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vidrio")
public class Vidrio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vidrio_id")
    private Long vidrioId;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "valor")
    private int valor;

}

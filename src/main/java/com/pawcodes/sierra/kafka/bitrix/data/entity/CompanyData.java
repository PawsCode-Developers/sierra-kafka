package com.pawcodes.sierra.kafka.bitrix.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company")
public class CompanyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int tipo_identificacion;
    int nit;
    int digita;
    String nombre;
    int bloqueo;
    int id_definicion_tributaria_tipo;
}

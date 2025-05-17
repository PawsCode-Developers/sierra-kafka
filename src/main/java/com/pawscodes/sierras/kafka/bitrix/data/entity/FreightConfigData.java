package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "softjs_documentos_ped_config_fletes")
public class FreightConfigData {
    @Id
    int id;
    String tipoflete;
    String categoriacliente;
    String pais;
    String departamento;
    String ciudad;
    double valorflete;
    double valorminimoventa;
    double porcasumefletes;
}

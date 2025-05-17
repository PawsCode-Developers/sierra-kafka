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
@Table(name = "v_cartera_resumen_edades")
public class QuotaData {
    int sw;
    String Tipo;
    @Id
    int Numero;
    long Nit;
    int vendedor;
    double valor_total;
    double valor_aplicado;
    double Saldo;
    double Sin_Vencer;
    double Vencida;
}

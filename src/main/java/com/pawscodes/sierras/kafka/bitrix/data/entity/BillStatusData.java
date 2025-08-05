package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_trazapedidofacturapago_Js")
public class BillStatusData {
    @Column(name = "Fecha orden")
    LocalDateTime orderDate;
    @Column(name = "Fecha fin produccion")
    LocalDateTime productionEndDate;
    @Id
    @Column(name = "Número")
    long number;
    @Column(name = "Autorizado factura")
    String billAuthorization;
    @Column(name = "Fecha autorizó")
    LocalDateTime authorizationDate;
    @Column(name = "Remisión")
    String remission;
    @Column(name = "Fecha remisión")
    LocalDateTime remissionDate;
    @Column(name = "Factura")
    String bill;
    @Column(name = "Fecha factura")
    LocalDateTime billDate;
    @Column(name = "Usuario confirma factura")
    String user;
    @Column(name = "Fecha hora confirma factura")
    LocalDateTime confirmDate;
}

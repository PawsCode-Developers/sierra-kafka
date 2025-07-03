package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "softjs_documentos_ped")
public class SoftJSDocumentPed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Builder.Default
    int sw = 1;
    int bodega;
    int numero;
    long nit;
    @Builder.Default
    char anulado = 'N';
    LocalDate fecha;
    @Column(name = "fechahora")
    LocalDateTime fechaHora;
    @Column(name = "valortotal")
    double valorTotal;
    @Column(name = "iddocped")
    int idDocPed;
    long vendedor;
    String condicion;
    String documento;
    String notas;
    int diasvalidez;
    @Builder.Default
    String pc = "BITRIX";
    @Builder.Default
    String pcactualizo = "BITRIX";
    String usuario;
    String usuarioactualizo;
    LocalDateTime fechahoraactualizo;
    @Builder.Default
    double fletes = 0;
    @Builder.Default
    int descuentopie = 0;
    @Builder.Default
    double ivafletes = 0;
    @Builder.Default
    double tasa = 1;
    @Builder.Default
    int idsolicitudservtecnico = 0;
    @Builder.Default
    String simbolomoneda = "COP";
    @Builder.Default
    int fletesconfig_valorconfigurado = 0;
    @Builder.Default
    int fletesconfig_valorminventa = 0;
    @Builder.Default
    int codigodireccion = 0;
    @Builder.Default
    int listaprecios = 1;
    @Builder.Default
    int concepto = 0;
    @Builder.Default
    int concepto2 = 0;
}

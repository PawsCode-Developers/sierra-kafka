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
@Table(name = "definicion_tributaria_tipo")
public class DefinitionTypeData {
    @Id
    String id;
    String tipo_tercero;
    String descripcion;
    String anulado;
}

package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.DepartmentId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(DepartmentId.class)
@Table(name = "y_departamentos")
public class DepartmentData {
    @Id
    String pais;
    @Id
    String departamento;
    String descripcion;
}

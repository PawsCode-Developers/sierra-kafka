package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.ActivitiesDescriptionId;
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
@IdClass(ActivitiesDescriptionId.class)
@Table(name = "softjs_prd_proceso_actividad")
public class ActivitiesDescriptionData {
    @Id
    String proceso;
    @Id
    String actividad;
    String descripcion;
}

package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.PrdProcessId;
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
@IdClass(PrdProcessId.class)
@Table(name = "softjs_prd_proceso_actividad_start_stop")
public class PrdProcessData {
    @Id
    String numeroop;
    @Id
    String codigo;
    String seq;
    @OneToOne
    @JoinColumn(name = "proceso", insertable = false, updatable = false)
    ProcessDescriptionData proceso;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "proceso", referencedColumnName = "proceso", insertable = false, updatable = false),
            @JoinColumn(name = "actividad", referencedColumnName = "actividad", insertable = false, updatable = false)
    })
    ActivitiesDescriptionData actividad;
    String noaplica;
}

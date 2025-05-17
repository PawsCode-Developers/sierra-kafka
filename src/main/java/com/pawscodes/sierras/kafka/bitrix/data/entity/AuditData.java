package com.pawscodes.sierras.kafka.bitrix.data.entity;

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
@Table(name = "auditoria")
public class AuditData {
    @Id
    @Builder.Default
    LocalDateTime fecha = LocalDateTime.now();
    @Builder.Default
    String pc = "BITRIX";
    String usuario;
    String que;
}

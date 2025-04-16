package com.pawscodes.sierras.kafka.bitrix.data.entity;

import jakarta.persistence.Column;
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
@Table(name = "consecutivos")
public class ConsecutiveData {
    @Id
    @Column(name = "tipo")
    String type;
    @Column(name = "siguiente")
    int next;
}

package com.pawscodes.sierras.kafka.bitrix.model.bitrix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitrixGetList<T> {

    T filter;

    @Builder.Default
    List<String> select = List.of("*", "UF_*");

    int start;
}

package com.pawscodes.sierras.kafka.bitrix.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Customer {
    private Integer companyId;
    private Integer customerId;
}

package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.ProductSubGroup2Data;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.ProductSubGroup2Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSubGroup2Repository extends JpaRepository<ProductSubGroup2Data, ProductSubGroup2Id> {
}

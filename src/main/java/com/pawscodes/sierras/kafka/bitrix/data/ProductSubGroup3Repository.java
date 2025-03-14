package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.ProductSubGroup3Data;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.ProductSubGroup3Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSubGroup3Repository extends JpaRepository<ProductSubGroup3Data, ProductSubGroup3Id> {
}

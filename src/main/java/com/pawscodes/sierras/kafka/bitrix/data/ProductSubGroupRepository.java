package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.ProductSubGroupData;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.ProductSubGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSubGroupRepository extends JpaRepository<ProductSubGroupData, ProductSubGroupId> {
}

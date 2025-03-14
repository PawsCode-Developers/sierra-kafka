package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.DiscountCli;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.DiscountCliId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountCliRepository extends JpaRepository<DiscountCli, DiscountCliId> {
}

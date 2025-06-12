package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.DocumentLinPed;
import com.pawscodes.sierras.kafka.bitrix.data.entity.DocumentLinPedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentLinPedHistoryRepository extends JpaRepository<DocumentLinPedHistory, Integer> {
    DocumentLinPed findTop1ByOrderByIdDesc();
}

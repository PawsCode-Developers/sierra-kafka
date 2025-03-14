package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.DocumentPed;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.DocumentPedId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentPedRepository extends JpaRepository<DocumentPed, DocumentPedId> {
}

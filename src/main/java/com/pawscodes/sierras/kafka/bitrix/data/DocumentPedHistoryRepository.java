package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.DocumentPedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentPedHistoryRepository extends JpaRepository<DocumentPedHistory, Integer> {
    DocumentPedHistory findByNumero(int numero);
}

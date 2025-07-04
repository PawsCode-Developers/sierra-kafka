package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.DocumentLinPedHistory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentLinPedHistoryRepository extends JpaRepository<DocumentLinPedHistory, Integer> {
    DocumentLinPedHistory findByNumeroAndCodigoAndSeq(int numero, String codigo, int seq);
    List<DocumentLinPedHistory> findByNumero(int numero);

    @Transactional
    void deleteBySeqGreaterThanAndNumero(int seq, int numero);
}

package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.DocumentLinPedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentLinPedHistoryRepository extends JpaRepository<DocumentLinPedHistory, Integer> {
    DocumentLinPedHistory findTop1ByOrderByIdDesc();

    DocumentLinPedHistory findByNumeroAndCodigoAndCantidad(int numero, String codigo, double cantidad);

    List<DocumentLinPedHistory> findByNumero(int numero);
}

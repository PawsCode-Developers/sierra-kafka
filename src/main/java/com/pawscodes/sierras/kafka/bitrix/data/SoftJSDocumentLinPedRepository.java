package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.SoftJSDocumentLinPed;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftJSDocumentLinPedRepository extends JpaRepository<SoftJSDocumentLinPed, Integer> {
    List<SoftJSDocumentLinPed> findByNumero(int numero);

    SoftJSDocumentLinPed findByNumeroAndCodigoAndSeq(int numero, String codigo, int seq);

    @Transactional
    void deleteBySeqGreaterThanAndNumero(int seq, int numero);
}

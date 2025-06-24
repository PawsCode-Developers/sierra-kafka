package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.SoftJSDocumentPed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftJSDocumentPedRepository extends JpaRepository<SoftJSDocumentPed, Integer> {
    SoftJSDocumentPed findByNumero(int numero);
}

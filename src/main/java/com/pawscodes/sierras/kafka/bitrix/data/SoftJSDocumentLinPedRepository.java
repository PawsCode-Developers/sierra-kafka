package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.SoftJSDocumentLinPed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftJSDocumentLinPedRepository extends JpaRepository<SoftJSDocumentLinPed, Integer> {
}

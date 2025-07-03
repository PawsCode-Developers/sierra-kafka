package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.DocumentLinPed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentLinPedRepository extends JpaRepository<DocumentLinPed, Integer> {
    DocumentLinPed findByNumeroAndCodigoAndCantidad(int numero, String codigo, double cantidad);

    @Query("SELECT e FROM DocumentLinPed e WHERE e.numero = ?1 ORDER BY e.seq DESC LIMIT 1")
    DocumentLinPed findLastByNumero(int numero);

    List<DocumentLinPed> findByNumero(int numero);
}
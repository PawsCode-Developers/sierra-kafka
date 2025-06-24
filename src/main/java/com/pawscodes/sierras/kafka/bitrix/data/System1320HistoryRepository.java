package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.System1320HistoryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface System1320HistoryRepository extends JpaRepository<System1320HistoryData, Integer> {
    List<System1320HistoryData> findByNitAndProgramaStartingWithOrderByAutorizadoDesc(long nit, String programa);

    List<System1320HistoryData> findByNitAndProgramaOrderByAutorizadoDesc(long nit, String programa);

    Optional<System1320HistoryData> findFirstByOrderByIdDesc();
}
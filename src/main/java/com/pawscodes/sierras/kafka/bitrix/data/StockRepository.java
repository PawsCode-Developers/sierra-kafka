package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.StockData;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.StockDataId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockData, StockDataId> {
    List<StockData> findByCodigoAndAnoAndMes(String codigo, int ano, int mes);
}

package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.BillStatusData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public interface BillStatusRepository extends JpaRepository<BillStatusData, Long> {
    @Query("SELECT e FROM BillStatusData e WHERE e.authorizationDate BETWEEN :startOfDay AND :endOfDay")
    List<BillStatusData> findByDateBetween(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    default List<BillStatusData> findYesterdayRecords() {
        LocalDateTime startOfDay = LocalDateTime.now(ZoneId.of("America/Bogota")).minusMinutes(40);
        LocalDateTime endOfDay = LocalDateTime.now(ZoneId.of("America/Bogota"));
        return findByDateBetween(startOfDay, endOfDay);
    }
}

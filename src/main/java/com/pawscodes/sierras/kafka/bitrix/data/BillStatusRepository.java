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
    @Query("SELECT e FROM BillStatusData e WHERE e.billDate BETWEEN :startOfDay AND :endOfDay")
    List<BillStatusData> findBillByDateBetween(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT e FROM BillStatusData e WHERE e.remissionDate BETWEEN :startOfDay AND :endOfDay AND e.billDate IS NULL")
    List<BillStatusData> findRemissionByDateBetween(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    default List<BillStatusData> findBillsRecords() {
        LocalDateTime startOfDay = LocalDateTime.now(ZoneId.of("America/Bogota")).minusHours(2);
        LocalDateTime endOfDay = LocalDateTime.now(ZoneId.of("America/Bogota"));
        return findBillByDateBetween(startOfDay, endOfDay);
    }

    default List<BillStatusData> findRemissionsRecords() {
        LocalDateTime startOfDay = LocalDateTime.now(ZoneId.of("America/Bogota")).minusHours(2);
        LocalDateTime endOfDay = LocalDateTime.now(ZoneId.of("America/Bogota"));
        return findRemissionByDateBetween(startOfDay, endOfDay);
    }
}

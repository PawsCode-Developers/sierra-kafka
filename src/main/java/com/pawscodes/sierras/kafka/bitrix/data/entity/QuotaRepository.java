package com.pawscodes.sierras.kafka.bitrix.data.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotaRepository extends JpaRepository<QuotaData, Integer> {
    @Query("SELECT e FROM QuotaData e WHERE e.Nit = :nit")
    List<QuotaData> findAllByNit(long nit);
}

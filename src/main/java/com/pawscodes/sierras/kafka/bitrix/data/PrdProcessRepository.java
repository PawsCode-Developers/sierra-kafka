package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.PrdProcessData;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.PrdProcessId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrdProcessRepository extends JpaRepository<PrdProcessData, PrdProcessId> {
    List<PrdProcessData> findByNumeroop(String numeroop);
}

package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.System1320Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface System1320Repository extends JpaRepository<System1320Data, Integer> {
    List<System1320Data> findByNitAndNotas(long nit, String notas);
}

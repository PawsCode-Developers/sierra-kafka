package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.ConsecutiveData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsecutiveRepository extends JpaRepository<ConsecutiveData, String> {
}

package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.CompanyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyData, Integer> {
}

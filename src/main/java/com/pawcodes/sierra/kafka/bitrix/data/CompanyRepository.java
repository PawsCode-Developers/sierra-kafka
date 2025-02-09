package com.pawcodes.sierra.kafka.bitrix.data;

import com.pawcodes.sierra.kafka.bitrix.data.entity.CompanyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyData, Integer> {
}

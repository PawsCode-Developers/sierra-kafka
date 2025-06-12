package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.CompanyDirData;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.CompanyDirId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyDirRepository extends JpaRepository<CompanyDirData, CompanyDirId> {
    List<CompanyDirData> findByNit(long nit);

    CompanyDirData findByNitAndCodigoDireccion(long nit, int codigo);
}

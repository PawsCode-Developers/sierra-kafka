package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.FreightConfigData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FreightConfigRepository extends JpaRepository<FreightConfigData, Integer> {
    FreightConfigData findByTipofleteAndCategoriaclienteAndPaisAndDepartamentoAndCiudad(String tipoFlete,
                                                                                        String categoriaCliente,
                                                                                        String pais,
                                                                                        String departamento,
                                                                                        String ciudad);
}

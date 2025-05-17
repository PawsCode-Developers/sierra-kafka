package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.ProductData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductData, String> {
    ProductData findByCodigo(String codigo);

    @Query("SELECT e FROM ProductData e WHERE e.codigo LIKE 'FLETES%'")
    List<ProductData> findByFreight();
}

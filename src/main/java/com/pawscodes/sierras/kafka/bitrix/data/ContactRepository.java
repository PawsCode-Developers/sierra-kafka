package com.pawscodes.sierras.kafka.bitrix.data;

import com.pawscodes.sierras.kafka.bitrix.data.entity.ContactData;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.ContactDataId;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends PagingAndSortingRepository<ContactData, ContactDataId> {
    List<ContactData> findByNit(String nit);
}

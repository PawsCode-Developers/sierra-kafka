package com.pawscodes.sierras.kafka.bitrix.data.entity;

import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.ContactDataId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ContactDataId.class)
@Table(name = "CRM_contactos")
public class ContactData {
    @Id
    String nit;
    @Id
    String contacto;
    String nombre;
    String apellidos;
    String tel_ofi1;
    String tel_ofi2;
    String tel_celular;
    String e_mail;
    String cargo;
    String ext1;
}

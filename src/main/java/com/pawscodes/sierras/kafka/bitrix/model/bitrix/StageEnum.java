package com.pawscodes.sierras.kafka.bitrix.model.bitrix;

import lombok.Getter;

@Getter
public enum StageEnum {
    BANDEJA_DE_ENTRADA("Bandeja de entrada", "UC_KABQS8"),
    COTIZACION("Cotización", "NEW"),
    SEGUIMIENTO_COTIZACION("Seguimiento cotización", "PREPAYMENT_INVOICE"),
    VALIDACION_PAGO_CUPO("Validación Pago / Cupo", "PREPARATION"),
    PEDIDO("Pedido", "EXECUTING"),
    SEGUIMIENTO_DEL_PEDIDO("Seguimiento del pedido", "UC_IN9PCF"),
    EN_PRODUCCION("En producción", "FINAL_INVOICE"),
    CERRADO_PERDIDO("Cerrado Perdido", "LOSE"),
    FACTURADO("Facturado", "WON");

    final String name;
    final String value;

    StageEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getValueFromName(String name) {
        for (StageEnum e : values()) {
            if (e.name.equals(name)) {
                return e.value;
            }
        }
        return "";
    }
}

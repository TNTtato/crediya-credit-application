package com.crediya.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name="tipo_prestamo")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditTypeEntity {
    @Id
    @Column("id_tipo_prestamo") Integer creditTypeId;
    @Column("nombre") String name;
    @Column("monto_minimo") Double minAmount;
    @Column("monto_maximo") Double maxAmount;
    @Column("tasa_interes") Float interestRate;
    @Column("validacion_automatica") Boolean automaticValidation;
}

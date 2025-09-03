package com.crediya.r2dbc.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name="solicitud")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditApplicationEntity {
    @Id
    @Column("id_solicitud")Integer applicationId;
    @Column("monto") Double creditAmount;
    @Column("plazo") Integer installments;
    @Column("email") String email;
    @Column("id_estado") Integer statusId;
    @Column("id_tipo_prestamo") Integer creditTypeId;
}

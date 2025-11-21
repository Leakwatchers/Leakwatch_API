package com.unifor.br.leakwatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Data
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gas_level")
    private Double gasLevel;

    // CORREÇÃO: Novo campo para mapear a chave estrangeira mac_address
    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "report_time")
    private LocalDateTime reportTime;

    @Column(name = "status")
    private String status;

    // O campo sensor_id (antigo) foi removido.
}

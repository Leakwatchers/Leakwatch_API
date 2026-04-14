package com.unifor.br.leakwatch.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gas_level")
    private Double gasLevel;

    @Column(name = "sensor_id")
    private String sensorId;

    @Column(name = "report_time")
    private LocalDateTime reportTime;

    @Column(name = "status")
    private String status;

    @Column(name = "sensor_ip")
    private String  sensorIp;
}
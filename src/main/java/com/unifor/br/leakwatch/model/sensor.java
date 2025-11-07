package com.unifor.br.leakwatch.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_name", nullable = false)
    private String sensorName;

    @Column(name = "sensor_type", nullable = false)
    private String sensorType = "G";
}

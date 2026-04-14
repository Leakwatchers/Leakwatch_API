package com.unifor.br.leakwatch.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sensor")
public class Sensor {

    @Id
    @Column(name = "ip_adress")
    private String ipAdress;

    @Column(name = "sensor_name")
    private String sensorName;

    @Column(name = "sensor_type")
    private String sensorType;

    @Column(name = "is_connected")
    private Boolean isConnected;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

}

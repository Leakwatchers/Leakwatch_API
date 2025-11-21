package com.unifor.br.leakwatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "sensor")
@Data
public class Sensor {

    @Id
    @Column(name = "mac_address") // Mapeia para a coluna mac_address (VARCHAR(17) PRIMARY KEY)
    private String macAddress;

    @Column(name = "sensor_name") // Mapeia para a coluna sensor_name
    private String sensorName;

    @Column(name = "sensor_type") // Mapeia para a coluna sensor_type
    private String sensorType;

    @Column(name = "is_connected") // Mapeia para a nova coluna is_connected
    private Boolean isConnected = false;

    // Construtores, getters e setters (gerados pelo Lombok @Data)
}

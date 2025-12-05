package com.unifor.br.leakwatch.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor")
public class Sensor {

    @Id
    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "sensor_name")
    private String sensorName;

    @Column(name = "sensor_type")
    private String sensorType;

    @Column(name = "is_connected")
    private Boolean isConnected;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    // === GETTERS E SETTERS ===

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
}

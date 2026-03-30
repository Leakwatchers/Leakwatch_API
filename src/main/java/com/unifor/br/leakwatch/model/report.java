package com.unifor.br.leakwatch.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gas_level")
    private Double gasLevel;

    @Column(name = "ip_adress")
    private String ipAdress;

    @Column(name = "report_time")
    private LocalDateTime reportTime;

    @Column(name = "status")
    private String status;

    public Long getId() { return id; }
    public Double getGasLevel() { return gasLevel; }
    public void setGasLevel(Double gasLevel) { this.gasLevel = gasLevel; }

    public String getIpAdress() { return ipAdress; }
    public void setMacAddress(String ipAdress) { this.ipAdress = ipAdress; }

    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

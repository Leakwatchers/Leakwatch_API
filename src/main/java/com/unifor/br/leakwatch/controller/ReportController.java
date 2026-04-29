package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Report;
import com.unifor.br.leakwatch.repository.ReportRepository;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    public record ReportResp(
            Long id,
            Double gasLevel,
            String sensorIp,
            String sensorName,
            LocalDateTime reportTime,
            String status
    ) {}

    private final ReportRepository repo;

    public ReportController(ReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<ReportResp> listarTodos() {
        return repo.findAll().stream().map(r -> new ReportResp(
                r.getId(),
                r.getGasLevel(),
                r.getSensor().getIpAdress(),
                r.getSensor().getSensorName(),
                r.getReportTime(),
                r.getStatus()
        )).toList();
    }

    @GetMapping("/sensor/{sensorId}")
    public List<Report> porSensor(@PathVariable Long sensorId) {
        return repo.findBySensorIdOrderByReportTimeDesc(sensorId);
    }

    @GetMapping("/ultimo/{sensorId}")
    public Report ultimo(@PathVariable Long sensorId) {
        return repo.findTopBySensorIdOrderByReportTimeDesc(sensorId);
    }

    @GetMapping("/alertas")
    public List<Report> alertas() {
        return repo.findAlertas();
    }
}
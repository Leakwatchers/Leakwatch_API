package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Report;
import com.unifor.br.leakwatch.model.Sensor;
import com.unifor.br.leakwatch.repository.ReportRepository;
import com.unifor.br.leakwatch.repository.SensorRepository;

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
            String macAddress,
            String sensorName,
            LocalDateTime reportTime,
            String status
    ) {}

    private final ReportRepository repo;
    private final SensorRepository sensorRepo;

    // INJETAR AMBOS
    public ReportController(ReportRepository repo, SensorRepository sensorRepo) {
        this.repo = repo;
        this.sensorRepo = sensorRepo;
    }

    @GetMapping
    public List<ReportResp> listarTodos() {
        return repo.findAll().stream().map(r -> {

            String sensorName = sensorRepo.findById(r.getMacAddress())
                    .map(Sensor::getSensorName)
                    .orElse("Desconhecido");

            return new ReportResp(
                    r.getId(),
                    r.getGasLevel(),
                    r.getMacAddress(),
                    sensorName,
                    r.getReportTime(),
                    r.getStatus()
            );
        }).toList();
    }

    @GetMapping("/sensor/{mac}")
    public List<Report> porSensor(@PathVariable String mac) {
        return repo.findByMacAddressOrderByReportTimeDesc(mac);
    }

    @GetMapping("/ultimo/{mac}")
    public Report ultimo(@PathVariable String mac) {
        return repo.findUltimoByMacAddress(mac);
    }

    @GetMapping("/alertas")
    public List<Report> alertas() {
        return repo.findAlertas();
    }
}

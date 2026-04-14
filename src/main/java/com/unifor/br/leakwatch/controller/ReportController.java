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
            String sensorIp,
            String sensorName,
            LocalDateTime reportTime,
            String status
    ) {}

    private final ReportRepository repo;
    private final SensorRepository sensorRepo;   // <-- injeta aqui

    public ReportController(ReportRepository repo, SensorRepository sensorRepo) {
        this.repo = repo;
        this.sensorRepo = sensorRepo;
    }

    // LISTA TODOS COM NOME DO SENSOR CORRETAMENTE
    @GetMapping
    public List<ReportResp> listarTodos() {
        return repo.findAll().stream().map(r -> {

            String name = sensorRepo.findById(r.getSensorIp())
                    .map(Sensor::getSensorName)
                    .orElse("Desconhecido");

            return new ReportResp(
                    r.getId(),
                    r.getGasLevel(),
                    r.getSensorIp(),
                    name,
                    r.getReportTime(),
                    r.getStatus()
            );
        }).toList();
    }

    @GetMapping("/sensor/{sensorIp}")
    public List<Report> porSensor(@PathVariable String sensorIp) {
        return repo.findBySensorIpOrderByReportTimeDesc(sensorIp);
    }

    @GetMapping("/ultimo/{sensorIp}")
    public Report ultimo(@PathVariable String sensorIp) {
        return repo.findUltimoBySensorIp(sensorIp).get(0);
    }

    @GetMapping("/alertas")
    public List<Report> alertas() {
        return repo.findAlertas();
    }
}

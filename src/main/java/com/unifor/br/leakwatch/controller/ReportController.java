package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.GasStatus;
import com.unifor.br.leakwatch.model.Report;
import com.unifor.br.leakwatch.repository.ReportRepository;

import org.springframework.http.ResponseEntity;
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

    public record ReportRequest(
            Double gasLevel,
            Long sensorId,
            String sensorIp
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
    public List<Report> porSensor(
            @PathVariable Long sensorId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        if (start != null && end != null) {
            LocalDateTime startDate = LocalDateTime.parse(start);
            LocalDateTime endDate   = LocalDateTime.parse(end);
            return repo.findBySensorIdAndReportTimeBetweenOrderByReportTimeAsc(
                    sensorId, startDate, endDate
            );
        }
        return repo.findBySensorIdOrderByReportTimeAsc(sensorId);
    }

    @GetMapping("/ultimo/{sensorId}")
    public Report ultimo(@PathVariable Long sensorId) {
        return repo.findTopBySensorIdOrderByReportTimeDesc(sensorId);
    }

    @GetMapping("/alertas")
    public List<Report> alertas() {
        return repo.findAlertas();
    }

    @GetMapping("/alertas/{status}")
    public ResponseEntity<List<Report>> alertasPorStatus(@PathVariable String status) {
        try {
            GasStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(repo.findByStatus(status.toUpperCase()));
    }

    @PostMapping
    public ResponseEntity<Report> salvar(@RequestBody Report report) {
        if (report.getGasLevel() != null) {
            GasStatus status = GasStatus.classify(report.getGasLevel());
            report.setStatus(status.name());
        }

        // Garante que o reportTime seja preenchido se vier nulo
        if (report.getReportTime() == null) {
            report.setReportTime(LocalDateTime.now());
        }

        return ResponseEntity.ok(repo.save(report));
    }
}
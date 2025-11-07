package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Sensor;
import com.unifor.br.leakwatch.repository.ReportRepository;
import com.unifor.br.leakwatch.repository.SensorRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sensores")
@CrossOrigin(origins = "*")
public class SensorController {

    private final SensorRepository sensorRepository;
    private final ReportRepository reportRepository;

    public SensorController(SensorRepository sensorRepository, ReportRepository reportRepository) {
        this.sensorRepository = sensorRepository;
        this.reportRepository = reportRepository;
    }

    // --- EXISTENTE: usado pelos gráficos ---
    @GetMapping
    public Map<String, Object> getSensoresData() {
        Map<String, Object> response = new HashMap<>();
        List<Sensor> sensores = sensorRepository.findAll();

        Set<String> horas = new TreeSet<>();
        sensores.forEach(sensor ->
                reportRepository.findBySensorIdOrderByReportTimeAsc(sensor.getId())
                        .forEach(r -> {
                            if (r.getReportTime() != null)
                                horas.add(r.getReportTime().getHour() + "h");
                        })
        );

        response.put("horas", horas);

        List<Map<String, Object>> sensoresData = sensores.stream().map(sensor -> {
            var reports = reportRepository.findBySensorIdOrderByReportTimeAsc(sensor.getId());
            Map<String, Object> s = new HashMap<>();
            s.put("nome", sensor.getSensorName());
            s.put("valores", reports.stream().map(r -> r.getGasLevel()).collect(Collectors.toList()));
            return s;
        }).collect(Collectors.toList());

        response.put("sensores", sensoresData);
        return response;
    }

    // --- NOVO: listar sensores simples ---
    @GetMapping("/listar")
    public List<Sensor> listarSensores() {
        return sensorRepository.findAll();
    }

    // --- NOVO: cadastrar sensor ---
    @PostMapping
    public Sensor cadastrarSensor(@RequestBody Sensor sensor) {
        return sensorRepository.save(sensor);
    }
}

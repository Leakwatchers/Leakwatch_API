package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Sensor;
import com.unifor.br.leakwatch.repository.ReportRepository;
import com.unifor.br.leakwatch.repository.UsuarioRepository;
import com.unifor.br.leakwatch.repository.SensorRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/vazamentos")
@CrossOrigin(origins = "*")
public class VazamentoController {

    private final SensorRepository sensorRepository;
    private final ReportRepository reportRepository;

    public VazamentoController(SensorRepository sensorRepository, ReportRepository reportRepository) {
        this.sensorRepository = sensorRepository;
        this.reportRepository = reportRepository;
    }

    @GetMapping
    public Map<String, Object> getVazamentos() {
        List<Sensor> sensores = sensorRepository.findAll();
        List<String> nomes = new ArrayList<>();
        List<Integer> quantidades = new ArrayList<>();

        for (Sensor s : sensores) {
            // CORREÇÃO: Usar getMacAddress() em vez de getId()
            var reports = reportRepository.findByMacAddressOrderByReportTimeAsc(s.getMacAddress());

            // Assumindo que o Report.java tem o método getGasLevel()
            int qtd = (int) reports.stream()
                    .filter(r -> r.getGasLevel() > 80.0)
                    .count();
            nomes.add(s.getSensorName());
            quantidades.add(qtd);
        }

        return Map.of("sensores", nomes, "quantidades", quantidades);
    }
}

package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Report;
import com.unifor.br.leakwatch.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportRepository repository;

    // --- GET: todos os relatórios ---
    @GetMapping
    public List<Report> listarTodos() {
        return repository.findAll();
    }

    // --- GET: relatórios por macAddress ---
    @GetMapping("/sensor/{macAddress}")
    public List<Report> listarPorSensor(@PathVariable String macAddress) {
        return repository.findByMacAddress(macAddress);
    }

    // --- POST: cria novo relatório ---
    @PostMapping
    public Report criar(@RequestBody Report report) {
        return repository.save(report);
    }

    // --- DELETE: remove um relatório pelo ID ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

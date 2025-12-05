package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Report;
import com.unifor.br.leakwatch.repository.ReportRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportRepository repo;

    public ReportController(ReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Report> listarTodos() {
        return repo.findAll();
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

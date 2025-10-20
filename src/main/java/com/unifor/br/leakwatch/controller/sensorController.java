package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.repository.sensorRepository;
import com.unifor.br.leakwatch.model.sensor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que é um Controller REST
@RequestMapping("/api/sensor") // URL base para todos os endpoints
public class sensorController {

    @Autowired
    private sensorRepository repository;

    // --- GET (Read) ---
    @GetMapping
    public List<sensor> listarTodos() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<sensor> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok) // Se encontrar, retorna 200 OK com o objeto
                .orElse(ResponseEntity.notFound().build()); // Se não, retorna 404 Not Found
    }

    // --- POST (Create) ---
    @PostMapping
    // Retorna o produto criado e o código 201 Created
    public sensor criarSensor(@RequestBody sensor sensor) {
        return repository.save(sensor);
    }

    // --- DELETE (Delete) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSensor(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.notFound().build(); // 404 Not Found
    }

    // --- PUT (Update) ---
    @PutMapping("/{id}")
    public ResponseEntity<sensor> atualizarsensor(@PathVariable Long id, @RequestBody sensor novosensor) {
        return repository.findById(id)
                .map(sensorExistente -> {
                    sensorExistente.setSensorName(novosensor.getSensorName());
                    sensorExistente.setSensorType(novosensor.getSensorType());
                    sensor atualizado = repository.save(sensorExistente);
                    return ResponseEntity.ok(atualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Sensor;
import com.unifor.br.leakwatch.repository.SensorRepository;
import com.unifor.br.leakwatch.MqttToPostgres;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private MqttToPostgres mqttService;

    public static class CadastroSensorDTO {
        public String ipAdress;
        public String sensorName;
        public String sensorType;
        public boolean isConnected;
    }

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Sensor> create(@RequestBody CadastroSensorDTO dto) {

        Sensor s = new Sensor();
        s.setIpAdress(dto.ipAdress);
        s.setSensorName(dto.sensorName);
        s.setSensorType(dto.sensorType);
        s.setIsConnected(dto.isConnected);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sensorRepository.save(s));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEW','MASTER')")
    public ResponseEntity<List<Sensor>> list() {
        return ResponseEntity.ok(sensorRepository.findAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Sensor> update(@PathVariable Long id,
                                         @RequestBody CadastroSensorDTO dto) {

        Optional<Sensor> opt = sensorRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Sensor s = opt.get();
        s.setSensorName(dto.sensorName);
        s.setSensorType(dto.sensorType);
        s.setIsConnected(dto.isConnected);

        return ResponseEntity.ok(sensorRepository.save(s));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!sensorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sensorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ping")
    @PreAuthorize("hasAnyRole('VIEW','MASTER')")
    public ResponseEntity<String> ping(@PathVariable Long id) {

        Optional<Sensor> opt = sensorRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sensor não encontrado");
        }

        Sensor sensor = opt.get();

        try {
            mqttService.sendCommand(sensor.getIpAdress(), "PING");

            return ResponseEntity.ok(
                    "PING enviado para " + sensor.getIpAdress() + ". Aguardando PONG..."
            );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar comando MQTT: " + ex.getMessage());
        }
    }
}
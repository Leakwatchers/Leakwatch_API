package com.unifor.br.leakwatch.controller;

import com.unifor.br.leakwatch.model.Sensor;
import com.unifor.br.leakwatch.repository.SensorRepository;
import com.unifor.br.leakwatch.MqttToPostgres;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // DTO para entrada
    public static class CadastroSensorDTO {
        public String macAddress;
        public String sensorName;
        public String sensorType;
        public boolean isConnected;
    }

    // MASTER → cria sensor
    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Sensor> create(@RequestBody CadastroSensorDTO dto) {

        if (sensorRepository.findById(dto.macAddress).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Sensor s = new Sensor();
        s.setMacAddress(dto.macAddress);
        s.setSensorName(dto.sensorName);
        s.setSensorType(dto.sensorType);
        s.setIsConnected(dto.isConnected);

        return ResponseEntity.status(HttpStatus.CREATED).body(sensorRepository.save(s));
    }

    // VIEW + MASTER → listar sensores
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEW','MASTER')")
    public ResponseEntity<List<Sensor>> list() {
        return ResponseEntity.ok(sensorRepository.findAll());
    }

    // MASTER → editar sensor
    @PutMapping("/{mac}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Sensor> update(@PathVariable String mac, @RequestBody CadastroSensorDTO dto) {

        Optional<Sensor> opt = sensorRepository.findById(mac);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Sensor s = opt.get();
        s.setSensorName(dto.sensorName);
        s.setSensorType(dto.sensorType);
        s.setIsConnected(dto.isConnected);

        return ResponseEntity.ok(sensorRepository.save(s));
    }

    // MASTER → remover sensor
    @DeleteMapping("/{mac}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> delete(@PathVariable String mac) {
        if (!sensorRepository.existsById(mac)) {
            return ResponseEntity.notFound().build();
        }
        sensorRepository.deleteById(mac);
        return ResponseEntity.noContent().build();
    }

    // VIEW + MASTER → enviar comando PING
    @PostMapping("/{mac}/ping")
    @PreAuthorize("hasAnyRole('VIEW','MASTER')")
    public ResponseEntity<String> ping(@PathVariable String mac) {

        Optional<Sensor> opt = sensorRepository.findById(mac);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sensor não encontrado");
        }

        try {
            mqttService.sendCommand(mac, "PING");

            return ResponseEntity.ok("PING enviado para " + mac + ". Aguardando PONG...");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar comando MQTT: " + ex.getMessage());
        }
    }
}

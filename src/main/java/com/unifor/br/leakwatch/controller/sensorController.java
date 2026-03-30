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

    public static class CadastroSensorDTO {
        public String ipAdress;
        public String sensorName;
        public String sensorType;
        public boolean isConnected;
    }

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Sensor> create(@RequestBody CadastroSensorDTO dto) {

        if (sensorRepository.findById(dto.ipAdress).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Sensor s = new Sensor();
        s.setIpAdress(dto.ipAdress);
        s.setSensorName(dto.sensorName);
        s.setSensorType(dto.sensorType);
        s.setIsConnected(dto.isConnected);

        return ResponseEntity.status(HttpStatus.CREATED).body(sensorRepository.save(s));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEW','MASTER')")
    public ResponseEntity<List<Sensor>> list() {
        return ResponseEntity.ok(sensorRepository.findAll());
    }

    @PutMapping("/{ip}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Sensor> update(@PathVariable String ip, @RequestBody CadastroSensorDTO dto) {

        Optional<Sensor> opt = sensorRepository.findById(ip);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Sensor s = opt.get();
        s.setSensorName(dto.sensorName);
        s.setSensorType(dto.sensorType);
        s.setIsConnected(dto.isConnected);

        return ResponseEntity.ok(sensorRepository.save(s));
    }

    @DeleteMapping("/{ip}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> delete(@PathVariable String ip) {
        if (!sensorRepository.existsById(ip)) {
            return ResponseEntity.notFound().build();
        }
        sensorRepository.deleteById(ip);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{ip}/ping")
    @PreAuthorize("hasAnyRole('VIEW','MASTER')")
    public ResponseEntity<String> ping(@PathVariable String ip) {

        Optional<Sensor> opt = sensorRepository.findById(ip);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sensor não encontrado");
        }

        try {
            mqttService.sendCommand(ip, "PING");

            return ResponseEntity.ok("PING enviado para " + ip + ". Aguardando PONG...");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar comando MQTT: " + ex.getMessage());
        }
    }
}

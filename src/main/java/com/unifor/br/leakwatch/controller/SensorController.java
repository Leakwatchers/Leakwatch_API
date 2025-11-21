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
@RequestMapping("/api/sensores" )
@CrossOrigin(origins = "*") // <--- CORREÇÃO APLICADA AQUI
public class SensorController {

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private MqttToPostgres mqttService;

    // DTO simplificado para o cadastro (agora inclui sensorType)
    public static class CadastroSensorDTO {
        public String sensorName;
        public String macAddress;
        public String sensorType;
    }

    /**
     * Endpoint para cadastrar um novo sensor. Apenas usuários com a role SUPER podem acessar.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<Sensor> cadastrarSensor(@RequestBody CadastroSensorDTO dto) {
        if (sensorRepository.findById(dto.macAddress).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Sensor já existe
        }

        Sensor novoSensor = new Sensor();
        novoSensor.setMacAddress(dto.macAddress);
        novoSensor.setSensorName(dto.sensorName);
        novoSensor.setSensorType(dto.sensorType);

        Sensor salvo = sensorRepository.save(novoSensor);
        return new ResponseEntity<>(salvo, HttpStatus.CREATED);
    }

    /**
     * Endpoint para listar todos os sensores. Apenas usuários com a role MIDDLE ou SUPER podem acessar.
     */
    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('MIDDLE', 'SUPER')")
    public ResponseEntity<List<Sensor>> listarSensores() {
        List<Sensor> sensores = sensorRepository.findAll();
        return new ResponseEntity<>(sensores, HttpStatus.OK);
    }

    /**
     * NOVO ENDPOINT: Envia o comando PING para um sensor específico via MQTT.
     * O frontend chamará este endpoint.
     */
    @PostMapping("/{macAddress}/ping")
    @PreAuthorize("hasAnyRole('MIDDLE', 'SUPER')")
    public ResponseEntity<String> pingSensor(@PathVariable String macAddress) {
        Optional<Sensor> sensorOpt = sensorRepository.findById(macAddress);
        if (sensorOpt.isEmpty()) {
            return new ResponseEntity<>("Sensor não encontrado", HttpStatus.NOT_FOUND);
        }

        try {
            mqttService.sendCommand(macAddress, "PING");

            return new ResponseEntity<>("Comando PING enviado para " + macAddress + ". Aguardando PONG...", HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Falha ao enviar comando MQTT para " + macAddress + ": " + e.getMessage());
            return new ResponseEntity<>("Falha ao enviar comando MQTT: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package com.unifor.br.leakwatch;

import com.unifor.br.leakwatch.model.Sensor;
import com.unifor.br.leakwatch.repository.SensorRepository;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocketFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MqttToPostgres implements MqttCallback {

    @Autowired
    private SensorRepository sensorRepository;

    // ==== CONFIGURAÇÕES DO BROKER HIVEMQ CLOUD ====
    private static final String MQTT_BROKER = "ssl://2d6a2b6382f6430cb4e1f780cfa73926.s1.eu.hivemq.cloud:8883";
    private static final String MQTT_STATUS_TOPIC = "leakwatch/sensor/#";
    private static final String MQTT_COMMAND_TOPIC_BASE = "leakwatch/cmd/";
    private static final String MQTT_CLIENT_ID = "JavaMQTTListenerService";

    private static final String MQTT_USER = "Vinicius";
    private static final String MQTT_PASS = "Vi123456";

    // ==== CONFIGURAÇÕES DO BANCO POSTGRES ====
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/leakwatch";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "5432";

    private MqttClient mqttClient;

    @PostConstruct
    public void init() {
        conectarMQTT();
    }

    private void conectarMQTT() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER, MQTT_CLIENT_ID);
            mqttClient.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setUserName(MQTT_USER);
            options.setPassword(MQTT_PASS.toCharArray());
            options.setSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());

            mqttClient.connect(options);

            System.out.println("✅ Conectado ao broker MQTT: " + MQTT_BROKER);

            mqttClient.subscribe(MQTT_STATUS_TOPIC);
            System.out.println("📡 Aguardando mensagens MQTT no tópico: " + MQTT_STATUS_TOPIC);

        } catch (MqttException e) {
            System.err.println("❌ Falha ao conectar ou subscrever ao MQTT.");
            e.printStackTrace();
        }
    }

    public void sendCommand(String macAddress, String command) throws MqttException {
        String topic = MQTT_COMMAND_TOPIC_BASE + macAddress;
        MqttMessage message = new MqttMessage(command.getBytes());
        message.setQos(1);

        mqttClient.publish(topic, message);

        System.out.println("📤 Comando '" + command + "' enviado para o tópico: " + topic);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("⚠ Conexão MQTT perdida: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        System.out.println("📥 Mensagem recebida [" + topic + "]: " + message);

        String macAddress = topic.substring(topic.lastIndexOf('/') + 1);

        // ---------- TRATAMENTO DE PONG ----------
        if ("PONG".equalsIgnoreCase(message.trim())) {
            handlePong(macAddress);
            return;
        }

        // ---------- TRATAMENTO DE TELEMETRIA ----------
        if (topic.startsWith("leakwatch/sensor/")) {
            handleSensorData(macAddress, message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    // ============================================================
    // TRATAMENTO DE PONG
    // ============================================================
    private void handlePong(String macAddress) {
        Optional<Sensor> sensorOpt = sensorRepository.findById(macAddress);

        if (sensorOpt.isPresent()) {
            Sensor sensor = sensorOpt.get();

            sensor.setIsConnected(true);
            sensor.setLastSeen(LocalDateTime.now());

            sensorRepository.save(sensor);

            System.out.println("📡 PONG recebido de " + macAddress + " → marcado como CONECTADO");
        } else {
            System.out.println("⚠ PONG de MAC desconhecido: " + macAddress);
        }
    }

    // ============================================================
    // TRATAMENTO DE TELEMETRIA DO SENSOR
    // ============================================================
    private void handleSensorData(String macAddress, String jsonMessage) {
        try {
            // Parsing manual simples (por performance)
            double gasLevel = extrairDouble(jsonMessage, "\"valor\":");
            String status = extrairString(jsonMessage, "\"status\":\"");

            salvarRelatorio(macAddress, gasLevel, status);

            // Atualiza informações do sensor
            Optional<Sensor> opt = sensorRepository.findById(macAddress);
            if (opt.isPresent()) {
                Sensor sensor = opt.get();

                sensor.setLastSeen(LocalDateTime.now());
                sensor.setIsConnected(true);

                sensorRepository.save(sensor);
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar dados do sensor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // EXTRAÇÃO DOS CAMPOS DO JSON
    // ============================================================
    private double extrairDouble(String json, String key) {
        int start = json.indexOf(key);
        if (start == -1) return 0;

        int end = json.indexOf("}", start);
        return Double.parseDouble(json.substring(start + key.length(), end).trim());
    }

    private String extrairString(String json, String key) {
        int start = json.indexOf(key);
        if (start == -1) return "";

        int end = json.indexOf("\"", start + key.length());
        return json.substring(start + key.length(), end);
    }

    // ============================================================
    // SALVAR RELATÓRIO NO BANCO POSTGRES
    // ============================================================
    private void salvarRelatorio(String macAddress, double gasLevel, String status) {
        String sql = "INSERT INTO report (gas_level, mac_address, report_time, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, gasLevel);
            stmt.setString(2, macAddress);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, status);

            stmt.executeUpdate();

            System.out.println("Telemetria salva (MAC: " + macAddress + ", Nível: " + gasLevel + ")");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar no banco: " + e.getMessage());
        }
    }

    // ============================================================
    // HEARTBEAT AUTOMÁTICO
    // ============================================================
    @Scheduled(fixedRate = 15000)
    public void verificarSensores() {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(30);

        sensorRepository.findAll().forEach(sensor -> {
            if (sensor.getLastSeen() == null || sensor.getLastSeen().isBefore(limite)) {
                if (Boolean.TRUE.equals(sensor.getIsConnected())) {
                    sensor.setIsConnected(false);
                    sensorRepository.save(sensor);
                    System.out.println("❌ Sensor OFFLINE: " + sensor.getMacAddress());
                }
            }
        });
    }
}

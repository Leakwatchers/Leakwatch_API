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

            System.out.println("Conectado ao broker MQTT: " + MQTT_BROKER);

            mqttClient.subscribe(MQTT_STATUS_TOPIC);
            System.out.println("Aguardando mensagens MQTT no tópico: " + MQTT_STATUS_TOPIC);

        } catch (MqttException e) {
            System.err.println("Falha ao conectar ou subscrever ao MQTT.");
            e.printStackTrace();
        }
    }

    public void sendCommand(String ipAdress, String command) throws MqttException {
        String topic = MQTT_COMMAND_TOPIC_BASE + ipAdress;
        MqttMessage message = new MqttMessage(command.getBytes());
        message.setQos(1);

        mqttClient.publish(topic, message);

        System.out.println("Comando '" + command + "' enviado para o tópico: " + topic);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("Conexão MQTT perdida: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        System.out.println("Mensagem recebida [" + topic + "]: " + message);

        String ipAdress = topic.substring(topic.lastIndexOf('/') + 1);

        if ("PONG".equalsIgnoreCase(message.trim())) {
            handlePong(ipAdress);
            return;
        }

        if (topic.startsWith("leakwatch/sensor/")) {
            handleSensorData(ipAdress, message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    private void handlePong(String ipAdress) {
        Optional<Sensor> sensorOpt = sensorRepository.findById(ipAdress);

        if (sensorOpt.isPresent()) {
            Sensor sensor = sensorOpt.get();

            sensor.setIsConnected(true);
            sensor.setLastSeen(LocalDateTime.now());

            sensorRepository.save(sensor);

            System.out.println("PONG recebido de " + ipAdress + " → marcado como CONECTADO");
        } else {
            System.out.println("PONG de IP desconhecido: " + ipAdress);
        }
    }

    private void handleSensorData(String ipAdress, String jsonMessage) {
        try {
            double gasLevel = extrairDouble(jsonMessage, "\"valor\":");
            String status = extrairString(jsonMessage, "\"status\":\"");

            salvarRelatorio(ipAdress, gasLevel, status);

            Optional<Sensor> opt = sensorRepository.findById(ipAdress);
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

    private void salvarRelatorio(String ipAdress, double gasLevel, String status) {
        String sql = "INSERT INTO report (gas_level, ipAdress, report_time, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, gasLevel);
            stmt.setString(2, ipAdress);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, status);

            stmt.executeUpdate();

            System.out.println("Telemetria salva (IP: " + ipAdress + ", Nível: " + gasLevel + ")");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar no banco: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 15000)
    public void verificarSensores() {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(30);

        sensorRepository.findAll().forEach(sensor -> {
            if (sensor.getLastSeen() == null || sensor.getLastSeen().isBefore(limite)) {
                if (Boolean.TRUE.equals(sensor.getIsConnected())) {
                    sensor.setIsConnected(false);
                    sensorRepository.save(sensor);
                    System.out.println("Sensor OFFLINE: " + sensor.getIpAdress());
                }
            }
        });
    }
}

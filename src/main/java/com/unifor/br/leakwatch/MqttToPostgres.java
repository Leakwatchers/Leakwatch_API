package com.unifor.br.leakwatch;

import com.unifor.br.leakwatch.model.Sensor;
import com.unifor.br.leakwatch.repository.SensorRepository;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
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
            System.out.println("Aguardando mensagens MQTT no tópico: " + MQTT_STATUS_TOPIC);

        } catch (MqttException e) {
            System.err.println("Falha ao conectar ou subscrever ao MQTT.");
            e.printStackTrace();
        }
    }

    public void sendCommand(String macAddress, String command) throws MqttException {
        String topic = MQTT_COMMAND_TOPIC_BASE + macAddress;
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
        System.out.println("Mensagem recebida no tópico [" + topic + "]: " + message);

        String macAddress = topic.substring(topic.lastIndexOf('/') + 1);

        if ("PONG".equals(message)) {
            handlePong(macAddress);
            return;
        }

        if (topic.startsWith("leakwatch/sensor/")) {
            handleSensorData(macAddress, message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    private void handlePong(String macAddress) {
        Optional<Sensor> sensorOpt = sensorRepository.findById(macAddress);
        if (sensorOpt.isPresent()) {
            Sensor sensor = sensorOpt.get();
            sensor.setIsConnected(true);
            sensorRepository.save(sensor);
            System.out.println("📡 PONG recebido de " + macAddress + ". Status atualizado para CONECTADO.");
        } else {
            System.out.println("PONG recebido de MAC desconhecido: " + macAddress);
        }
    }

    private void handleSensorData(String macAddress, String jsonMessage) {
        try {
            double gasLevel = 0.0;

            String valorKey = "\"valor\":";
            int start = jsonMessage.indexOf(valorKey);
            if (start != -1) {
                int end = jsonMessage.indexOf("}", start);
                String valorStr = jsonMessage.substring(start + valorKey.length(), end).trim();
                gasLevel = Double.parseDouble(valorStr);
            } else {
                System.err.println("Erro ao extrair 'valor' do JSON: " + jsonMessage);
                return;
            }

            String statusKey = "\"status\":\"";
            String status = "SEGURO";
            start = jsonMessage.indexOf(statusKey);
            if (start != -1) {
                int end = jsonMessage.indexOf("\"", start + statusKey.length());
                status = jsonMessage.substring(start + statusKey.length(), end);
            }

            salvarNoBanco(macAddress, gasLevel, status);

            Optional<Sensor> sensorOpt = sensorRepository.findById(macAddress);
            if (sensorOpt.isPresent() && !sensorOpt.get().getIsConnected()) {
                Sensor sensor = sensorOpt.get();
                sensor.setIsConnected(true);
                sensorRepository.save(sensor);
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar dados do sensor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void salvarNoBanco(String macAddress, double gasLevel, String status) {
        String sql = "INSERT INTO report (gas_level, mac_address, report_time, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, gasLevel);
            stmt.setString(2, macAddress);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, status);
            stmt.executeUpdate();

            System.out.println("💾 Valor inserido no banco (MAC: " + macAddress + ", Nível: " + gasLevel + ")");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar no banco de dados: " + e.getMessage());
        }
    }
}

package com.unifor.br.leakwatch;

import com.unifor.br.leakwatch.model.Report;
import com.unifor.br.leakwatch.model.Sensor;
import com.unifor.br.leakwatch.repository.ReportRepository;
import com.unifor.br.leakwatch.repository.SensorRepository;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocketFactory;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MqttToPostgres implements MqttCallback {

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ReportRepository reportRepository;

    private static final String MQTT_BROKER = "ssl://2d6a2b6382f6430cb4e1f780cfa73926.s1.eu.hivemq.cloud:8883";
    private static final String MQTT_STATUS_TOPIC = "leakwatch/sensor/#";
    private static final String MQTT_COMMAND_TOPIC_BASE = "leakwatch/cmd/";
    private static final String MQTT_CLIENT_ID = "JavaMQTTListenerService";

    private static final String MQTT_USER = "Vinicius";
    private static final String MQTT_PASS = "Vi123456";

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

            System.out.println("Conectado ao MQTT");
            mqttClient.subscribe(MQTT_STATUS_TOPIC);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String ipAdress, String command) throws MqttException {
        String topic = MQTT_COMMAND_TOPIC_BASE + ipAdress;
        MqttMessage message = new MqttMessage(command.getBytes());
        message.setQos(1);

        mqttClient.publish(topic, message);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("MQTT caiu: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {

        String message = new String(mqttMessage.getPayload());
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
        Optional<Sensor> sensorOpt = sensorRepository.findByIpAdress(ipAdress);

        sensorOpt.ifPresent(sensor -> {
            sensor.setIsConnected(true);
            sensor.setLastSeen(LocalDateTime.now());
            sensorRepository.save(sensor);

            System.out.println("PONG de " + ipAdress);
        });
    }

    private void handleSensorData(String ipAdress, String jsonMessage) {
        try {
            double gasLevel = extrairDouble(jsonMessage, "\"valor\":");
            String status = extrairString(jsonMessage, "\"status\":\"");

            Optional<Sensor> opt = sensorRepository.findByIpAdress(ipAdress);

            if (opt.isEmpty()) {
                System.out.println("Sensor não cadastrado: " + ipAdress);
                return;
            }

            Sensor sensor = opt.get();

            // 🔥 salva com relacionamento correto
            Report report = new Report();
            report.setGasLevel(gasLevel);
            report.setStatus(status);
            report.setReportTime(LocalDateTime.now());
            report.setSensor(sensor);
            report.setSensorIp(ipAdress);

            reportRepository.save(report);

            // atualiza sensor
            sensor.setLastSeen(LocalDateTime.now());
            sensor.setIsConnected(true);
            sensorRepository.save(sensor);

        } catch (Exception e) {
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

    @Scheduled(fixedRate = 15000)
    public void verificarSensores() {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(30);

        sensorRepository.findAll().forEach(sensor -> {
            if (sensor.getLastSeen() == null || sensor.getLastSeen().isBefore(limite)) {
                if (Boolean.TRUE.equals(sensor.getIsConnected())) {
                    sensor.setIsConnected(false);
                    sensorRepository.save(sensor);
                    System.out.println("OFFLINE: " + sensor.getIpAdress());
                }
            }
        });
    }
}
package com.unifor.br.leakwatch;

import org.eclipse.paho.client.mqttv3.*;
import javax.net.ssl.SSLSocketFactory;
import java.sql.*;
import java.time.LocalDateTime;

public class MqttToPostgres {

    // ==== CONFIGURAÇÕES DO BROKER HIVEMQ CLOUD ====
    private static final String MQTT_BROKER = "ssl://2d6a2b6382f6430cb4e1f780cfa73926.s1.eu.hivemq.cloud:8883";
    private static final String MQTT_TOPIC = "teste/mq2/status";
    private static final String MQTT_CLIENT_ID = "JavaMQTTListener";
    private static final String MQTT_USER = "Vinicius";
    private static final String MQTT_PASS = "Vi123456";

    // ==== CONFIGURAÇÕES DO BANCO POSTGRES ====
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/leakwatch";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "5432";

    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(MQTT_BROKER, MQTT_CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setUserName(MQTT_USER);
            options.setPassword(MQTT_PASS.toCharArray());
            options.setSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());

            client.connect(options);
            System.out.println("✅ Conectado ao broker MQTT: " + MQTT_BROKER);

            client.subscribe(MQTT_TOPIC, (topic, msg) -> {
                String message = new String(msg.getPayload());
                System.out.println("Mensagem recebida: " + message);

                double gasLevel = extrairValor(message);
                if (gasLevel >= 0) {
                    salvarNoBanco(gasLevel);
                } else {
                    System.out.println("Mensagem ignorada (sem valor numérico válido).");
                }
            });

            System.out.println("Aguardando mensagens MQTT...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double extrairValor(String msg) {
        try {
            int idx = msg.indexOf("Valor:");
            if (idx == -1) return -1;
            String valor = msg.substring(idx + 6).trim();
            return Double.parseDouble(valor);
        } catch (Exception e) {
            return -1;
        }
    }

    private static void salvarNoBanco(double gasLevel) {
        String sql = "INSERT INTO report (id, gas_level, sensor_id, report_time) VALUES (DEFAULT, ?, 1, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, gasLevel);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

            System.out.println("💾 Valor inserido no banco: " + gasLevel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

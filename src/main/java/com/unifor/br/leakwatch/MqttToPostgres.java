package com.unifor.br.leakwatch;

<<<<<<< HEAD
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
=======
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

    // Injetando o repositório para atualizar o status do sensor
    @Autowired
    private SensorRepository sensorRepository;

    // ==== CONFIGURAÇÕES DO BROKER HIVEMQ CLOUD ====
    private static final String MQTT_BROKER = "ssl://2d6a2b6382f6430cb4e1f780cfa73926.s1.eu.hivemq.cloud:8883";
    // Tópico de status genérico para escutar todos os sensores
    private static final String MQTT_STATUS_TOPIC = "leakwatch/sensor/#";
    private static final String MQTT_COMMAND_TOPIC_BASE = "leakwatch/cmd/"; // Base para enviar comandos
    private static final String MQTT_CLIENT_ID = "JavaMQTTListenerService";
    private static final String MQTT_USER = "Vinicius";
    private static final String MQTT_PASS = "Vi123456";

    // ==== CONFIGURAÇÕES DO BANCO POSTGRES (Manter para a lógica de salvar dados) ====
>>>>>>> 4315d37 (Ping Pong)
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/leakwatch";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "5432";

<<<<<<< HEAD
    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(MQTT_BROKER, MQTT_CLIENT_ID);
=======
    private MqttClient mqttClient;

    /**
     * Inicializa a conexão MQTT após a construção do bean Spring.
     */
    @PostConstruct
    public void init() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER, MQTT_CLIENT_ID);
            mqttClient.setCallback(this);

>>>>>>> 4315d37 (Ping Pong)
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setUserName(MQTT_USER);
            options.setPassword(MQTT_PASS.toCharArray());
            options.setSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());

<<<<<<< HEAD
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
=======
            mqttClient.connect(options);
            System.out.println("✅ Conectado ao broker MQTT: " + MQTT_BROKER);

            // Subscreve ao tópico genérico de status
            mqttClient.subscribe(MQTT_STATUS_TOPIC);
            System.out.println("Aguardando mensagens MQTT no tópico: " + MQTT_STATUS_TOPIC);

        } catch (MqttException e) {
            System.err.println("Falha ao conectar ou subscrever ao MQTT.");
>>>>>>> 4315d37 (Ping Pong)
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
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
=======
    /**
     * Método chamado pelo SensorController para enviar comandos (como PING) ao ESP32.
     * @param macAddress O MAC Address do sensor alvo.
     * @param command O comando a ser enviado (ex: "PING", "LIGAR_LED").
     */
    public void sendCommand(String macAddress, String command) throws MqttException {
        String topic = MQTT_COMMAND_TOPIC_BASE + macAddress;
        MqttMessage message = new MqttMessage(command.getBytes());
        message.setQos(1);
        mqttClient.publish(topic, message);
        System.out.println("Comando '" + command + "' enviado para o tópico: " + topic);
    }

    // ====================================================================================
    // ==== IMPLEMENTAÇÃO DO MqttCallback (Listener) ====
    // ====================================================================================

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("Conexão MQTT perdida: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        System.out.println("Mensagem recebida no tópico [" + topic + "]: " + message);

        // 1. Extrair o MAC Address do tópico
        // O tópico é "leakwatch/sensor/MAC_ADDRESS"
        String macAddress = topic.substring(topic.lastIndexOf('/') + 1);

        // 2. Tratar a mensagem de PONG
        if ("PONG".equals(message)) {
            handlePong(macAddress);
            return;
        }

        // 3. Tratar a mensagem de dados (JSON)
        if (topic.startsWith("leakwatch/sensor/")) {
            handleSensorData(macAddress, message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Não é necessário implementar para este caso
    }

    /**
     * Trata a resposta PONG do sensor, atualizando o status de conexão no banco.
     */
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

    /**
     * Trata a mensagem de dados do sensor (JSON) e salva no banco.
     */
    private void handleSensorData(String macAddress, String jsonMessage) {
        // O ESP32 envia: {"mac":"AA:BB:CC:DD:EE:FF", "status":"ALERTA", "valor":2500.0}
        try {
            // **IMPORTANTE:** Você deve usar uma biblioteca JSON (como Jackson ou Gson) para parsear o JSON.
            // Como não sei qual você usa, vou simular a extração do valor.

            // Simulação de extração do valor (DEVE SER SUBSTITUÍDO POR UM PARSER JSON REAL)
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

            // Simulação de extração do status
            String statusKey = "\"status\":\"";
            String status = "SEGURO";
            start = jsonMessage.indexOf(statusKey);
            if (start != -1) {
                int end = jsonMessage.indexOf("\"", start + statusKey.length());
                status = jsonMessage.substring(start + statusKey.length(), end);
            }

            // Lógica de salvar no banco (adaptada para usar o macAddress)
            salvarNoBanco(macAddress, gasLevel, status);

            // Atualizar o status de conexão (se estiver enviando dados, está conectado)
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

    /**
     * Salva o nível de gás no banco de dados, usando o MAC Address como chave estrangeira.
     */
    private void salvarNoBanco(String macAddress, double gasLevel, String status) {
        // SQL ajustado para usar mac_address como chave estrangeira
        String sql = "INSERT INTO report (gas_level, mac_address, report_time, status) VALUES (?, ?, ?, ?)";
>>>>>>> 4315d37 (Ping Pong)
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, gasLevel);
<<<<<<< HEAD
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

            System.out.println("💾 Valor inserido no banco: " + gasLevel);
        } catch (SQLException e) {
            e.printStackTrace();
=======
            stmt.setString(2, macAddress); // Agora usa o MAC Address
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, status);
            stmt.executeUpdate();

            System.out.println("💾 Valor inserido no banco (MAC: " + macAddress + ", Nível: " + gasLevel + ")");
        } catch (SQLException e) {
            System.err.println("Erro ao salvar no banco de dados: " + e.getMessage());
>>>>>>> 4315d37 (Ping Pong)
        }
    }
}

package com.unifor.br.leakwatch.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Serviço de alertas via WhatsApp usando a API REST do Twilio.
 *
 * Configure as propriedades em application.properties:
 *   twilio.account-sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 *   twilio.auth-token=your_auth_token
 *   twilio.from-number=whatsapp:+14155238886
 *   twilio.to-number=whatsapp:+5585999999999
 *   leakwatch.gas.threshold=300.0
 *   leakwatch.alert.cooldown-seconds=60
 */
@Service
public class WhatsAppAlertService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @Value("${twilio.to-number}")
    private String toNumber;

    @Value("${leakwatch.gas.threshold:300.0}")
    private double gasThreshold;

    @Value("${leakwatch.alert.cooldown-seconds:60}")
    private long cooldownSeconds;

    private long lastAlertEpoch = 0;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Chamado pelo MqttToPostgres ao receber telemetria.
     * Envia alerta via WhatsApp se o nível de gás ultrapassar o limiar configurado.
     *
     * @param gasLevel   nível de gás lido do sensor (ex: 450.5)
     * @param macAddress MAC do sensor que gerou a leitura
     * @param status     status do sensor (ex: "ALERTA", "NORMAL")
     */
    public void verificarEAlertar(double gasLevel, String macAddress, String status) {
        if (gasLevel < gasThreshold) {
            return; // Nível normal, nada a fazer
        }

        long agora = System.currentTimeMillis() / 1000;
        if (agora - lastAlertEpoch < cooldownSeconds) {
            System.out.printf("[WhatsApp] ⏳ Cooldown ativo (%ds restantes). Alerta suprimido para %s%n",
                    cooldownSeconds - (agora - lastAlertEpoch), macAddress);
            return;
        }

        lastAlertEpoch = agora;
        String mensagem = montarMensagem(gasLevel, macAddress, status);
        enviarMensagem(mensagem);
    }

    private String montarMensagem(double gasLevel, String macAddress, String status) {
        String horario = LocalDateTime.now().format(FORMATTER);
        return String.format(
                "🚨 *ALERTA LEAKWATCH* 🚨\n\n" +
                "⚠️ Nível de gás CRÍTICO detectado!\n\n" +
                "📍 Sensor: %s\n" +
                "📊 Leitura: %.1f ppm\n" +
                "🔴 Limite: %.1f ppm\n" +
                "📌 Status: %s\n" +
                "⏰ Horário: %s\n\n" +
                "_Verifique o local imediatamente e ventile o ambiente._",
                macAddress, gasLevel, gasThreshold, status, horario
        );
    }

    private void enviarMensagem(String mensagem) {
        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

            String body = "From=" + encode(fromNumber) +
                          "&To="   + encode(toNumber) +
                          "&Body=" + encode(mensagem);

            String credenciais = Base64.getEncoder()
                    .encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + credenciais)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                System.out.println("[WhatsApp] ✅ Alerta enviado com sucesso!");
            } else {
                System.err.println("[WhatsApp] ❌ Falha. Status HTTP: " + response.statusCode());
                System.err.println("[WhatsApp] Resposta Twilio: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("[WhatsApp] ❌ Erro ao chamar API Twilio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String encode(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}

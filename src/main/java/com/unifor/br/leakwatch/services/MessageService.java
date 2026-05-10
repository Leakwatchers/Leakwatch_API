package com.unifor.br.leakwatch.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.unifor.br.leakwatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {

    String sid = System.getenv("TWILIO_SID");
    String token = System.getenv("TWILIO_TOKEN");

    private static final String FROM =
            "whatsapp:+14155238886";

    // contador de alertas consecutivos
    private final Map<String, Integer> contadorAlerta =
            new HashMap<>();

    // cooldown do último alerta
    private final Map<String, LocalDateTime> ultimoAlerta =
            new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    public MessageService() {

        Twilio.init(
                ACCOUNT_SID,
                AUTH_TOKEN
        );
    }

    public void verificarEAlertar(
            double gasLevel,
            String sensorIp,
            String status,
            String sensorName
    ) {

        // ignora OK
        if (status.startsWith("OK")) {

            contadorAlerta.put(
                    sensorIp,
                    0
            );

            return;
        }

        // incrementa contador
        int quantidade =
                contadorAlerta.getOrDefault(
                        sensorIp,
                        0
                ) + 1;

        contadorAlerta.put(
                sensorIp,
                quantidade
        );

        // precisa de 3 leituras seguidas
        if (quantidade < 3) {
            return;
        }

        LocalDateTime agora =
                LocalDateTime.now();

        LocalDateTime ultimo =
                ultimoAlerta.get(sensorIp);

        // cooldown de 3 minutos
        if (
                ultimo != null &&
                        ultimo.plusMinutes(3).isAfter(agora)
        ) {

            System.out.println(
                    "Cooldown ativo: " +
                            sensorName
            );

            return;
        }

        String mensagem =
                "⚠ ALERTA DE GÁS\n\n" +
                        "Sensor: " + sensorName + "\n" +
                        "Nível: " + gasLevel + " ppm\n" +
                        "Status: " + status + "\n\n" +
                        "3 medições consecutivas detectadas.";

        userRepository.findAll().forEach(user -> {

            String telefone =
                    user.getTelefone();

            if (
                    telefone != null &&
                            !telefone.isBlank()
            ) {

                enviarMensagem(
                        telefone,
                        mensagem
                );
            }
        });

        ultimoAlerta.put(
                sensorIp,
                agora
        );

        System.out.println(
                "ALERTA ENVIADO: " +
                        sensorName
        );
    }

    private void enviarMensagem(
            String telefone,
            String mensagem
    ) {

        try {

            telefone =
                    formatarTelefone(
                            telefone
                    );

            Message msg =
                    Message.creator(

                            new PhoneNumber(
                                    "whatsapp:" + telefone
                            ),

                            new PhoneNumber(
                                    FROM
                            ),

                            mensagem

                    ).create();

            System.out.println(
                    "====== TWILIO ======"
            );

            System.out.println(
                    "TO: " + telefone
            );

            System.out.println(
                    "SID: " + msg.getSid()
            );

            System.out.println(
                    "STATUS: " + msg.getStatus()
            );

            System.out.println(
                    "===================="
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private String formatarTelefone(
            String telefone
    ) {

        telefone = telefone
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace(" ", "");

        if (!telefone.startsWith("+55")) {

            telefone =
                    "+55" + telefone;
        }

        return telefone;
    }
}
// Exemplo: src/main/java/com/exemplo/api/model/Sensor.java
package com.unifor.br.leakwatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data; // Importação necessária do Lombok
import lombok.NoArgsConstructor; // Opcional, mas útil
import lombok.AllArgsConstructor; // Opcional, mas útil

@Entity // Indica que é uma entidade JPA (tabela no banco)
@Data // Essa anotação gera Getters, Setters, toString, equals e hashCode
@NoArgsConstructor // Opcional: Gera um construtor sem argumentos
public class sensor {
    @Id // Chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serial;
    private String sensorName; //
    private String sensorType;


}
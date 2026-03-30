package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

// CORREÇÃO: O tipo da chave primária deve ser String (para o macAddress)
public interface SensorRepository extends JpaRepository<Sensor, String> { }

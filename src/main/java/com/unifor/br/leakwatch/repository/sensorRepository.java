package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> { }

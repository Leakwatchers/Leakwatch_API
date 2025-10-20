package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.sensor;
import org.springframework.data.jpa.repository.JpaRepository;


public interface sensorRepository extends JpaRepository<sensor, Long> {

}
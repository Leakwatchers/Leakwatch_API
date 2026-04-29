package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findByIpAdress(String ipAdress);

}
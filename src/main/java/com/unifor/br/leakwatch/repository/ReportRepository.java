package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findBySensorIdOrderByReportTimeAsc(Long sensorId);

    List<Report> findBySensorIdOrderByReportTimeDesc(Long sensorId);

    Report findTopBySensorIdOrderByReportTimeDesc(Long sensorId);

    @Query("SELECT r FROM Report r WHERE r.status = 'ALERTA' ORDER BY r.reportTime DESC")
    List<Report> findAlertas();

}
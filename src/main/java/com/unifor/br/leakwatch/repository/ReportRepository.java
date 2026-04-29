package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findBySensorIdOrderByReportTimeAsc(Long sensorId);

    List<Report> findBySensorIdOrderByReportTimeDesc(Long sensorId);

    Report findTopBySensorIdOrderByReportTimeDesc(Long sensorId);

    // Todos os reports acima de OK, ordenados do mais recente
    @Query("SELECT r FROM Report r WHERE r.status IN ('ALERTA', 'CRITICO', 'RISCO') ORDER BY r.reportTime DESC")
    List<Report> findAlertas();

    // Filtro por status específico
    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.reportTime DESC")
    List<Report> findByStatus(@Param("status") String status);

    List<Report> findBySensorIdAndReportTimeBetweenOrderByReportTimeAsc(
            Long sensorId,
            LocalDateTime start,
            LocalDateTime end
    );
}
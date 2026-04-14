package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findBySensorIpOrderByReportTimeAsc(String sensorIp);

    List<Report> findBySensorIpOrderByReportTimeDesc(String sensorIp);

    @Query("SELECT r FROM Report r WHERE r.status = 'ALERTA' ORDER BY r.reportTime DESC")
    List<Report> findAlertas();

    @Query("SELECT r FROM Report r WHERE r.sensorIp = :sensorIp ORDER BY r.reportTime DESC")
    List<Report> findUltimoBySensorIp(@Param("sensorIp") String sensorIp);

}
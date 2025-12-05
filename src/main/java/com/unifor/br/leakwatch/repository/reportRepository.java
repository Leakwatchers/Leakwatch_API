package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByMacAddressOrderByReportTimeAsc(String macAddress);

    List<Report> findByMacAddressOrderByReportTimeDesc(String macAddress);

    @Query("SELECT r FROM Report r WHERE r.status = 'ALERTA' ORDER BY r.reportTime DESC")
    List<Report> findAlertas();

    @Query("SELECT r FROM Report r WHERE r.macAddress = :mac ORDER BY r.reportTime DESC LIMIT 1")
    Report findUltimoByMacAddress(String mac);

}

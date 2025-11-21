package com.unifor.br.leakwatch.repository;

import com.unifor.br.leakwatch.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // NOVOS MÉTODOS usando macAddress (String)
    List<Report> findByMacAddressOrderByReportTimeAsc(String macAddress);
    List<Report> findByMacAddress(String macAddress);
}

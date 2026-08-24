package com.hazard.repository.hazard;

import com.hazard.domain.hazard.EmdatFloodRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Repository for EM-DAT Flood Records (hazard.emdat_flood_records)
 */
@Repository
public interface EmdatFloodRecordRepository extends JpaRepository<EmdatFloodRecord, Integer> {

    List<EmdatFloodRecord> findByYearOrderByYearDesc(Integer year);

    List<EmdatFloodRecord> findByYearBetweenOrderByYearDesc(Integer startYear, Integer endYear);

    List<EmdatFloodRecord> findByCountryIgnoreCaseOrderByYearDesc(String country);

    List<EmdatFloodRecord> findByIsoIgnoreCaseOrderByYearDesc(String iso);

    List<EmdatFloodRecord> findAllByOrderByTotalAffectedDesc();
}

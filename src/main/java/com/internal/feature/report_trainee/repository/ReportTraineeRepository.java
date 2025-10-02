package com.internal.feature.report_trainee.repository;

import com.internal.feature.report_trainee.models.TraineeReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportTraineeRepository extends JpaRepository<TraineeReport, Long>, JpaSpecificationExecutor<TraineeReport> {
}
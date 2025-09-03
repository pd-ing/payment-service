package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.MissionExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRepository extends JpaRepository<MissionExecution, String> {
}
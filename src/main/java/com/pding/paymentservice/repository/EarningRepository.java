package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Earning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EarningRepository extends JpaRepository<Earning, Long> {
    Optional<Earning> findByUserId(String userId);
}

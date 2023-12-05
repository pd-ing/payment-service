package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, String> {
    Optional<Withdrawal> findByTransactionId(String transactionId);
}

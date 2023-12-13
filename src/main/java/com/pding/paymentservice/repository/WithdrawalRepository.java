package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, String> {
    Optional<Withdrawal> findByTransactionId(String transactionId);

    List<Withdrawal> findByPdUserIdAndStatus(String pdUserId, WithdrawalStatus status);

    List<Withdrawal> findByPdUserId(String pdUserId);

    List<Withdrawal> findByPdUserIdOrderByCreatedDateDesc(String pdUserId);

    Optional<Withdrawal> findByPdUserIdAndTransactionId(String pdUserId, String transactionId);

}

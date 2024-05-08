package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.ReferralCommission;
import com.pding.paymentservice.models.enums.CommissionTransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralCommissionRepository extends JpaRepository<ReferralCommission, String> {

    Optional<ReferralCommission> findByWithdrawalId(String withdrawalId);

    List<ReferralCommission> findByCommissionTransferStatusIn(List<CommissionTransferStatus> statuses);
}

package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRespository extends JpaRepository<Ledger, Long> {
}

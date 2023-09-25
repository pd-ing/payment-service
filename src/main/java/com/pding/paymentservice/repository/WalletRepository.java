package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Wallet;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findWalletByUserID(Long userID);
    
}
package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Wallet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
    Optional<Wallet> findWalletByUserId(String userID);


    @Query("SELECT COALESCE(SUM(w.trees), 0) FROM Wallet w")
    BigDecimal sumOfAllTreesForUser();

}
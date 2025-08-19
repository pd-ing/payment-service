package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.LivestreamPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LivestreamPurchaseRepository extends JpaRepository<LivestreamPurchase, String> {
}
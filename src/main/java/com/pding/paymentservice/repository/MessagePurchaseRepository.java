package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.MessagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

public interface MessagePurchaseRepository extends JpaRepository<MessagePurchase, String> {
}

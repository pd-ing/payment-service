package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation, Long> {
}

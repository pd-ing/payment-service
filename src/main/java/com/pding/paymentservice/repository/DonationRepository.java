package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, String> {
    List<Donation> findByDonorUserId(String donorUserId);

    List<Donation> findByPdUserId(String pdUserId);

}

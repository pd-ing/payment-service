package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.PdRewardsUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PdRewardsUsersRepository extends JpaRepository<PdRewardsUsers, String> {
    Optional<PdRewardsUsers> findByPdUserId(String pdUserId);
}

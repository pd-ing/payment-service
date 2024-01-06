package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.CallDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallRepository extends JpaRepository<CallDetails, String> {

}



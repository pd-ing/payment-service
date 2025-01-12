package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.MExposureTicket;
import com.pding.paymentservice.models.enums.ExposureTicketType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExposureTicketRepository extends JpaRepository<MExposureTicket, ExposureTicketType> {
}

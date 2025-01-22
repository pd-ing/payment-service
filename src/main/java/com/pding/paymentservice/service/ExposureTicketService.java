package com.pding.paymentservice.service;

import com.pding.paymentservice.models.MExposureTicket;
import com.pding.paymentservice.repository.ExposureTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExposureTicketService {
    private final ExposureTicketRepository exposureTicketRepository;

    public List<MExposureTicket> getTicketPrices() {
        return exposureTicketRepository.findAll();
    }
}

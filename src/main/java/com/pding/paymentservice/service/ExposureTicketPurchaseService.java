package com.pding.paymentservice.service;

import com.pding.paymentservice.models.ExposureTicketPurchase;
import com.pding.paymentservice.models.MExposureTicket;
import com.pding.paymentservice.models.enums.ExposureTicketStatus;
import com.pding.paymentservice.models.enums.ExposureTicketType;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.repository.ExposureTicketPurchaseRepository;
import com.pding.paymentservice.repository.ExposureTicketRepository;
import com.pding.paymentservice.security.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class ExposureTicketPurchaseService {
    private final ExposureTicketRepository exposureTicketRepository;
    private final WalletService walletService;
    private final AuthHelper authHelper;
    private final ExposureTicketPurchaseRepository exposureTicketPurchaseRepository;
    private final LedgerService ledgerService;

    @Transactional
    public ExposureTicketPurchase buyTicket(ExposureTicketType type) {
        String userId = authHelper.getUserId();
        MExposureTicket ticket = exposureTicketRepository.findById(type).orElseThrow(() -> new IllegalArgumentException("Invalid ticket type"));
        BigDecimal ticketPrice = ticket.getPrice();

        walletService.deductTreesFromWallet(userId, ticketPrice);
        ExposureTicketPurchase  purchase = new ExposureTicketPurchase();
        purchase.setUserId(userId);
        purchase.setType(type);
        purchase.setTreesConsumed(ticketPrice);
        purchase.setPurchasedDate(LocalDateTime.now());
        purchase.setStatus(ExposureTicketStatus.UNUSED);
        purchase = exposureTicketPurchaseRepository.save(purchase);

        ledgerService.saveToLedger(purchase.getId(), ticketPrice, new BigDecimal(0), TransactionType.BUY_EXPOSURE_TICKET, userId);
        return purchase;
    }

    public Page<ExposureTicketPurchase> getPurchasedTicketOfUser(Pageable pageable) {
        String userId = authHelper.getUserId();
        return exposureTicketPurchaseRepository.findByUserId(userId, pageable);
    }

    public ExposureTicketPurchase useTicket(String ticketId) {
        String userId = authHelper.getUserId();
        ExposureTicketPurchase purchase = exposureTicketPurchaseRepository.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Invalid ticket id"));
        if(!userId.equalsIgnoreCase(purchase.getUserId())) {
            throw new IllegalArgumentException("Ticket does not belong to user");
        }
        if (purchase.getStatus() == ExposureTicketStatus.USED) {
            throw new IllegalArgumentException("Ticket already used");
        }
        purchase.setStatus(ExposureTicketStatus.USED);
        purchase.setUsedDate(LocalDateTime.now());

        //TODO: Add logic to use exposure ticket
        return exposureTicketPurchaseRepository.save(purchase);
    }
}

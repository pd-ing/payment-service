package com.pding.paymentservice.service;

import com.pding.paymentservice.aws.SendNotificationSqsMessage;
import com.pding.paymentservice.models.ExposureTicketPurchase;
import com.pding.paymentservice.models.MExposureSlot;
import com.pding.paymentservice.models.MExposureTicket;
import com.pding.paymentservice.models.enums.ExposureTicketStatus;
import com.pding.paymentservice.models.enums.ExposureTicketType;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.UserLite;
import com.pding.paymentservice.repository.ExposureSlotRepository;
import com.pding.paymentservice.repository.ExposureTicketPurchaseRepository;
import com.pding.paymentservice.repository.ExposureTicketRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.TokenSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ExposureTicketPurchaseService {
    private final ExposureTicketRepository exposureTicketRepository;
    private final WalletService walletService;
    private final AuthHelper authHelper;
    private final ExposureTicketPurchaseRepository exposureTicketPurchaseRepository;
    private final ExposureSlotRepository exposureSlotRepository;
    private final LedgerService ledgerService;
    private final UserServiceNetworkManager userServiceNetworkManager;
    private final TokenSigner tokenSigner;
    private final SendNotificationSqsMessage sendNotificationSqsMessage;

    @Transactional
    public ExposureTicketPurchase buyTicket(ExposureTicketType type) {
        String userId = authHelper.getUserId();
        MExposureTicket ticket = exposureTicketRepository.findById(type).orElseThrow(() -> new IllegalArgumentException("Invalid ticket type"));
        BigDecimal ticketPrice = ticket.getPrice();

        walletService.deductTreesFromWallet(userId, ticketPrice);
        ExposureTicketPurchase purchase = new ExposureTicketPurchase();
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
        if (!userId.equalsIgnoreCase(purchase.getUserId())) {
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

    public List<UserLite> getTopExposurePds() throws Exception {
        List<MExposureSlot> exposureSlots = exposureSlotRepository.findAll();
        Set<String> userIds = exposureSlots.stream().map(MExposureSlot::getUserId).collect(Collectors.toSet());

        List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(userIds).blockFirst();
        return usersFlux.stream().map(user -> UserLite.fromPublicUserNet(user, tokenSigner)).collect(Collectors.toList());
    }

    public void forceReleaseTicket(String userId) {
        exposureSlotRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User does not have exposure slot"));
        exposureSlotRepository.deleteById(userId);
        sendNotificationSqsMessage.sendForceReleaseTopExposureNotification(userId);
    }
}

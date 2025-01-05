package com.pding.paymentservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pding.paymentservice.aws.SendNotificationSqsMessage;
import com.pding.paymentservice.models.ExposureTicketPurchase;
import com.pding.paymentservice.models.MExposureSlot;
import com.pding.paymentservice.models.MExposureSlotHistory;
import com.pding.paymentservice.models.MExposureTicket;
import com.pding.paymentservice.models.enums.ExposureSlotNumber;
import com.pding.paymentservice.models.enums.ExposureTicketStatus;
import com.pding.paymentservice.models.enums.ExposureTicketType;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.UserLite;
import com.pding.paymentservice.repository.ExposureSlotHistoryRepository;
import com.pding.paymentservice.repository.ExposureSlotRepository;
import com.pding.paymentservice.repository.ExposureTicketPurchaseRepository;
import com.pding.paymentservice.repository.ExposureTicketRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.TokenSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    private final ExposureSlotHistoryRepository exposureSlotHistoryRepository;
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
        purchase.setPurchasedDate(Instant.now());
        purchase.setStatus(ExposureTicketStatus.UNUSED);
        purchase = exposureTicketPurchaseRepository.save(purchase);

        ledgerService.saveToLedger(purchase.getId(), ticketPrice, new BigDecimal(0), TransactionType.BUY_EXPOSURE_TICKET, userId);
        return purchase;
    }

    public Page<ExposureTicketPurchase> getPurchasedTicketOfUser(Pageable pageable) {
        String userId = authHelper.getUserId();
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "purchasedDate"));
        return exposureTicketPurchaseRepository.findByUserId(userId, pageRequest);
    }

    @Transactional
    public ExposureTicketPurchase useTicket(String ticketId) {
        String userId = authHelper.getUserId();
        ExposureTicketPurchase purchaseTicket = exposureTicketPurchaseRepository.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Invalid ticket id"));
        if (!userId.equalsIgnoreCase(purchaseTicket.getUserId())) {
            throw new IllegalArgumentException("Ticket does not belong to user");
        }
        if (purchaseTicket.getStatus() == ExposureTicketStatus.USED) {
            throw new IllegalArgumentException("Ticket already used");
        }

        ExposureTicketType type = purchaseTicket.getType();
        //TODO: validate type & time

        //assign top slot
        List<MExposureSlot> allSlots = exposureSlotRepository.findAll();
        boolean hasEmptySlot = allSlots.size() < 3;
        if (hasEmptySlot) {
            MExposureSlot slot = allSlots.stream().filter(s -> s.getUserId().equals(userId)).findFirst().orElse(null);
            if(slot != null) {
                throw new IllegalArgumentException("You already has exposure slot");
            }

            //assign slot
            List<ExposureSlotNumber> selectedSlotNumber = allSlots.stream().map(MExposureSlot::getSlotNumber).collect(Collectors.toList());
            ExposureSlotNumber slotNumber = Arrays.stream(ExposureSlotNumber.values()).filter(s -> !selectedSlotNumber.contains(s)).findFirst().orElse(null);
            if(slotNumber == null) {
                throw new IllegalArgumentException("Failed to assign slot, please try again");
            }

            slot = new MExposureSlot();
            slot.setUserId(userId);
            slot.setSlotNumber(slotNumber);
            slot = exposureSlotRepository.save(slot);

            //save History
            MExposureSlotHistory history = new MExposureSlotHistory();
            history.setId(slot.getId());
            history.setUserId(userId);
            history.setSlotNumber(slotNumber.toString());
            history.setStartTime(Instant.now());
            history.setEndTime(Instant.now().plusSeconds(3600));
            exposureSlotHistoryRepository.save(history);

            if(!sendNotificationSqsMessage.sendAutoExpireTopExposureSlot(userId)) {
                throw new IllegalArgumentException("Failed to use ticket, please try again");
            }
        }

        purchaseTicket.setStatus(ExposureTicketStatus.USED);
        purchaseTicket.setUsedDate(Instant.now());
        return exposureTicketPurchaseRepository.save(purchaseTicket);
    }

    public List<UserLite> getTopExposurePds() throws Exception {
        List<MExposureSlot> exposureSlots = exposureSlotRepository.findAll();
        Set<String> userIds = exposureSlots.stream().map(MExposureSlot::getUserId).collect(Collectors.toSet());

        List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(userIds).blockFirst();
        return usersFlux.stream().map(user -> UserLite.fromPublicUserNet(user, tokenSigner)).collect(Collectors.toList());
    }

    public void forceReleaseTicket(String userId) {
        exposureSlotRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User does not have exposure slot"));
        exposureSlotRepository.deleteById(userId);
        sendNotificationSqsMessage.sendForceReleaseTopExposureNotification(userId);
    }

    public void handleAutoExpireSlot(String userId) {
        MExposureSlot slot = exposureSlotRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User does not have exposure slot"));
        exposureSlotRepository.delete(slot);
        sendNotificationSqsMessage.sendForceReleaseTopExposureNotification(userId);
    }
}

package com.pding.paymentservice.service;

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
import com.pding.paymentservice.payload.dto.UserTicketCountDTO;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.CountUserTicketByType;
import com.pding.paymentservice.payload.response.SlotOverviewResponse;
import com.pding.paymentservice.payload.response.UserLite;
import com.pding.paymentservice.repository.*;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.TokenSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
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
    private final OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    private static final Map<String, ZoneId> ZONEID_MAP = Map.of(
        "kr", ZoneId.of("Asia/Seoul"),
        "ja", ZoneId.of("Asia/Tokyo"),
        "th", ZoneId.of("Asia/Bangkok"),
        "vi", ZoneId.of("Asia/Ho_Chi_Minh"),
        "zh_TW", ZoneId.of("Asia/Taipei"),
        "pl", ZoneId.of("Asia/Manila")
    );

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
        purchase.setIsGiveByAdmin(false);
        purchase = exposureTicketPurchaseRepository.save(purchase);

        ledgerService.saveToLedger(purchase.getId(), ticketPrice, new BigDecimal(0), TransactionType.BUY_EXPOSURE_TICKET, userId);
        return purchase;
    }

    public Page<ExposureTicketPurchase> getPurchasedTicketOfUser(Pageable pageable) {
        String userId = authHelper.getUserId();
        PageRequest pageRequest = PageRequest.of(0,  Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "purchasedDate"));
        return exposureTicketPurchaseRepository.findByUserId(userId, pageRequest);
    }

    public Page<UserTicketCountDTO> getPurchasedTicketOfUser(String userId, String ticketType, Pageable pageable) {
        return exposureTicketPurchaseRepository.countUserTicketByDate(userId, ticketType, pageable);
    }

    @Transactional
    public ExposureTicketPurchase useTicket(ExposureTicketType type) throws Exception {
        String userId = authHelper.getUserId();
        List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(Set.of(userId)).blockFirst();
        if (!usersFlux.get(0).getIsCreator()) {
            throw new IllegalArgumentException("Only creator can use exposure ticket");
        }

        String language = usersFlux.get(0).getLanguage();

//        if(language == null) {
//            throw new IllegalArgumentException("Cannot use the exposure ticket. Please contact support for assistance");
//        }

        ZoneId zoneId = language != null ? ZONEID_MAP.getOrDefault(language, ZoneId.of("UTC")) : ZoneId.of("UTC");
        //validate time zone
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        if(type.equals(ExposureTicketType.MORNING_AFTERNOON) && !(now.getHour() >= 6 && now.getHour() < 18)) {
            throw new IllegalArgumentException("Morning-Afternoon Ticket can only be used from 6AM to 6PM");
        }

        if(type.equals(ExposureTicketType.EVENING_NIGHT) && (now.getHour() >= 6 && now.getHour() < 18)) {
            throw new IllegalArgumentException("Evening-Night Ticket can only be used from 6PM to 6AM next day");
        }

        ExposureTicketPurchase purchaseTicket = exposureTicketPurchaseRepository.findFirstByTypeAndStatusAndUserId(type, ExposureTicketStatus.UNUSED, userId).orElseThrow(() -> new IllegalArgumentException("No ticket found"));

        //TODO: validate type & time

        //assign top slot
        List<MExposureSlot> allSlots = exposureSlotRepository.findAll();
        boolean hasEmptySlot = allSlots.size() < 3;
        if (hasEmptySlot) {
            MExposureSlot slot = allSlots.stream().filter(s -> s.getUserId().equals(userId)).findFirst().orElse(null);
            if (slot != null) {
                throw new IllegalArgumentException("You already has exposure slot");
            }

            //assign slot
            List<ExposureSlotNumber> selectedSlotNumber = allSlots.stream().map(MExposureSlot::getSlotNumber).collect(Collectors.toList());
            ExposureSlotNumber slotNumber = Arrays.stream(ExposureSlotNumber.values()).filter(s -> !selectedSlotNumber.contains(s)).findFirst().orElse(null);
            if (slotNumber == null) {
                throw new IllegalArgumentException("Failed to assign slot, please try again");
            }

            Instant startTime = Instant.now();
            Instant endTime = Instant.now().plusSeconds(3600);
            slot = new MExposureSlot();
            slot.setUserId(userId);
            slot.setSlotNumber(slotNumber);
            slot.setStartTime(startTime);
            slot.setEndTime(endTime);
            slot.setTicketType(type);
            slot = exposureSlotRepository.save(slot);

            //save History
            MExposureSlotHistory history = new MExposureSlotHistory();
            history.setId(slot.getId());
            history.setUserId(userId);
            history.setSlotNumber(slotNumber.toString());
            history.setStartTime(startTime);
            history.setEndTime(endTime);
            history.setTicketType(type.toString());
            exposureSlotHistoryRepository.save(history);
            purchaseTicket.setStatus(ExposureTicketStatus.USED);
            purchaseTicket.setUsedDate(Instant.now());
            return exposureTicketPurchaseRepository.save(purchaseTicket);

//            if(!sendNotificationSqsMessage.sendAutoExpireTopExposureSlot(userId)) {
//                throw new IllegalArgumentException("Failed to use ticket, please try again");
//            }
        } else {
            throw new IllegalArgumentException("No slot available");
        }
    }

    public List<UserLite> getTopExposurePds() throws Exception {

        String userId = authHelper.getUserId();

        List<MExposureSlot> exposureSlots = exposureSlotRepository.findAll();
        Instant now = Instant.now();

        if (otherServicesTablesNativeQueryRepository.isFollowingExists(userId) != 1 && !exposureSlots.contains(userId)) {
            return new ArrayList<>();
        }

        Set<String> userIds = exposureSlots.stream().filter(s -> s.getEndTime().isAfter(now)).map(MExposureSlot::getUserId).collect(Collectors.toSet());
        if(userIds.isEmpty()) {
            return List.of();
        }

        List<String> alreadyFollowOrUnFollow = otherServicesTablesNativeQueryRepository.findFollowingByListPd(userId, userIds);
        alreadyFollowOrUnFollow.remove(userId);
        Set<String> finalUserIds = userIds.stream().filter(uId -> !alreadyFollowOrUnFollow.contains(uId)).collect(Collectors.toSet());

        if(finalUserIds.isEmpty()) {
            return List.of();
        }
        List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(finalUserIds).blockFirst();
        return usersFlux.stream().map(user -> UserLite.fromPublicUserNet(user, tokenSigner)).collect(Collectors.toList());
    }

    @Transactional
    public void forceReleaseTicket(String userId) throws Exception {
        if (!userServiceNetworkManager.isUserAdmin(userId).blockLast()) {
            throw new IllegalArgumentException("Only admin can force release ticket");
        }
        Instant now = Instant.now();
        MExposureSlot slot = exposureSlotRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User does not have exposure slot"));
        MExposureSlotHistory history = exposureSlotHistoryRepository.findById(slot.getId())
            .orElse(new MExposureSlotHistory(slot.getId(), slot.getUserId(), slot.getStartTime(), slot.getEndTime(), slot.getSlotNumber().toString(), now, true, slot.getTicketType().toString()));
        exposureSlotHistoryRepository.save(history);

        exposureSlotRepository.delete(slot);
        sendNotificationSqsMessage.sendForceReleaseTopExposureNotification(userId);
    }

//    public void handleAutoExpireSlot(String userId) {
//        MExposureSlot slot = exposureSlotRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User does not have exposure slot"));
//        exposureSlotRepository.delete(slot);
//        sendNotificationSqsMessage.sendForceReleaseTopExposureNotification(userId);
//    }

    public List<CountUserTicketByType> countUserTicketByType() {
        String userId = authHelper.getUserId();
        return countUserTicketByType(userId);
    }

    public List<CountUserTicketByType> countUserTicketByType(String pdId) {
        List<CountUserTicketByType> result = Arrays.stream(ExposureTicketType.values()).map(type -> {
            Long count = exposureTicketPurchaseRepository.countByTypeAndUserIdAndStatus(type, pdId, ExposureTicketStatus.UNUSED);
            return new CountUserTicketByType(type, count);
        }).collect(Collectors.toList());
        return result;
    }

    public List<SlotOverviewResponse> getSlotOverview() throws Exception {
        Instant now = Instant.now();
        List<MExposureSlot> exposureSlots = exposureSlotRepository.findAll().stream().filter(s -> s.getEndTime().isAfter(now)).collect(Collectors.toList());;
        Set<String> userIds = exposureSlots.stream().map(MExposureSlot::getUserId).collect(Collectors.toSet());
        List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(userIds).blockFirst();

        return exposureSlots.stream().map(slot -> {
            PublicUserNet user = usersFlux.stream().filter(u -> u.getId().equals(slot.getUserId())).findFirst().orElse(null);
            if (user == null) {
                return null;
            }

            return new SlotOverviewResponse(
                slot.getUserId(), slot.getId(), user.getNickname(), slot.getTicketType().toString(), slot.getStartTime(), slot.getEndTime()
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());

    }

    @Transactional
    public void refundTicket(String transactionId) throws Exception {
        String userId = authHelper.getUserId();

        if (!userServiceNetworkManager.isUserAdmin(userId).blockLast()) {
            throw new IllegalArgumentException("Only admin can refund transaction");
        }

        ExposureTicketPurchase purchase = exposureTicketPurchaseRepository.findById(transactionId).orElseThrow(() -> new IllegalArgumentException("Invalid transaction id"));
        if (purchase.getStatus() == ExposureTicketStatus.USED) {
            throw new IllegalArgumentException("Cannot refund used ticket");
        }
        if(purchase.getStatus() == ExposureTicketStatus.REFUNDED) {
            throw new IllegalArgumentException("Ticket already refunded");
        }
        purchase.setStatus(ExposureTicketStatus.REFUNDED);
        exposureTicketPurchaseRepository.save(purchase);

        walletService.addToWallet(purchase.getUserId(), purchase.getTreesConsumed(), BigDecimal.ZERO, LocalDateTime.now());
        ledgerService.saveToLedger(purchase.getId(), purchase.getTreesConsumed(), new BigDecimal(0), TransactionType.REFUND_EXPOSURE_TICKET, purchase.getUserId());
    }

    public List<ExposureTicketPurchase> giveTicket(String userId, ExposureTicketType type, Integer numberOfTicket) throws Exception {
        String loginUser = authHelper.getUserId();

        if (!userServiceNetworkManager.isUserAdmin(loginUser).blockLast()) {
            throw new IllegalArgumentException("Only admin can issue ticket");
        }

//        MExposureTicket ticket = exposureTicketRepository.findById(type).orElseThrow(() -> new IllegalArgumentException("Invalid ticket type"));
        BigDecimal ticketPrice = BigDecimal.ZERO;

        List<ExposureTicketPurchase> purchases = new LinkedList<>();
        for(int i = 0; i < numberOfTicket; i++) {
            ExposureTicketPurchase purchase = new ExposureTicketPurchase();
            purchase.setId(UUID.randomUUID().toString());
            purchase.setUserId(userId);
            purchase.setType(type);
            purchase.setTreesConsumed(ticketPrice);
            purchase.setPurchasedDate(Instant.now());
            purchase.setStatus(ExposureTicketStatus.UNUSED);
            purchase.setIsGiveByAdmin(true);
            purchases.add(purchase);

        }
        return exposureTicketPurchaseRepository.saveAll(purchases);


    }
}

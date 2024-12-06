package com.pding.paymentservice.service;

import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.Earning;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.payload.dto.GrossRevenueByDateGraph;
import com.pding.paymentservice.payload.dto.GrossRevenueByDateRangeGraph;
import com.pding.paymentservice.payload.dto.LeafEarningInCallingHistoryDTO;
import com.pding.paymentservice.payload.dto.LeafGiftHistoryDTO;
import com.pding.paymentservice.payload.dto.PdSummaryDTO;
import com.pding.paymentservice.payload.dto.PurchasedLeafHistoryDTO;
import com.pding.paymentservice.payload.dto.PurchasedLeafHistorySummaryDTO;
import com.pding.paymentservice.repository.DonationRepository;
import com.pding.paymentservice.repository.EarningRepository;
import com.pding.paymentservice.repository.PaymentStatisticRepository;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.repository.WalletHourlyCaptureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentStatisticService {
    private final PaymentStatisticRepository paymentStatisticRepository;

    private final EarningRepository earningRepository;
    private final VideoPurchaseRepository videoPurchaseRepository;
    private final DonationRepository donationRepository;
    private final WalletHourlyCaptureRepository walletHourlyCaptureRepository;

    public Page<LeafEarningInCallingHistoryDTO> leafsEarningHistory(String pdId, String startDate, String endDate, Pageable pageable) {
        return paymentStatisticRepository.getLeafEarningInCallingHistory(pdId, startDate, endDate, pageable);
    }

    public Page<LeafGiftHistoryDTO> leafsEarningFromGiftHistory(String pdId, Pageable pageable) {
        return paymentStatisticRepository.getLeafEarningFromGiftHistory(pdId, pageable);
    }

    public Mono<PdSummaryDTO> pdSummary(String pdId) {
        Mono<Map<String, BigDecimal>> callTypeDurationMono = Mono.fromCallable(() -> paymentStatisticRepository.getCallTypeDuration(pdId))
            .subscribeOn(Schedulers.boundedElastic());

        Mono<Long> totalTextMessagesMono = Mono.fromCallable(() -> paymentStatisticRepository.getTotalTextMessage(pdId))
            .subscribeOn(Schedulers.boundedElastic());

        Mono<Long> totalGiftsMono = Mono.zip(
            Mono.fromCallable(() -> paymentStatisticRepository.getTotalGiftsInCall(pdId)),
            Mono.fromCallable(() -> paymentStatisticRepository.getTotalGiftsInChat(pdId)),
            Long::sum
        ).subscribeOn(Schedulers.boundedElastic());

        Mono<Optional<Earning>> earningMono = Mono.fromCallable(() -> earningRepository.findByUserId(pdId))
            .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(callTypeDurationMono, totalTextMessagesMono, totalGiftsMono, earningMono)
            .map(tuple -> {
                Map<String, BigDecimal> summary = tuple.getT1();
                Long totalTextMessages = tuple.getT2();
                Long totalGifts = tuple.getT3();
                Optional<Earning> earning = tuple.getT4();

                PdSummaryDTO pdSummaryDTO = new PdSummaryDTO();
                pdSummaryDTO.setVideoCallTotalDurationInSecond(summary.get("video"));
                pdSummaryDTO.setVoiceCallTotalDurationInSecond(summary.get("audio"));
                pdSummaryDTO.setTotalTextMessages(totalTextMessages);
                pdSummaryDTO.setTotalGifts(totalGifts);

                earning.ifPresent(e -> {
                    pdSummaryDTO.setTotalHoldingLeafs(e.getLeafsEarned());
                    pdSummaryDTO.setTotalHoldingTrees(e.getTreesEarned());
                });

                pdSummaryDTO.setGetStreamUserId(pdId);
                return pdSummaryDTO;
            });
    }

    public Page<PurchasedLeafHistoryDTO> leafPaymentHistory(String searchString, String startDate, String endDate, Pageable pageable) {
        return paymentStatisticRepository.getPurchasedLeafWalletHistory(searchString, startDate, endDate, pageable);
    }

    public PurchasedLeafHistorySummaryDTO leafPaymentHistorySummary() {

        Mono<BigDecimal> callTypeDurationMono = Mono.fromCallable(() -> paymentStatisticRepository.getTotalPurchasedLeafs())
            .subscribeOn(Schedulers.boundedElastic());

        Mono<BigDecimal> totalTextMessagesMono = Mono.fromCallable(() -> paymentStatisticRepository.getTotalLeafsRemainingInWallet())
            .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(callTypeDurationMono, totalTextMessagesMono)
            .map(tuple -> {
                BigDecimal totalPurchasedLeafs = tuple.getT1();
                BigDecimal totalLeafsRemainingInWallet = tuple.getT2();

                PurchasedLeafHistorySummaryDTO purchasedLeafHistorySummaryDTO = new PurchasedLeafHistorySummaryDTO();
                purchasedLeafHistorySummaryDTO.setTotalLeafsPurchased(totalPurchasedLeafs);
                purchasedLeafHistorySummaryDTO.setTotalLeafsRemaining(totalLeafsRemainingInWallet);

                return purchasedLeafHistorySummaryDTO;
            }).block();
    }

    public GrossRevenueByDateGraph getGrossRevenueGraph(String pdId, LocalDate selectedDate) {
        LocalDateTime startOfSelectedDate = selectedDate.atStartOfDay();
        LocalDateTime endOfSelectedDate = selectedDate.atTime(23, 59, 59);
        List<VideoPurchase> videoPurchases = videoPurchaseRepository.getVideoPurchasesByVideoOwnerUserIdAndDates(pdId, startOfSelectedDate, endOfSelectedDate);
        List<Donation> donations = donationRepository.findDonationsByPdIdAndDateRange(pdId, startOfSelectedDate, endOfSelectedDate);

        //get gross revenue of pd
        BigDecimal grossRevenue = videoPurchases.stream()
            .map(VideoPurchase::getTreesConsumed)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(donations.stream()
                .map(Donation::getDonatedTrees)
                .reduce(BigDecimal.ZERO, BigDecimal::add));


        //get map hour - revenue map of video purchases
        Map<Integer, BigDecimal> revenuePerHour = videoPurchases.stream()
            .collect(Collectors.groupingBy(vp -> vp.getLastUpdateDate().getHour(), Collectors.reducing(BigDecimal.ZERO, VideoPurchase::getTreesConsumed, BigDecimal::add)));

        //get map hour - revenue map of donations
        Map<Integer, BigDecimal> donationRevenuePerHour = donations.stream()
            .collect(Collectors.groupingBy(d -> d.getLastUpdateDate().getHour(), Collectors.reducing(BigDecimal.ZERO, Donation::getDonatedTrees, BigDecimal::add)));


        Map<Integer, BigDecimal> mergedMap = new HashMap<>();
        revenuePerHour.forEach((k, v) -> mergedMap.merge(k, v, BigDecimal::add));
        donationRevenuePerHour.forEach((k, v) -> mergedMap.merge(k, v, BigDecimal::add));

        //field hour not present in mergedMap, add it with value 0
        for (int i = 0; i < 24; i++) {
            mergedMap.putIfAbsent(i, BigDecimal.ZERO);
        }

        return new GrossRevenueByDateGraph(
            selectedDate,
            grossRevenue,  new TreeMap<>(mergedMap));
    }


    public Object getGrossRevenueGraph(String pdId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime startOfSelectedDate = fromDate.atStartOfDay();
        LocalDateTime endOfSelectedDate = toDate.atTime(23, 59, 59);

        List<VideoPurchase> videoPurchases = videoPurchaseRepository.getVideoPurchasesByVideoOwnerUserIdAndDates(pdId, startOfSelectedDate, endOfSelectedDate);
        List<Donation> donations = donationRepository.findDonationsByPdIdAndDateRange(pdId, startOfSelectedDate, endOfSelectedDate);

        //get gross revenue of pd
        BigDecimal grossRevenue = videoPurchases.stream()
            .map(VideoPurchase::getTreesConsumed)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(donations.stream()
                .map(Donation::getDonatedTrees)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        //get map date - revenue map of video purchases
        Map<LocalDate, BigDecimal> revenuePerDate = videoPurchases.stream()
            .collect(Collectors.groupingBy(vp -> vp.getLastUpdateDate().toLocalDate(), Collectors.reducing(BigDecimal.ZERO, VideoPurchase::getTreesConsumed, BigDecimal::add)));

        //get map date - revenue map of donations
        Map<LocalDate, BigDecimal> donationRevenuePerDate = donations.stream()
            .collect(Collectors.groupingBy(d -> d.getLastUpdateDate().toLocalDate(), Collectors.reducing(BigDecimal.ZERO, Donation::getDonatedTrees, BigDecimal::add)));

        Map<LocalDate, BigDecimal> mergedMap = new HashMap<>();
        revenuePerDate.forEach((k, v) -> mergedMap.merge(k, v, BigDecimal::add));
        donationRevenuePerDate.forEach((k, v) -> mergedMap.merge(k, v, BigDecimal::add));

        //field date not present in mergedMap, add it with value 0
        for (LocalDate date = fromDate; date.isBefore(toDate); date = date.plusDays(1)) {
            mergedMap.putIfAbsent(date, BigDecimal.ZERO);
        }

        Map<LocalDate, BigDecimal> sortedMap = new TreeMap<>(mergedMap);

        return new GrossRevenueByDateRangeGraph(fromDate, toDate, grossRevenue, sortedMap);
    }

    public Object getDailyTotalTreeGraph(LocalDate fromDate, LocalDate toDate, String unit) {
        return null;
    }

    public Object getHourlyTotalTreeGraph(LocalDate date) {
        return null;
    }
}

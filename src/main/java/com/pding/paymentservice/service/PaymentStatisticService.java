package com.pding.paymentservice.service;

import com.pding.paymentservice.models.Earning;
import com.pding.paymentservice.payload.dto.LeafEarningInCallingHistoryDTO;
import com.pding.paymentservice.payload.dto.LeafGiftHistoryDTO;
import com.pding.paymentservice.payload.dto.PdSummaryDTO;
import com.pding.paymentservice.payload.dto.PurchasedLeafHistoryDTO;
import com.pding.paymentservice.payload.dto.PurchasedLeafHistorySummaryDTO;
import com.pding.paymentservice.repository.EarningRepository;
import com.pding.paymentservice.repository.PaymentStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentStatisticService {
    private final PaymentStatisticRepository paymentStatisticRepository;

    private final EarningRepository earningRepository;

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
}

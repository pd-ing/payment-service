package com.pding.paymentservice.service.job;

import com.pding.paymentservice.models.WalletHourlyCapture;
import com.pding.paymentservice.repository.WalletHourlyCaptureRepository;
import com.pding.paymentservice.service.EarningService;
import com.pding.paymentservice.service.WalletService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class WalletCaptureJob {

    @Autowired
    WalletService walletService;

    @Autowired
    EarningService earningService;

    @Autowired
    WalletHourlyCaptureRepository walletHourlyCaptureRepository;

    @Scheduled(cron = "59 59 * ? * *", zone = "Asia/Seoul")
    //for test
//    @Scheduled(cron = "0 * * ? * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "WalletCaptureJob_captureWallet",
            lockAtLeastFor = "PT30S", lockAtMostFor = "PT1M")
    public void captureWallet() {
        //get zoned date time of zone Asia/Seoul
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        //check if is the last day of the month
        Boolean isEndOfDay = seoulDateTime.getHour() == 23;
        Boolean isEndOfWeek = seoulDateTime.getDayOfWeek().getValue() == 7;
        Boolean isEndOfMonth = seoulDateTime.getDayOfMonth() == seoulDateTime.getMonth().length(seoulDateTime.toLocalDate().isLeapYear());
//        Boolean isEndOfYear = seoulDateTime.getDayOfYear() == seoulDateTime.toLocalDate().lengthOfYear();

        BigDecimal sumOfAllTree = walletService.getTotalTrees();
        BigDecimal sumOfAllTreesEarned = earningService.sumOfAllTreesEarned();

//        String date = seoulDateTime.toLocalDate().toString();
//        String time = seoulDateTime.toLocalTime().toString();

        WalletHourlyCapture walletHourlyCapture = WalletHourlyCapture.builder()
                .captureTime(seoulDateTime)
                .totalTreeLeftInWallet(sumOfAllTree)
                .totalTreeLeftInEarning(sumOfAllTreesEarned)
                .isEndOfDay(isEndOfDay)
                .isEndOfWeek(isEndOfWeek)
                .isEndOfMonth(isEndOfMonth).build();

        walletHourlyCaptureRepository.save(walletHourlyCapture);
    }
}

package com.pding.paymentservice.job;

import com.pding.paymentservice.repository.VideoPurchaseRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CollectUserNotPurchaseVideo {

    @Autowired
    VideoPurchaseRepository videoPurchaseRepository;

    @Scheduled(cron = "0 0 0 ? * MON")
    @SchedulerLock(name = "CollectUserNotPurchaseVideo_execute",
        lockAtLeastFor = "PT3M", lockAtMostFor = "PT5M")
    public void execute() {
        videoPurchaseRepository.saveEmailToSendCRMEmail();
    }
}

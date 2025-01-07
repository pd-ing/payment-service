package com.pding.paymentservice.job;

import com.pding.paymentservice.models.MExposureSlot;
import com.pding.paymentservice.models.MExposureSlotHistory;
import com.pding.paymentservice.repository.ExposureSlotHistoryRepository;
import com.pding.paymentservice.repository.ExposureSlotRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ReleaseExpiredExposureSlotJob {
    @Autowired
    ExposureSlotRepository exposureSlotRepository;

    @Autowired
    ExposureSlotHistoryRepository exposureSlotHistoryRepository;

    @Scheduled(cron = "0 * * ? * *")
    @Transactional
    @SchedulerLock(name = "ReleaseExpiredExposureSlotJob_releaseExpiredExposureSlot",
        lockAtLeastFor = "PT30S", lockAtMostFor = "PT1M")
    public void releaseExpiredExposureSlot() {
        Instant now = Instant.now();
        List<MExposureSlot> slots = exposureSlotRepository.findAll(); // acceptable for now, because the number of slots is only 3
        for (MExposureSlot slot : slots) {
            if(slot.getEndTime().isBefore(now)) {
                MExposureSlotHistory history = exposureSlotHistoryRepository.findById(slot.getId())
                    .orElse(new MExposureSlotHistory(slot.getId(), slot.getUserId(), slot.getStartTime(), slot.getEndTime(), slot.getSlotNumber().toString(), now, false, slot.getTicketType().toString()));
                history.setReleasedTime(now);
                history.setIsForcedRelease(false);
                exposureSlotHistoryRepository.save(history);

                //release the slot
                exposureSlotRepository.delete(slot);
            }
        }
    }
}

package com.pding.paymentservice.service.cache;

import com.pding.paymentservice.constant.AppConstant;
import com.pding.paymentservice.models.tables.inner.VideoEarningsAndSales;
import com.pding.paymentservice.payload.dto.VideoSalesAndPurchaseNetCache;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.util.MapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VideoSalesAndPurchaseNetCacheService {
    private final Long CACHE_EXPIRATION = 2 * 60 * 60L;         //  2 hours

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private VideoPurchaseRepository videoPurchaseRepo;

    public Map<String, VideoSalesAndPurchaseNetCache> getVideoAndSaleByVideoIds(List<String> videoIds) {
        if (CollectionUtils.isEmpty(videoIds)) {
            return Collections.emptyMap();
        }

        Map<String, VideoSalesAndPurchaseNetCache> mapVideoSaleAndPurchaseByID = new HashMap<>(videoIds.size());
        for (String videoId : videoIds) {
            mapVideoSaleAndPurchaseByID.put(videoId, this.getVideoSaleAndPurchaseByVideoId(videoId));
        }
        return mapVideoSaleAndPurchaseByID;
    }

    public VideoSalesAndPurchaseNetCache getVideoSaleAndPurchaseByVideoId(String videoId) {
        String key = AppConstant.PREFIX_KEY_CACHE_VIDEO_SALE_AND_PURCHASE + videoId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            Map<String, VideoEarningsAndSales> totalTreesEarnedAndSalesCountMapForVideoIds
                    = videoPurchaseRepo.getTotalTreesEarnedAndSalesCountMapForVideoIds(List.of(videoId));
            if (totalTreesEarnedAndSalesCountMapForVideoIds.isEmpty()) {
                VideoSalesAndPurchaseNetCache videoSalesAndPurchaseNetCache = new VideoSalesAndPurchaseNetCache(videoId, 0d, 0L);
                this.setCacheVideoSaleAndPurchase(videoId, videoSalesAndPurchaseNetCache, CACHE_EXPIRATION);
                return videoSalesAndPurchaseNetCache;
            }

            VideoSalesAndPurchaseNetCache videoSalesAndPurchaseNetCache = VideoSalesAndPurchaseNetCache.builder()
                    .videoId(videoId)
                    .treesEarned(totalTreesEarnedAndSalesCountMapForVideoIds.get(videoId).getTreesEarned().doubleValue())
                    .totalSales(totalTreesEarnedAndSalesCountMapForVideoIds.get(videoId).getTotalSales())
                    .build();
            this.setCacheVideoSaleAndPurchase(videoId, videoSalesAndPurchaseNetCache, CACHE_EXPIRATION);
            return videoSalesAndPurchaseNetCache;
        }

        return VideoSalesAndPurchaseNetCache.from(value);
    }

    public void setCacheVideoSaleAndPurchase(String videoId, VideoSalesAndPurchaseNetCache videoSalesAndPurchaseNetCache, Long expirationMinutes) {
        String key = AppConstant.PREFIX_KEY_CACHE_VIDEO_SALE_AND_PURCHASE + videoId;
        String value = MapperUtils.toString(videoSalesAndPurchaseNetCache);
        if (value == null) {
            log.error("Failed to convert VideoSalesAndPurchaseNetCache to string");
            return;
        }

        redisTemplate.opsForValue().set(key, value, expirationMinutes, TimeUnit.MINUTES);
    }

    public void deleteCache(String videoId) {
        String key = AppConstant.PREFIX_KEY_CACHE_VIDEO_SALE_AND_PURCHASE + videoId;
        this.redisTemplate.delete(key);
    }
}

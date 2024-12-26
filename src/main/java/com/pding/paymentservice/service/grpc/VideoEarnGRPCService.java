package com.pding.paymentservice.service.grpc;

import com.pding.grpc.user_management.GetVideoIdsRequest;
import com.pding.grpc.user_management.PaymentServiceGrpc;
import com.pding.grpc.user_management.VideoSalesAndPurchaseNet;
import com.pding.grpc.user_management.VideoSalesAndPurchaseResponseNetResponse;
import com.pding.paymentservice.models.tables.inner.VideoEarningsAndSales;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@GrpcService
public class VideoEarnGRPCService extends PaymentServiceGrpc.PaymentServiceImplBase{

    private final Logger log = LoggerFactory.getLogger(VideoEarnGRPCService.class);

    @Autowired
    private VideoPurchaseRepository videoPurchaseRepo;

    @Override
    public void getSalesAndPurchaseDataOfVideos(GetVideoIdsRequest request, StreamObserver<VideoSalesAndPurchaseResponseNetResponse> responseObserver) {
        log.info("Received GRPC request to get video earns for video id: " + request.getIdsList());
        long s = System.currentTimeMillis();
        Map<String, VideoEarningsAndSales> totalTreesEarnedAndSalesCountMapForVideoIds = videoPurchaseRepo.getTotalTreesEarnedAndSalesCountMapForVideoIds(request.getIdsList());

        Map<String, VideoSalesAndPurchaseNet> mapByVideoId = totalTreesEarnedAndSalesCountMapForVideoIds.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> VideoSalesAndPurchaseNet.newBuilder()
                        .setTreesEarned(ObjectUtils.defaultIfNull(entry.getValue().getTreesEarned(), BigDecimal.ZERO).doubleValue())
                        .setTotalSales(entry.getValue().getTotalSales())
                        .build()
                )
        );

        VideoSalesAndPurchaseResponseNetResponse response = VideoSalesAndPurchaseResponseNetResponse.newBuilder()
                .putAllVideoEarningsAndSales(mapByVideoId)
                .build();

        log.info("Received GRPC response for video earns: {} ms", System.currentTimeMillis() - s);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

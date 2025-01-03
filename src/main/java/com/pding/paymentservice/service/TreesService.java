package com.pding.paymentservice.service;

import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.request.StatisticTopSellPDRequest;
import com.pding.paymentservice.payload.response.StatisticTopSellPDResponse;
import com.pding.paymentservice.payload.response.TreeSpentHistory.TreeSpentHistoryRecord;
import com.pding.paymentservice.repository.DonationRepository;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.repository.TreesRepository;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.CommonMethods;
import com.pding.paymentservice.util.TokenSigner;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TreesService {


    @Autowired
    DonationRepository donationRepository;

    @Autowired
    VideoPurchaseRepository videoPurchaseRepository;

    @Autowired
    TreesRepository treesRepository;

    @Autowired
    AuthHelper authHelper;

    @Autowired
    CommonMethods commonMethods;

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    public List<PublicUserNet> getTopFans(Long limit) throws Exception {
        String userId = authHelper.getUserId();
        List<String> lstBlockedUsers = otherServicesTablesNativeQueryRepository.findBlockedUsersByUserId(userId);
        if(lstBlockedUsers.isEmpty()) {
            lstBlockedUsers.add("_");
        }
        List<Object[]> topFans = treesRepository.getUserTotalTreesSpentWithLimit(limit, lstBlockedUsers);
        return commonMethods.getPublicUserInfo(topFans);
    }

    public Page<TreeSpentHistoryRecord> getTreeSpentHistory(int page, int size) {
        String userId = authHelper.getUserId();
        List<TreeSpentHistoryRecord> treeSpentHistoryList = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> tshPage = treesRepository.getTreesSpentHistory(userId, pageable);
        for (Object innerObject : tshPage) {
            Object[] tsh = (Object[]) innerObject;
            TreeSpentHistoryRecord tshObj = new TreeSpentHistoryRecord();
            tshObj.setLastUpdateDate(tsh[0].toString());
            tshObj.setType(tsh[1].toString());
            tshObj.setPdProfileId(tsh[2].toString());
            tshObj.setAmount(tsh[3].toString());

            treeSpentHistoryList.add(tshObj);
        }

        return new PageImpl<>(treeSpentHistoryList, pageable, tshPage.getTotalElements());
    }

    public BigDecimal totalTreesSpentByUserOnVideo(String userId) {
        return donationRepository.getTotalDonatedTreesByDonorUserId(userId);
    }

    public BigDecimal totalTreesSpentByUserOnDonation(String userId) {
        return videoPurchaseRepository.getTotalTreesConsumedByUserId(userId);
    }

    public List<StatisticTopSellPDResponse> statisticTopTreeByPDIds(StatisticTopSellPDRequest statisticTopSellPDRequest) {
        if (CollectionUtils.isEmpty(statisticTopSellPDRequest.getPdIds())) {
            return Collections.emptyList();
        }

        Map<String, StatisticTopSellPDResponse> mapByPdId = videoPurchaseRepository.statisticTopSellPDs(statisticTopSellPDRequest.getPdIds(), statisticTopSellPDRequest.getFrom())
                .stream()
                .collect(Collectors.toMap(StatisticTopSellPDResponse::getPdId, s -> s));

        List<StatisticTopSellPDResponse> response = new ArrayList<>();
        for (String pdId : statisticTopSellPDRequest.getPdIds()) {
            StatisticTopSellPDResponse pdResponse = mapByPdId.get(pdId);
            if (pdResponse == null) {
                pdResponse = StatisticTopSellPDResponse.builder().pdId(pdId).totalTrees(BigDecimal.ZERO).build();
            }
            response.add(pdResponse);
        }
        return response;
    }
}

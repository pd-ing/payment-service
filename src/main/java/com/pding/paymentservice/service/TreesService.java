package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.TreeSpentHistory.TreeSpentHistoryRecord;
import com.pding.paymentservice.repository.DonationRepository;
import com.pding.paymentservice.repository.TreesRepository;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.CommonMethods;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public List<PublicUserNet> getTopFans(Long limit) throws Exception {
        List<Object[]> topFans = treesRepository.getUserTotalTreesSpentWithLimit(limit);
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
}

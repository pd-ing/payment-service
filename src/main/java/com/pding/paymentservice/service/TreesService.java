package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.generic.TreeSpentHistory;
import com.pding.paymentservice.repository.TreesRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.util.CommonMethods;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TreesService {

    @Autowired
    UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    TreesRepository treesRepository;

    @Autowired
    TokenSigner tokenSigner;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    AuthHelper authHelper;

    @Autowired
    CommonMethods commonMethods;

    public List<PublicUserNet> getTopFans(Long limit) throws Exception {
        List<Object[]> topFans = treesRepository.getUserTotalTreesSpentWithLimit(limit);
        return commonMethods.getPublicUserInfo(topFans);
    }

    public Page<TreeSpentHistory> getTreeSpentHistory(int page, int size) {
        String userId = authHelper.getUserId();
        List<TreeSpentHistory> treeSpentHistoryList = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> tshPage = treesRepository.getTreesSpentHistory(userId, pageable);
        for (Object innerObject : tshPage) {
            Object[] tsh = (Object[]) innerObject;
            TreeSpentHistory tshObj = new TreeSpentHistory();
            tshObj.setLastUpdateDate(tsh[0].toString());
            tshObj.setType(tsh[1].toString());
            tshObj.setPdProfileId(tsh[2].toString());
            tshObj.setAmount(tsh[3].toString());

            treeSpentHistoryList.add(tshObj);
        }

        return new PageImpl<>(treeSpentHistoryList, pageable, tshPage.getTotalElements());
    }
}

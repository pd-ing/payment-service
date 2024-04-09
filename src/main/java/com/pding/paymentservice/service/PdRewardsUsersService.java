package com.pding.paymentservice.service;

import com.pding.paymentservice.models.PdRewardsUsers;
import com.pding.paymentservice.payload.request.PdRewardUsersRequest;
import com.pding.paymentservice.repository.PdRewardsUsersRepository;
import com.pding.paymentservice.security.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PdRewardsUsersService {

    @Autowired
    AuthHelper authHelper;

    @Autowired
    PdRewardsUsersRepository pdRewardsUsersRepository;

    public String updateRewardSettings(String description, String rewardForTopUsers) {
        String pdUserId = authHelper.getUserId();

        Optional<PdRewardsUsers> pdRewardsUsersOptional = pdRewardsUsersRepository.findByPdUserId(pdUserId);

        PdRewardsUsers pdRewardsUsers = null;
        if (pdRewardsUsersOptional.isEmpty()) {
            pdRewardsUsers = new PdRewardsUsers();
            pdRewardsUsers.setDescription(description);
            pdRewardsUsers.setRewardForTopUsers(rewardForTopUsers);
            pdRewardsUsers.setPdUserId(pdUserId);
            pdRewardsUsers.setLastUpdateDate(LocalDateTime.now());

        } else {
            pdRewardsUsers = pdRewardsUsersOptional.get();
            pdRewardsUsers.setDescription(description);
            pdRewardsUsers.setRewardForTopUsers(rewardForTopUsers);
            pdRewardsUsers.setLastUpdateDate(LocalDateTime.now());
        }
        pdRewardsUsersRepository.save(pdRewardsUsers);

        return "Reward settings updated successfully";
    }

    public PdRewardUsersRequest getRewardSettings() throws Exception {
        String pdUserId = authHelper.getUserId();
        Optional<PdRewardsUsers> pdRewardsUsersOptional = pdRewardsUsersRepository.findByPdUserId(pdUserId);
        if (!pdRewardsUsersOptional.isEmpty()) {
            PdRewardsUsers pdRewardsUsers = pdRewardsUsersOptional.get();

            PdRewardUsersRequest pdRewardUsersRequest = PdRewardUsersRequest.fromJsonString(pdRewardsUsers.getRewardForTopUsers());
            pdRewardUsersRequest.setDescription(pdRewardsUsers.getDescription());

            return pdRewardUsersRequest;
        }
        
        return null;
    }
}

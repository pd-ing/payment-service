package com.pding.paymentservice.service;

import com.pding.paymentservice.models.PdRewardsUsers;
import com.pding.paymentservice.payload.request.PdRewardUsersRequest;
import com.pding.paymentservice.repository.PdRewardsUsersRepository;
import com.pding.paymentservice.security.AuthHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class PdRewardsUsersService {

    @Autowired
    AuthHelper authHelper;

    @Autowired
    PdRewardsUsersRepository pdRewardsUsersRepository;

    public String updateRewardSettings(PdRewardUsersRequest pdRewardUsersRequest) throws Exception {
        String pdUserId = authHelper.getUserId();


        Optional<PdRewardsUsers> pdRewardsUsersOptional = pdRewardsUsersRepository.findByPdUserId(pdUserId);

        PdRewardsUsers pdRewardsUsers = null;
        if (pdRewardsUsersOptional.isEmpty()) {

            pdRewardsUsers = new PdRewardsUsers();

            pdRewardsUsers.setDescription(pdRewardUsersRequest.getDescription());
            pdRewardsUsers.setPdUserId(pdUserId);
            pdRewardsUsers.setLastUpdateDate(LocalDateTime.now());

            PdRewardUsersRequest.PdRewardUsersJsonMapper pdRewardUsersJsonMapper = new PdRewardUsersRequest.PdRewardUsersJsonMapper();
            pdRewardUsersJsonMapper = pdRewardUsersRequest.updatePdRewardsUsers(pdRewardUsersRequest, pdRewardUsersJsonMapper);
            String rewardForTopUsers = pdRewardUsersRequest.toJsonString(pdRewardUsersJsonMapper);
            pdRewardsUsers.setRewardForTopUsers(rewardForTopUsers);


        } else {
            pdRewardsUsers = pdRewardsUsersOptional.get();
            if (pdRewardUsersRequest.getDescription() != null) {
                pdRewardsUsers.setDescription(pdRewardUsersRequest.getDescription());
            }
            pdRewardsUsers.setLastUpdateDate(LocalDateTime.now());

            PdRewardUsersRequest.PdRewardUsersJsonMapper pdRewardUsersJsonMapper = PdRewardUsersRequest.getPdRewardUsersObjectFromJson(pdRewardsUsers.getRewardForTopUsers());
            pdRewardUsersJsonMapper = pdRewardUsersRequest.updatePdRewardsUsers(pdRewardUsersRequest, pdRewardUsersJsonMapper);
            String rewardForTopUsers = pdRewardUsersRequest.toJsonString(pdRewardUsersJsonMapper);
            pdRewardsUsers.setRewardForTopUsers(rewardForTopUsers);

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

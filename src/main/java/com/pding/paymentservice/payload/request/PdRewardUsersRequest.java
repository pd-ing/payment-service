package com.pding.paymentservice.payload.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PdRewardUsersRequest {
    String description;

    String rewardTextForNumberOneFan;

    String rewardTextForNumberTwoFan;

    String rewardTextForNumberThreeFan;

    String rewardTextForNumberFourToTenFans;

    String rewardTextForNumberElevenToTwentyFans;


    // Method to convert object to JSON string
    public String toJsonString(PdRewardUsersJsonMapper pdRewardUsersJsonMapper) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(pdRewardUsersJsonMapper);
    }

    public static PdRewardUsersRequest fromJsonString(String jsonString) throws JsonProcessingException {
        // Convert JSON string to object
        PdRewardUsersJsonMapper pdRewardUsersJsonMapper = getPdRewardUsersObjectFromJson(jsonString);

        // Create PdRewardUsersRequest object with the retrieved data
        return PdRewardUsersRequest.builder()
                .rewardTextForNumberOneFan(pdRewardUsersJsonMapper.getRewardTextForNumberOneFan())
                .rewardTextForNumberTwoFan(pdRewardUsersJsonMapper.getRewardTextForNumberTwoFan())
                .rewardTextForNumberThreeFan(pdRewardUsersJsonMapper.getRewardTextForNumberThreeFan())
                .rewardTextForNumberFourToTenFans(pdRewardUsersJsonMapper.getRewardTextForNumberFourToTenFans())
                .rewardTextForNumberElevenToTwentyFans(pdRewardUsersJsonMapper.getRewardTextForNumberElevenToTwentyFans())
                .build();
    }

    public PdRewardUsersJsonMapper updatePdRewardsUsers(PdRewardUsersRequest pdRewardUsersRequest, PdRewardUsersJsonMapper pdRewardUsersJsonMapper) {
        //Set text for #1 fan
        if (pdRewardUsersRequest.getRewardTextForNumberOneFan() != null) {
            pdRewardUsersJsonMapper.setRewardTextForNumberOneFan(pdRewardUsersRequest.getRewardTextForNumberOneFan());
        }

        //Set text for #2 fan
        if (pdRewardUsersRequest.getRewardTextForNumberTwoFan() != null) {
            pdRewardUsersJsonMapper.setRewardTextForNumberTwoFan(pdRewardUsersRequest.getRewardTextForNumberTwoFan());
        }

        //Set text for #3 fan
        if (pdRewardUsersRequest.getRewardTextForNumberThreeFan() != null) {
            pdRewardUsersJsonMapper.setRewardTextForNumberThreeFan(pdRewardUsersRequest.getRewardTextForNumberThreeFan());
        }

        //Set text for #4-10 fan
        if (pdRewardUsersRequest.getRewardTextForNumberFourToTenFans() != null) {
            pdRewardUsersJsonMapper.setRewardTextForNumberFourToTenFans(pdRewardUsersRequest.getRewardTextForNumberFourToTenFans());
        }

        //Set text for #11-20 fan
        if (pdRewardUsersRequest.getRewardTextForNumberElevenToTwentyFans() != null) {
            pdRewardUsersJsonMapper.setRewardTextForNumberElevenToTwentyFans(pdRewardUsersRequest.getRewardTextForNumberElevenToTwentyFans());
        }

        return pdRewardUsersJsonMapper;
    }

    public static PdRewardUsersJsonMapper getPdRewardUsersObjectFromJson(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, PdRewardUsersJsonMapper.class);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PdRewardUsersJsonMapper {
        String rewardTextForNumberOneFan;
        String rewardTextForNumberTwoFan;
        String rewardTextForNumberThreeFan;
        String rewardTextForNumberFourToTenFans;
        String rewardTextForNumberElevenToTwentyFans;
    }
}

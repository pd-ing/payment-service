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
    @NotBlank(message = "Description cannot be null or empty")
    String description;

    @NotBlank(message = "Reward text for number one fan cannot be null or empty")
    String rewardTextForNumberOneFan;

    @NotBlank(message = "Reward text for number two fan cannot be null or empty")
    String rewardTextForNumberTwoFan;

    @NotBlank(message = "Reward text for number three fan cannot be null or empty")
    String rewardTextForNumberThreeFan;

    @NotBlank(message = "Reward text for number four to ten fans cannot be null or empty")
    String rewardTextForNumberFourToTenFans;

    @NotBlank(message = "Reward text for number eleven to twenty fans cannot be null or empty")
    String rewardTextForNumberElevenToTwentyFans;


    // Method to convert object to JSON string
    public String toJsonString() throws JsonProcessingException {
        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a new object excluding the description field
        PdRewardUsersRewards pdRewardUsersRewards = PdRewardUsersRewards.builder()
                .rewardTextForNumberOneFan(this.rewardTextForNumberOneFan)
                .rewardTextForNumberTwoFan(this.rewardTextForNumberTwoFan)
                .rewardTextForNumberThreeFan(this.rewardTextForNumberThreeFan)
                .rewardTextForNumberFourToTenFans(this.rewardTextForNumberFourToTenFans)
                .rewardTextForNumberElevenToTwentyFans(this.rewardTextForNumberElevenToTwentyFans)
                .build();

        // Convert object to JSON string and return
        return objectMapper.writeValueAsString(pdRewardUsersRewards);
    }

    public static PdRewardUsersRequest fromJsonString(String jsonString) throws JsonProcessingException {
        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        // Convert JSON string to object
        PdRewardUsersRewards pdRewardUsersRewards = objectMapper.readValue(jsonString, PdRewardUsersRewards.class);

        // Create PdRewardUsersRequest object with the retrieved data
        return PdRewardUsersRequest.builder()
                .rewardTextForNumberOneFan(pdRewardUsersRewards.getRewardTextForNumberOneFan())
                .rewardTextForNumberTwoFan(pdRewardUsersRewards.getRewardTextForNumberTwoFan())
                .rewardTextForNumberThreeFan(pdRewardUsersRewards.getRewardTextForNumberThreeFan())
                .rewardTextForNumberFourToTenFans(pdRewardUsersRewards.getRewardTextForNumberFourToTenFans())
                .rewardTextForNumberElevenToTwentyFans(pdRewardUsersRewards.getRewardTextForNumberElevenToTwentyFans())
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PdRewardUsersRewards {
        String rewardTextForNumberOneFan;
        String rewardTextForNumberTwoFan;
        String rewardTextForNumberThreeFan;
        String rewardTextForNumberFourToTenFans;
        String rewardTextForNumberElevenToTwentyFans;
    }
}

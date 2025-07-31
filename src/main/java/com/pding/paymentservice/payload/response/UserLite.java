package com.pding.paymentservice.payload.response;

import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.util.TokenSigner;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class UserLite {

    String id;

    String displayName;

    String description;

    String profilePicture;

    String profileId;

    String pdCategory;

    String language;

    Boolean isRecommendedPd;

    Long follower;

    public UserLite(String id, String displayName, String description, String profilePicture, String profileId, String pdCategory, Long follower) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.profilePicture = profilePicture;
        this.profileId = profileId;
        this.pdCategory = pdCategory;
        this.follower = follower;
    }

    public static UserLite fromPublicUserNet(PublicUserNet src, TokenSigner tokenSigner) {
        String displayName = src.getId();
         if (src.getNickname() != null && !src.getNickname().isEmpty()) {
            displayName = src.getNickname();
        } else if (src.getProfileId() != null && !src.getProfileId().isEmpty()) {
            displayName = src.getProfileId();
        }

        String profilePicture = null;

        try {
            if (src.getProfilePicture() != null) {
                profilePicture = tokenSigner.generateUnsignedImageUrl(tokenSigner.composeImagesPath(src.getProfilePicture()));
            }
        } catch (Exception e) {}

        return new UserLite(
            src.getId(), displayName, src.getDescription(), profilePicture, src.getProfileId(), src.getPdCategory(), src.getFollower()
        );
    }

}

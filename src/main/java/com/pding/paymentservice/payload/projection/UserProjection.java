package com.pding.paymentservice.payload.projection;

public interface UserProjection {
    String getId();

    String getEmail();

    Boolean getIsCreator();

    String getProfilePicture();

    Boolean getIsEnabled();

    String getNickname();

    String getDescription();

    String getCoverImage();

    String getProfileId();

    String getPdCategory();

    String getLanguage();

    String getDisplayName();
}

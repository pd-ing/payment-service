package com.pding.paymentservice.payload.projection;

public interface VideoProjection {
    String getVideoId();
    String getUserId();
    Boolean getDrmEnable();
    Boolean getIs4kEnable();
    Long getDuration();
    String getVideoLibraryId();
    Boolean getAdvancedEncodingEnable();

}

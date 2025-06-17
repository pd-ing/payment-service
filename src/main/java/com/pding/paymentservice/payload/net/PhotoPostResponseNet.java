package com.pding.paymentservice.payload.net;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response from content service for photo post details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoPostResponseNet {
    private String id;
    private String userId;
    private String title;
    private String description;
    private Long uploadedTimeStamp;
    private Long updatedTimeStamp;
    private BigDecimal trees;
    private Boolean isPaid;
    private Boolean isAdult;
    private Boolean isVisible;
    private String status;
    private Double ratingScore;
    private Boolean isDeleted;
    private Boolean isPinned;
    private LocalDateTime pinnedAt;
    private Boolean ratingVisible;
    private PublicUserNet user;
    private List<String> photoUrls = new ArrayList<>();
    private Integer totalPhotos;
    private Integer publicPhotos;

    // Price information
    public static class PhotoDurationPriceDTO {
        private String id;
        private String duration;
        private BigDecimal trees;
        private Boolean enabled;

        public PhotoDurationPriceDTO() {}

        public PhotoDurationPriceDTO(String id, String duration, BigDecimal trees, Boolean enabled) {
            this.id = id;
            this.duration = duration;
            this.trees = trees;
            this.enabled = enabled;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public BigDecimal getTrees() {
            return trees;
        }

        public void setTrees(BigDecimal trees) {
            this.trees = trees;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }

    private List<PhotoDurationPriceDTO> prices = new ArrayList<>();
}

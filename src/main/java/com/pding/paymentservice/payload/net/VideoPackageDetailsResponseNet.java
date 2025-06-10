package com.pding.paymentservice.payload.net;

import com.pding.paymentservice.models.enums.PackageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Response from content service for video package details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoPackageDetailsResponseNet {
    private String id; // Package ID
    private String sellerId; // Seller ID
    private String title; // Package title
    private String description; // Package description
    private Integer discountPercentage; // Discount percentage
    private LocalDateTime startDate; // Sale start date
    private LocalDateTime endDate; // Sale end date
    private Boolean isActive; // Whether the package is active
    private BigDecimal basePrice; // Original price before discount
    private BigDecimal discountedPrice; // Price after discount
    private BigDecimal personalizedPrice; // Personalized price based on user's owned videos
    private Set<String> videoIds; // IDs of videos in the package
    private List<String> ownedVideoIds; // IDs of videos the user already owns
    private List<VideoPackageItemDTONet> items = new ArrayList<>(); // Details of videos in the package
    private PackageType packageType = PackageType.THEME_PACKAGE; // Type of package (FREE_CHOICE_PACKAGE or THEME_PACKGE)
    private Integer numberOfVideos;

    public List<VideoPackageItemDTONet> getItems() {
        if(this.items == null) return new ArrayList<>();
        return items;
    }
}

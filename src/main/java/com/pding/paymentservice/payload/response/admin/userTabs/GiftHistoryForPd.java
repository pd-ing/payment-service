package com.pding.paymentservice.payload.response.admin.userTabs;

import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.DonationHistoryForAdminDashboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftHistoryForPd {
    BigDecimal totalTreesGifted;

    Page<DonationHistoryForAdminDashboard> donationHistoryForAdminDashboardList;
}

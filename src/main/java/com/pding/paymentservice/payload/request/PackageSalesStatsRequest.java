package com.pding.paymentservice.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request for getting sales statistics for a list of packages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageSalesStatsRequest {
    private List<String> packageIds;
}

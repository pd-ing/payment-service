package com.pding.paymentservice.app.config.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PdSegments {
    ENGAGEMENT_HIGH,
    ENGAGEMENT_LOW,
    ENGAGEMENT_ALL,
    REVENUE_HIGH,
    REVENUE_LOW,
    REVENUE_ALL,
    FREQUENCY_CONSISTENT,
    FREQUENCY_OCCASIONAL,
    FREQUENCY_ALL;


    public static PdSegments fromString(String pdType) {
        if (pdType == null) {
            throw new IllegalArgumentException("pdType cannot be null");
        }
        try {
            return PdSegments.valueOf(pdType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid pdType: " + pdType);
        }
    }

    public static List<String> convertToSegmentList(PdSegments pdSegment) {
        List<String> segments = new ArrayList<>();

        switch (pdSegment) {
            case ENGAGEMENT_HIGH:
                segments.add("ENGAGEMENT_HIGH");
                break;
            case ENGAGEMENT_LOW:
                segments.add("ENGAGEMENT_LOW");
                break;
            case ENGAGEMENT_ALL:
                segments.addAll(Arrays.asList("ENGAGEMENT_HIGH", "ENGAGEMENT_LOW"));
                break;
            case REVENUE_HIGH:
                segments.add("REVENUE_HIGH");
                break;
            case REVENUE_LOW:
                segments.add("REVENUE_LOW");
                break;
            case REVENUE_ALL:
                segments.addAll(Arrays.asList("REVENUE_HIGH", "REVENUE_LOW"));
                break;
            case FREQUENCY_CONSISTENT:
                segments.add("FREQUENCY_CONSISTENT");
                break;
            case FREQUENCY_OCCASIONAL:
                segments.add("FREQUENCY_OCCASIONAL");
                break;
            case FREQUENCY_ALL:
                segments.addAll(Arrays.asList("FREQUENCY_CONSISTENT", "FREQUENCY_OCCASIONAL"));
                break;
            default:
                throw new IllegalArgumentException("Unknown segment: " + pdSegment);
        }

        return segments;
    }
}

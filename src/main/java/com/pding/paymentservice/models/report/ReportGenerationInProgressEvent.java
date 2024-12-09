package com.pding.paymentservice.models.report;

import com.pding.paymentservice.repository.GenerateReportEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
public class ReportGenerationInProgressEvent implements GenerateReportEvent {
    private String reportId;
    private long timestamp;
    private Map<String, Object> metadata;
    @Override
    public String getEventType() {
        return "GENERATION_IN_PROGRESS";
    }
}

package com.pding.paymentservice.models.report;

import com.pding.paymentservice.repository.GenerateReportEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationStartedEvent implements GenerateReportEvent {
    private String reportId;
    private long timestamp;
    private String reportType;
    private Map<String, Object> parameters;

    @Override
    public String getEventType() {
        return "GENERATION_STARTED";
    }
}

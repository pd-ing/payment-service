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
public class ReportGenerationCompletedEvent implements GenerateReportEvent {
    private String reportId;
    private long timestamp;
    private String reportTitle;
    private String message;
    private long fileSize;
    private Map<String, Object> metadata;
    @Override
    public String getEventType() {
        return "GENERATION_COMPLETED";
    }
}

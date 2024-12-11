package com.pding.paymentservice.models.report;

import com.pding.paymentservice.repository.GenerateReportEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
// Event when report generation fails
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationFailedEvent implements GenerateReportEvent {
    private String reportId;
    private long timestamp;
    private String errorCode;
    private String errorMessage;
    private String failureStep;
    private Map<String, Object> errorDetails;
    @Override
    public String getEventType() {
        return "GENERATION_FAILED";
    }
}

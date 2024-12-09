package com.pding.paymentservice.repository;


public interface GenerateReportEvent {
    String getEventType();
    String getReportId();
    long getTimestamp();
}
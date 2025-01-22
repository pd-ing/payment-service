package com.pding.paymentservice.payload.request;

import com.pding.paymentservice.models.enums.ExposureTicketType;
import lombok.Data;

@Data
public class GiveExposureTicketByAdminRequest {
    private ExposureTicketType type;
    private String userId;
    private Integer numberOfTicket;
}

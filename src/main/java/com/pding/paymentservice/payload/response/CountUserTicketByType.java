package com.pding.paymentservice.payload.response;

import com.pding.paymentservice.models.enums.ExposureTicketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountUserTicketByType {
    ExposureTicketType type;
    Long count;
}

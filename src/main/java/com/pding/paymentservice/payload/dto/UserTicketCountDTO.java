package com.pding.paymentservice.payload.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserTicketCountDTO {
    private Integer count;
    private LocalDate date;
}

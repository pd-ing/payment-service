package com.pding.paymentservice.payload.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticTopSellPDRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull
    private List<String> pdIds;

    @NotNull
    private LocalDateTime from;
}

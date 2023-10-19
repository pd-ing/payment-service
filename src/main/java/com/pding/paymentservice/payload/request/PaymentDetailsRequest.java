package com.pding.paymentservice.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsRequest {
    @NotNull(message = "User ID cannot be null.")
    private Long userid;

    @NotNull(message = "Trees cannot be null.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Trees should be greater than 0.")
    private BigDecimal trees;

    @NotNull(message = "Purchase Date cannot be null.")
    private LocalDateTime purchasedDate;

    @NotNull(message = "Amount cannot be null.")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount should be at least 0.01.")
    private BigDecimal amount;

    @NotBlank(message = "Payment Method cannot be blank.")
    private String paymentMethod;

    @NotBlank(message = "Currency cannot be blank.")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency should be a 3-letter ISO code.")
    private String currency;

    @Size(max = 500, message = "Description should not exceed 500 characters.")
    private String description;

    // You can expand on this with more specific regex to validate IP addresses.
    @NotBlank(message = "IP Address cannot be blank.")
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "Invalid IP Address format.")
    private String ipAddress;

    @NotBlank(message = "Transaction id cannot be blank")
    private String transactionID;

    @NotBlank(message = "Transaction status cannot be blank")
    private String transactionStatus;
}

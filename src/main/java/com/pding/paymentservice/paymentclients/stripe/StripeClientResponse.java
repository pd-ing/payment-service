package com.pding.paymentservice.paymentclients.stripe;

import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StripeClientResponse {
    Product product;
    Session session;
}

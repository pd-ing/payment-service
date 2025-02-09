package com.pding.paymentservice.payload.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IsUserAdminResponseNet {

    @JsonAlias({"isAdmin", "admin"})
    Boolean isAdmin;

}

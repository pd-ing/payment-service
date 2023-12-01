package com.pding.paymentservice.payload.net;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetUsersResponseNet {

    ErrorResponse error;
    
    List<PublicUserNet> result;

}

package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.app.config.enums.PdSegments;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.PageUserLiteResponse;
import com.pding.paymentservice.payload.response.UserLite;
import com.pding.paymentservice.payload.response.UserObject;
import com.pding.paymentservice.repository.PdClassificationRepository;
import com.pding.paymentservice.util.CommonMethods;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PDClassificationService {

    @Autowired
    PdClassificationRepository pdClassificationRepository;

    @Autowired
    TokenSigner tokenSigner;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    CommonMethods commonMethods;


    public List<PublicUserNet> getPdUserListFromSegment(PdSegments pdSegment, int page, int size ) throws Exception {
        if (page < 0 || size < 0) {
            throw new IllegalArgumentException("Page or size parameter should not be negative");
        }
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Object[]> topActivePds = pdClassificationRepository.findTopActiveUsers(pageable);
            return commonMethods.getPublicUserInfo((List<Object[]>) topActivePds);
           // Page<UserLite> userLite = userPage.map(user -> UserLite.fromUser(user, tokenSigner, pdLogger));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.EVENTS, e);
            throw new RuntimeException("An error occurred while retrieving users", e);
        }
    }

}

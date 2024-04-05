package com.pding.paymentservice.util;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserNet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommonMethods {

    @Autowired
    TokenSigner tokenSigner;

    @Autowired
    UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    PdLogger pdLogger;

    public List<PublicUserNet> getPublicUserInfo(List<Object[]> userObjects) throws Exception {
        if (userObjects.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> callerUserIds = userObjects.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());


        Map<String, BigDecimal> topFansMap = userObjects.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],          // calledUserId
                        row -> (BigDecimal) row[1]       // totalLeafsTransacted
                ));

        List<PublicUserNet> publicUsers = userServiceNetworkManager
                .getUsersListFlux(callerUserIds)
                .collect(Collectors.toList())
                .block();

        for (PublicUserNet user : publicUsers) {
            String profilePicture = null;
            try {
                if (user.getProfilePicture() != null) {
                    profilePicture = tokenSigner.signImageUrl(tokenSigner.composeImagesPath(user.getProfilePicture()), 8);
                }
            } catch (Exception e) {
                pdLogger.logException(PdLogger.EVENT.IMAGE_CDN_LINK, e);
                e.printStackTrace();
            }

            String coverImage = null;
            try {
                if (user.getCoverImage() != null) {
                    coverImage = tokenSigner.signImageUrl(tokenSigner.composeImagesPath(user.getCoverImage()), 8);
                }
            } catch (Exception e) {
                pdLogger.logException(PdLogger.EVENT.IMAGE_CDN_LINK, e);
                e.printStackTrace();

            }

            BigDecimal totalTreesSpent = topFansMap.get(user.getId());

            user.setTotalTreesSpent(totalTreesSpent);
            user.setProfilePicture(profilePicture);
            user.setCoverImage(coverImage);
        }

        return publicUsers;
    }

    public static String calculateFeeAndTax(String amountPaid) {
        try {
            BigDecimal totalAmount = new BigDecimal(amountPaid).divide(new BigDecimal("100"));
            BigDecimal taxAmount = totalAmount.divide(new BigDecimal("11"), 0, BigDecimal.ROUND_DOWN);
            BigDecimal baseAmount = totalAmount.subtract(taxAmount);
            String result = baseAmount.setScale(0) + "$ + VAT " + taxAmount + "$";

            return result;
        } catch (Exception e) {
            return amountPaid + " in cents/dollars";
        }

    }

}

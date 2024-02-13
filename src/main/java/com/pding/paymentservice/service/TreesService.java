package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GenericListDataResponse;
import com.pding.paymentservice.repository.TreesRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TreesService {

    @Autowired
    UserServiceNetworkManager userServiceNetworkManager;

    @Autowired
    TreesRepository treesRepository;

    @Autowired
    TokenSigner tokenSigner;

    @Autowired
    PdLogger pdLogger;

    List<PublicUserNet> getTopFans(Long limit) throws Exception {
        List<Object[]> topFans = treesRepository.getUserTotalTreesSpentWithLimit(limit);
        return getPublicUserInfo(topFans);
    }

    List<PublicUserNet> getPublicUserInfo(List<Object[]> userObjects) throws Exception {
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


    public ResponseEntity<?> topFans(Long limit) {
        if (limit == null || limit <= 0 || limit > 30) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "limit parameter is invalid or not passed. Please pass limit between 1-30"));
        }
        try {
            List<PublicUserNet> publicUserNetList = getTopFans(limit);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, publicUserNetList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.TOP_FAN_LIST, e);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(errorResponse, null));
        }
    }
}

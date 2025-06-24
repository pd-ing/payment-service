package com.pding.paymentservice.controllers;

import com.pding.paymentservice.models.PhotoPurchase;
import com.pding.paymentservice.payload.request.CheckImagePurchasedRequest;
import com.pding.paymentservice.payload.response.UserLite;
import com.pding.paymentservice.payload.response.custompagination.PaginationInfoWithGenericList;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.payload.response.generic.GenericSliceResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.PhotoPurchaseService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling photo purchase operations for web
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class PhotoPurchaseServiceController {

    @Autowired
    private AuthHelper authHelper;

    @Autowired
    private PhotoPurchaseService photoPurchaseService;

    /**
     * Get the purchaser list of a photo post
     *
     * @param photoId The ID of the photo post
     * @param page    The page number
     * @param size    The page size
     * @return A list of users who purchased the photo post
     */
    @GetMapping(value = "/getPurchaserOfPhoto")
    public ResponseEntity<?> purchaserListOfPhoto(
        @RequestParam String photoId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return photoPurchaseService.loadPurchaseListOfSellerResponse(photoId, page, size);
    }

    /**
     * Buy a photo post with a specific duration
     *
     * @param postId   The ID of the post to buy
     * @param duration The duration of the purchase
     * @return The purchase transaction
     */
    @PostMapping(value = "/buyPhotoPost")
    public ResponseEntity<?> buyPhotoPostWithDuration(
        @RequestParam(value = "postId") String postId,
        @RequestParam(value = "duration") String duration) {
        String userId = authHelper.getUserId();
        return ResponseEntity.ok(new GenericClassResponse<>(null, photoPurchaseService.buyPhotoPost(userId, postId, duration)));
    }

    /**
     * Check if a list of photo posts have been purchased by the current user
     *
     * @param request The request containing the list of post IDs to check
     * @return A map of post IDs to boolean values indicating if they have been purchased
     */
    @PostMapping(value = "/isPhotoPostPurchased")
    public ResponseEntity<?> isPhotoPostPurchased(
        @RequestBody CheckImagePurchasedRequest request) {
        String userId = authHelper.getUserId();
        return ResponseEntity.ok(new GenericClassResponse<>(null, photoPurchaseService.isPhotoPostPurchased(userId, request.getPostIds())));
    }

    /**
     * Get all photo posts purchased by the current user from a specific creator
     *
     * @param pdId The ID of the creator
     * @param page The page number
     * @param size The page size
     * @return A slice of photo purchases
     */
    @GetMapping(value = "/getPurchasedPhotoPosts")
    public ResponseEntity<?> getPurchasedPhotoPosts(
        @RequestParam(value = "pdId") String pdId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
        String userId = authHelper.getUserId();
        Slice<PhotoPurchase> photoPurchases =
            photoPurchaseService.getPurchasedPhotoPosts(userId, pdId, page, size);

        return ResponseEntity.ok(new GenericSliceResponse<>(null, photoPurchases.getContent(), photoPurchases.hasNext()));
    }

    /**
     * Get all creators whose photo posts have been purchased by the current user
     *
     * @param size The page size
     * @param page The page number
     * @return A page of user lite objects
     * @throws Exception If there is an error getting the user information
     */
    @GetMapping(value = "/allPdWhosePhotoPostsArePurchasedByUser")
    public ResponseEntity<?> allPdWhosePhotoPostsArePurchasedByUser(
        @RequestParam(value = "size", defaultValue = "20") int size,
        @RequestParam(value = "page", defaultValue = "0") int page
    ) throws Exception {
        String userId = authHelper.getUserId();
        Page<UserLite> userPage = photoPurchaseService.getAllPdUserIdWhosePhotosArePurchasedByUser(userId, size, page);

        return ResponseEntity.ok(new PaginationInfoWithGenericList(userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.getContent()));
    }
}

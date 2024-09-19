package com.pding.paymentservice.controllers;

import com.pding.paymentservice.app.config.enums.PdSegments;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.PageUserLiteResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.service.PDClassificationService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class PDClassificationController {

    @Autowired
    PDClassificationService pdClassificationService;

    @GetMapping("/pdClassification")
    ResponseEntity<?> getPdSegments(@RequestParam String pdType, @RequestParam(defaultValue = "0") @Min(0) int page,
                                    @RequestParam(defaultValue = "10") @Min(1) int size) {
        try {
            PdSegments segment;
            segment = PdSegments.fromString(pdType.toUpperCase());
            List<PublicUserNet> publicUserNetList = pdClassificationService.getPdUserListFromSegment(segment, page, size);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, publicUserNetList));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(errorResponse, null));
        }
    }

}

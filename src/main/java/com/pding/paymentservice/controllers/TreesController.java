package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.payload.response.generic.TreeSpentHistory;
import com.pding.paymentservice.service.TreesService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class TreesController {

    @Autowired
    TreesService treesService;

    @Autowired
    PdLogger pdLogger;

    @GetMapping(value = "/topFans")
    public ResponseEntity<?> getTopFans(@RequestParam(value = "limit") Long limit) {
        if (limit == null || limit <= 0 || limit > 30) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "limit parameter is invalid or not passed. Please pass limit between 1-30"));
        }
        try {
            List<PublicUserNet> publicUserNetList = treesService.getTopFans(limit);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, publicUserNetList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.TOP_FAN_LIST, e);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(errorResponse, null));
        }
    }

    @GetMapping(value = "/treeSpentHistory")
    public ResponseEntity<?> getTreeSpentHistory(@RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be greater than or equal to 0") int page,
                                                 @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than or equal to 1") int size) {
        try {
            Page<TreeSpentHistory> publicUserNetList = treesService.getTreeSpentHistory(page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, publicUserNetList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.TOP_FAN_LIST, e);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(errorResponse, null));
        }
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}

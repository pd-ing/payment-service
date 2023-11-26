package com.pding.paymentservice.controllers;

import com.pding.paymentservice.models.Earning;
import com.pding.paymentservice.service.DonationService;
import com.pding.paymentservice.service.EarningService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class DonationServiceController {

    @Autowired
    DonationService donationService;

    @PostMapping(value = "/donate")
    public ResponseEntity<?> buyVideo(@RequestParam(value = "donorUserId") String donorUserId, @RequestParam(value = "trees") BigDecimal trees, @RequestParam(value = "pdUserId") String pdUserId) {
        return donationService.donateToPd(donorUserId, trees, pdUserId);
    }

    @GetMapping(value = "/donationHistoryForUser")
    public ResponseEntity<?> getDonationHistoryForUser(@RequestParam(value = "donorUserId") String donorUserId) {
        return donationService.getDonationHistoryForUser(donorUserId);
    }

    @GetMapping(value = "/donationHistoryForPd")
    public ResponseEntity<?> getDonationHistoryForPd(@RequestParam(value = "pdUserId") String pdUserId) {
        return donationService.getDonationHistoryForPd(pdUserId);
    }
}

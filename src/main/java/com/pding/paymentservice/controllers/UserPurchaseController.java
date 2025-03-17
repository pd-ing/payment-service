package com.pding.paymentservice.controllers;

import com.pding.paymentservice.service.UserPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user-purchase/internal")
public class UserPurchaseController {

    @Autowired
    private UserPurchaseService userPurchaseService;

    @GetMapping(value = "/find-from-last-days")
    public ResponseEntity<List<String>> findUserPurchaseFromLastDays(@RequestParam(value = "days", defaultValue = "7") int days) {
        return ResponseEntity.ok(userPurchaseService.findUserPurchaseFromLastDays(days));
    }
}

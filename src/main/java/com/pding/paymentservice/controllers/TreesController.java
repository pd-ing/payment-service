package com.pding.paymentservice.controllers;

import com.pding.paymentservice.service.TreesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class TreesController {

    @Autowired
    TreesService treesService;

    @GetMapping(value = "/topFans")
    public ResponseEntity<?> getTopFans(@RequestParam(value = "limit") Long limit) {
        return treesService.topFans(limit);
    }
}

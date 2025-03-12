package com.sas.saveandsound.controller;

import com.sas.saveandsound.service.DataInitializationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataInitializationService dataInitializationService;

    public DataController(DataInitializationService dataInitializationService) {
        this.dataInitializationService = dataInitializationService;
    }

    @GetMapping("/initialize")
    public void initializeData() {
        dataInitializationService.initializeData();
    }
}


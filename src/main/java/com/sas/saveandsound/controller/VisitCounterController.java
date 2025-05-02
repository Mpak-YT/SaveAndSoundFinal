package com.sas.saveandsound.controller;

import com.sas.saveandsound.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class VisitCounterController {

    private final VisitCounterService visitCounterService;

    public VisitCounterController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Operation(summary = "Get visit count",
            description = "Retrieves the number of visits to the getAllUsers endpoint.")
    @GetMapping("count")
    public Integer getVisitCount() {
        return visitCounterService.getVisitCount();
    }

}

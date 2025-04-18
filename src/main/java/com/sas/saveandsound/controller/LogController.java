package com.sas.saveandsound.controller;

import com.sas.saveandsound.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log API", description = "API for fetching application logs.")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(
            summary = "Get the full application log file",
            description = "Fetches the complete application log file for download as a single file."
    )
    @GetMapping("/full")
    public ResponseEntity<FileSystemResource> getFullLogFile() {
        return logService.getFullLogFile();
    }

    @Operation(
            summary = "Get log file by date",
            description = "Fetches logs for a specific date. " +
                    "Logs are filtered based on the provided date (format: dd-MM-yyyy)."
    )
    @GetMapping("/download")
    public ResponseEntity<FileSystemResource> getLogFileByDate(
            @Parameter(description = "Date for which logs are requested, in the format dd-MM-yyyy.",
                    example = "03-04-2025")
            @RequestParam String date) {
        return logService.getLogFileByDate(date);
    }
}

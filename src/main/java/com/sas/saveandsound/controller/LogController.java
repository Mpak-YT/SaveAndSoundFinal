package com.sas.saveandsound.controller;

import com.sas.saveandsound.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

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
            summary = "Create log file by date",
            description = "Creates a log file for a specific date " +
                    "and returns the process ID (format: dd-MM-yyyy)."
    )
    @PostMapping("/create")
    public ResponseEntity<Integer> getLogFileByDate(
            @Parameter(description = "Date for which logs are requested, in the format dd-MM-yyyy.",
                    example = "03-04-2025")
            @RequestParam String date) {
        return logService.getLogFileByDate(date);
    }

    @Operation(summary = "Get log file by date",
            description = "Retrieves the content of the log file for a specific date.")
    @GetMapping("/file/{id}")
    public ResponseEntity<Object> getLogFileByDateContent(@PathVariable int id) {
        return logService.getLogFileByIdContent(id);
    }

    @Operation(summary = "Get log creation status",
            description = "Checks the status of the log creation process by ID.")
    @GetMapping("/status/{id}")
    public ResponseEntity<String> getLogCreationStatus(@PathVariable int id) {
        return ResponseEntity.ok(logService.getLogCreationStatus(id));
    }
}

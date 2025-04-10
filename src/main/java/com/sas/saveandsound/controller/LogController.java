package com.sas.saveandsound.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log API", description = "API for fetching application logs.")
public class LogController {

    @Value("${logs.directory.path}")
    private String logsDirectoryPath;

    @Operation(
            summary = "Get the full application log file",
            description = "Fetches the complete application log file for download as a single file."
    )
    @GetMapping("/full")
    public ResponseEntity<FileSystemResource> getFullLogFile() {
        File logFile = new File(logsDirectoryPath, "application.log");

        if (!logFile.exists() || !logFile.isFile()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(logFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + logFile.getName())
                .body(resource);
    }

    @Operation(
            summary = "Get log file by date",
            description = "Fetches logs for a specific date. " +
                    "Logs are filtered based on the provided date (format: dd-MM-yyyy)."
    )
    @GetMapping("/download")
    public ResponseEntity<FileSystemResource> getLogFileByDate(
            @Parameter(description = "Date for which logs are requested, in the format " +
                    "dd-MM-yyyy.", example = "03-04-2025")
            @RequestParam String date) {
        // Проверка формата даты
        if (date == null || !date.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return ResponseEntity.badRequest().body(null);
        }

        // Преобразование строки в дату
        String[] dateParts = date.split("-");
        String formattedDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

        LocalDate inputDate;
        try {
            inputDate = LocalDate.of(
                    Integer.parseInt(dateParts[2]),
                    Integer.parseInt(dateParts[1]),
                    Integer.parseInt(dateParts[0])
            );
        } catch (DateTimeException e) {
            return ResponseEntity.badRequest().build(); // Неправильная дата
        }

        // Проверка, что дата в прошлом
        if (!inputDate.isBefore(LocalDate.now()) && !inputDate.isEqual(LocalDate.now())) {
            return ResponseEntity.badRequest().body(null);
        }

        // Поиск и фильтрация логов
        File logFile = new File(logsDirectoryPath, "application.log");
        if (!logFile.exists() || !logFile.isFile()) {
            return ResponseEntity.notFound().build();
        }

        File filteredLogFile = new File(logsDirectoryPath,
                "application-" + formattedDate + ".log");

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(filteredLogFile))) {

            String line;
            boolean hasLogs = false;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(formattedDate)) {
                    writer.write(line);
                    writer.newLine();
                    hasLogs = true;
                }
            }

            if (!hasLogs) {
                return ResponseEntity.notFound().build();
            }

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (!filteredLogFile.exists() || filteredLogFile.length() == 0) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(filteredLogFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filteredLogFile.getName())
                .body(resource);
    }
}

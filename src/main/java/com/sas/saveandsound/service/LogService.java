package com.sas.saveandsound.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;

@Service
public class LogService {

    @Value("${logs.directory.path}")
    private String logsDirectoryPath;

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

    public ResponseEntity<FileSystemResource> getLogFileByDate(String date) {
        if (date == null || !date.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return ResponseEntity.badRequest().body(null);
        }

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
            return ResponseEntity.badRequest().build();
        }

        if (!inputDate.isBefore(LocalDate.now()) && !inputDate.isEqual(LocalDate.now())) {
            return ResponseEntity.badRequest().body(null);
        }

        File logFile = new File(logsDirectoryPath, "application.log");
        if (!logFile.exists() || !logFile.isFile()) {
            return ResponseEntity.notFound().build();
        }

        File filteredLogFile = new File(logsDirectoryPath, "application-" + formattedDate + ".log");

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

package com.sas.saveandsound.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    @Value("${logs.directory.path}")
    private String logsDirectoryPath;

    private final AtomicInteger logIdGenerator = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, String> logStatuses = new ConcurrentHashMap<>();

    private String getFormattedDate(String date) {
        if (date == null || !date.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return null;
        }

        String[] dateParts = date.split("-");
        return dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
    }

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

    public ResponseEntity<Integer> getLogFileByDate(String date) {
        String formattedDate = getFormattedDate(date);
        if (formattedDate == null) {
            return ResponseEntity.badRequest().body(null);
        }

        LocalDate inputDate;
        try {
            inputDate = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }

        if (!inputDate.isBefore(LocalDate.now()) && !inputDate.isEqual(LocalDate.now())) {
            return ResponseEntity.badRequest().body(null);
        }

        int logId = logIdGenerator.incrementAndGet();
        logStatuses.put(logId, "IN_PROGRESS");

        File logFile = new File(logsDirectoryPath, String.format("application-%s.log", formattedDate));
        File mainLogFile = new File(logsDirectoryPath, "application.log");

        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
                filterLogFile(mainLogFile, logFile, formattedDate, logId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Re-interrupt the thread
                logger.error("Thread was interrupted during delay", e);
                logStatuses.put(logId, "FAILED");
            }
        });

        return ResponseEntity.ok(logId);
    }

    /**
     * Reads the main log file and writes filtered content to a new log file.
     */
    private void filterLogFile(File mainLogFile, File logFile, String formattedDate, int logId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(mainLogFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(formattedDate)) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            logStatuses.put(logId, "COMPLETED");

        } catch (IOException e) {
            logger.error("Error filtering log file: {}", e.getMessage(), e);
            logStatuses.put(logId, "FAILED");
        }
    }


    public String getLogCreationStatus(int id) {
        return logStatuses.getOrDefault(id, "NOT_FOUND");
    }

    public synchronized ResponseEntity<InputStreamResource> getLogFileByDateContent(String date) {
        String formattedDate = getFormattedDate(date);
        if (formattedDate == null) {
            return ResponseEntity.badRequest().build();
        }

        File logFile = new File(logsDirectoryPath, "application-" + formattedDate + ".log");

        if (!logFile.exists() || !logFile.isFile()) {
            return ResponseEntity.notFound().build();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);

        } catch (IOException e) {
            logger.error("Error reading log file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}

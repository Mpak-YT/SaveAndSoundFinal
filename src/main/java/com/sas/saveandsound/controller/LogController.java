package com.sas.saveandsound.controller;

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

@RestController
@RequestMapping("/api/logs")
public class LogController {

    // Путь к логам из application.properties
    @Value("${logs.directory.path}")
    private String logsDirectoryPath;

    // Метод для возврата общего файла application.log
    @GetMapping("/full")
    public ResponseEntity<FileSystemResource> getFullLogFile() {
        File logFile = new File(logsDirectoryPath, "application.log");

        if (!logFile.exists() || !logFile.isFile()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(logFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + logFile.getName())
                .body(resource);
    }

    // Метод для фильтрации логов по дате
    @GetMapping("/download")
    public ResponseEntity<FileSystemResource> getLogFileByDate(@RequestParam String date) {
        // Проверяем формат даты (день-месяц-год)
        if (date == null || !date.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return ResponseEntity.badRequest().build();
        }

        // Преобразуем дату в формат yyyy-MM-dd
        String[] dateParts = date.split("-");
        String formattedDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

        // Файл с логами
        File logFile = new File(logsDirectoryPath, "application.log");
        if (!logFile.exists() || !logFile.isFile()) {
            return ResponseEntity.notFound().build();
        }

        // Создаём временный файл для логов выбранной даты
        File filteredLogFile = new File(logsDirectoryPath, "application-" + formattedDate + ".log");

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(filteredLogFile))) {

            String line;
            boolean hasLogs = false; // Флаг для проверки наличия строк за указанную дату

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(formattedDate)) { // Проверяем, начинается ли строка с указанной даты
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

        // Проверяем, существует ли файл и содержит ли он данные
        if (!filteredLogFile.exists() || filteredLogFile.length() == 0) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(filteredLogFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filteredLogFile.getName())
                .body(resource);
    }
}

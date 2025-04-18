package com.sas.saveandsound.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class LogServiceTest {

    private LogService logService;

    private final String testLogsDirectoryPath = "src/test/resources/logs";

    @BeforeEach
    void setUp() {
        logService = new LogService();

        // Устанавливаем значение приватного поля `logsDirectoryPath` через ReflectionTestUtils
        ReflectionTestUtils.setField(logService, "logsDirectoryPath", testLogsDirectoryPath);
    }

    // Тест для метода getFullLogFile
    @Test
    void testGetFullLogFile_FileExists() {
        // Arrange
        File logFile = new File(testLogsDirectoryPath, "application.log");
        boolean directoriesCreated = logFile.getParentFile().mkdirs();
        assertTrue(directoriesCreated || logFile.getParentFile().exists(), "Не удалось создать директорию для лог-файлов.");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write("Test log content.");
        } catch (IOException e) {
            fail("Не удалось создать тестовый лог-файл. Ошибка: " + e.getMessage());
        }

        // Act
        ResponseEntity<FileSystemResource> response = logService.getFullLogFile();

        // Assert
        assertNotNull(response, "Ответ должен быть не null.");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP статус должен быть OK.");
        assertTrue(response.getBody() != null && response.getBody().exists(), "Лог-файл должен существовать.");
        assertEquals("application.log", response.getBody().getFilename(), "Имя файла должно быть 'application.log'.");

        // Удаляем тестовый файл после теста
        boolean fileDeleted = logFile.delete();
        assertTrue(fileDeleted, "Не удалось удалить тестовый лог-файл.");
    }


    @Test
    void testGetFullLogFile_FileDoesNotExist() {

        // Act
        ResponseEntity<FileSystemResource> response = logService.getFullLogFile();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Тест для метода getLogFileByDate
    @Test
    void testGetLogFileByDate_Success() {
        // Arrange
        File logFile = new File(testLogsDirectoryPath, "application.log");
        File filteredLogFile = new File(testLogsDirectoryPath, "application-2023-04-01.log");

        // Создаём директорию и проверяем результат
        boolean directoriesCreated = logFile.getParentFile().mkdirs();
        assertTrue(directoriesCreated || logFile.getParentFile().exists(), "Не удалось создать директорию для лог-файлов.");

        // Создаём тестовый лог-файл
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write("2023-04-01 Log entry 1");
            writer.newLine();
            writer.write("2023-04-01 Log entry 2");
            writer.newLine();
            writer.write("2023-04-02 Log entry");
        } catch (IOException e) {
            fail("Не удалось создать тестовый лог-файл. Ошибка: " + e.getMessage());
        }

        // Act
        ResponseEntity<FileSystemResource> response = logService.getLogFileByDate("01-04-2023");

        // Assert
        assertNotNull(response, "Ответ должен быть не null.");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP статус должен быть OK.");
        assertNotNull(response.getBody(), "Тело ответа не должно быть null.");
        assertTrue(response.getBody().exists(), "Отфильтрованный лог-файл должен существовать.");
        assertTrue(filteredLogFile.exists(), "Файл с отфильтрованными логами должен существовать.");
        assertEquals("application-2023-04-01.log", response.getBody().getFilename(), "Имя файла должно быть 'application-2023-04-01.log'.");

        // Проверяем содержимое отфильтрованного файла
        try (BufferedReader reader = new BufferedReader(new FileReader(filteredLogFile))) {
            assertEquals("2023-04-01 Log entry 1", reader.readLine());
            assertEquals("2023-04-01 Log entry 2", reader.readLine());
            assertNull(reader.readLine(), "В файле не должно быть дополнительных строк.");
        } catch (IOException e) {
            fail("Не удалось прочитать отфильтрованный лог-файл. Ошибка: " + e.getMessage());
        }

        // Удаляем тестовые файлы после теста
        boolean logFileDeleted = logFile.delete();
        boolean filteredLogFileDeleted = filteredLogFile.delete();
        assertTrue(logFileDeleted, "Не удалось удалить тестовый лог-файл.");
        assertTrue(filteredLogFileDeleted, "Не удалось удалить отфильтрованный лог-файл.");
    }


    @Test
    void testGetLogFileByDate_InvalidDateFormat() {
        // Act
        ResponseEntity<FileSystemResource> response = logService.getLogFileByDate("invalid-date");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }

    @Test
    void testGetLogFileByDate_FileDoesNotExist() {
        // Arrange
        // Act
        ResponseEntity<FileSystemResource> response = logService.getLogFileByDate("01-04-2023");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }



}

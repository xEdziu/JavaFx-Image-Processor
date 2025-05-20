package dev.goral.javafximageprocessor;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerService {
    private static final String FILE_NAME = "log.txt";

    public static void log(String operationName, String level, long durationMs) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String entry = String.format("[%s] [%s] Operacja: %s, Czas: %d ms\n", time, level, operationName, durationMs);
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            writer.write(entry);
        } catch (IOException e) {
            System.err.println("Błąd zapisu logu: " + e.getMessage());
        }
    }

    public static void logError(String operationName, Exception ex) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String entry = String.format("[%s] [ERROR] Operacja: %s, Błąd: %s\n", time, operationName, ex.getMessage());
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            writer.write(entry);
        } catch (IOException e) {
            System.err.println("Błąd zapisu logu: " + e.getMessage());
        }
    }
}
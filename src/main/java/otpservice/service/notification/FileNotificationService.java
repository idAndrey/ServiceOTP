package otpservice.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import otpservice.model.User;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Реализация NotificationService для сохранения OTP-кодов в файл.
 * Путь к файлу передаётся в параметре recipient при вызове sendCode().
 */
public class FileNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);
    // Формат временной метки
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Сохраняет OTP-код в файл.
     *
     * @param user recipientPath путь к файлу, где нужно сохранить код
     * @param code          OTP-код
     */
    @Override
    public void sendCode(User user, String code, int operationNumber) {
        String recipientPath = user.getUsername();
        Path path = Paths.get(recipientPath);
        String entry = String.format("%s - OTP: %s%n",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                code);
        try {
            // Убедимся, что директория существует
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            // Запишем код в файл (создаём, если нужно, и дописываем в конец)
            Files.write(path, entry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.info("OTP code written to file {}", recipientPath);
        } catch (IOException e) {
            logger.error("Failed to write OTP to file {}", recipientPath, e);
            throw new RuntimeException("File write failed", e);
        }
    }
}


package otpservice.service.notification;

import otpservice.model.Operation;
import otpservice.model.User;

/**
 * Интерфейс для отправки OTP-кодов.
 * Реализации данного интерфейса отвечают за отправку кода
 * через разные каналы (email, SMS, Telegram, файл).
 */
public interface NotificationService {
    /**
     * Отправляет одноразовый код пользователю.
     *
     * @param userRecipient адрес или идентификатор получателя:
     *                  для email — email-адрес,
     *                  для SMS — номер телефона,
     *                  для Telegram — chatId,
     *                  для файла — путь или имя файла.
     * @param code      строковое представление OTP-кода.
     */
    void sendCode(User userRecipient, String code, int operationNumber);
}
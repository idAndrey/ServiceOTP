package otpservice.service;

import otpservice.dao.OperationDao;
import otpservice.dao.OtpCodeDao;
import otpservice.dao.OtpConfigDao;
import otpservice.dao.UserDao;
import otpservice.model.*;
import otpservice.service.notification.NotificationChannel;
import otpservice.service.notification.NotificationService;
import otpservice.service.notification.NotificationServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom random = new SecureRandom();

    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final UserDao userDao;
    private final NotificationServiceFactory notificationFactory;
    private final OperationDao operationDao;

    public OtpService(OtpCodeDao otpCodeDao,
                      OtpConfigDao otpConfigDao,
                      UserDao userDao,
                      NotificationServiceFactory notificationFactory,
                      OperationDao operationDao) {
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
        this.userDao = userDao;
        this.notificationFactory = notificationFactory;
        this.operationDao = operationDao;
    }

    /**
     * Генерирует новый OTP-код, сохраняет его в БД и возвращает строку.
     */
    public String generateOtp(Long userId, int operationNumber) {
        OtpConfig config = otpConfigDao.getConfig();
        int length = config.getLength();

        // Генерация случайного цифрового кода нужной длины
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        String code = sb.toString();

        // Сохраняем в БД
        OtpCode otp = new OtpCode(
                null,
                userId,
                operationNumber,
                code,
                OtpStatus.ACTIVE,
                LocalDateTime.now()
        );
        otpCodeDao.save(otp);
        Operation operation = operationDao.findByNumber(operationNumber);
        logger.info("Generated OTP {} for userId={}, operationNumber={}, nameOperation={}", code, userId, operationNumber, operation.getName());
        return code;
    }

    /**
     * Возвращает текущую конфигурацию длины и TTL для кодов.
     */
    public OtpConfig getConfig() {
        return otpConfigDao.getConfig();
    }

    /**
     * Сгенерировать и отправить код указанным каналом.
     */
    public void sendOtpToUser(Long userId, int operationNumber, NotificationChannel channel) {
        String code = generateOtp(userId, operationNumber);
        User user = userDao.findById(userId);
        if (user == null) {
            logger.error("sendOtpToUser: user not found, id={}", userId);
            throw new IllegalArgumentException("User not found");
        }

        // Для простоты используем username как адресат (email, sms, chatId)
        String recipient = user.getUsername();
        NotificationService svc = notificationFactory.getService(channel);
        svc.sendCode(user, code, operationNumber);
        Operation operation = operationDao.findByNumber(operationNumber);
        logger.info("Sent OTP code for userId={} via {} to confirm operationName={} [{}]", userId, channel, operation.getName(), operationNumber);
    }

    /**
     * Проверяет введённый код: активность и срок жизни, и переключает статус на USED.
     */
    public boolean validateOtpAndMark(String inputCode) {
        OtpCode otp = otpCodeDao.findByCode(inputCode);
        if (otp == null) {
            logger.warn("validateOtpAndMark: code not found {}", inputCode);
            return false;
        }
        // Проверка статуса
        if (otp.getStatus() != OtpStatus.ACTIVE) {
            logger.warn("validateOtpAndMark: code {} is not active (status={})", inputCode, otp.getStatus());
            return false;
        }
        // Проверка истечения по времени
        OtpConfig config = otpConfigDao.getConfig();
        LocalDateTime expiry = otp.getCreatedAt().plusSeconds(config.getTtlSeconds());
        if (LocalDateTime.now().isAfter(expiry)) {
            otpCodeDao.markAsExpiredOlderThan(Duration.ofSeconds(config.getTtlSeconds()));
            logger.warn("validateOtpAndMark: code {} expired at {}", inputCode, expiry);
            return false;
        }

        // Всё ок — помечаем как USED
        otpCodeDao.markAsUsed(otp.getId());
        logger.info("validateOtpAndMark: code {} validated and marked USED", inputCode);
        return true;
    }

    /**
     * Проверяет введённый код: активность и срок жизни, и НЕ переключает статус на USED.
     */
    public boolean validateOtp(String inputCode) {
        OtpCode otp = otpCodeDao.findByCode(inputCode);
        if (otp == null) {
            logger.warn("validateOtp: code not found {}", inputCode);
            return false;
        }
        // Проверка статуса
        if (otp.getStatus() != OtpStatus.ACTIVE) {
            logger.warn("validateOtp: code {} is not active (status={})", inputCode, otp.getStatus());
            return false;
        }
        // Проверка истечения по времени
        OtpConfig config = otpConfigDao.getConfig();
        LocalDateTime expiry = otp.getCreatedAt().plusSeconds(config.getTtlSeconds());
        if (LocalDateTime.now().isAfter(expiry)) {
            otpCodeDao.markAsExpiredOlderThan(Duration.ofSeconds(config.getTtlSeconds()));
            logger.warn("validateOtp: code {} expired at {}", inputCode, expiry);
            return false;
        }

        // Всё ок — помечаем как USED
//        otpCodeDao.markAsUsed(otp.getId());
//        logger.info("validateOtp: code {} validated and marked USED", inputCode);
        return true;
    }

    /**
     * Меняет статус всех просроченных кодов на EXPIRED.
     */
    public void markExpiredOtps() {
        OtpConfig config = otpConfigDao.getConfig();
        Duration ttl = Duration.ofSeconds(config.getTtlSeconds());
        otpCodeDao.markAsExpiredOlderThan(ttl);
        logger.info("markExpiredOtps: expired codes older than {} seconds", config.getTtlSeconds());
    }
}


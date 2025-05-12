package otpservice.service;

import otpservice.dao.OtpConfigDao;
import otpservice.dao.OtpCodeDao;
import otpservice.dao.UserDao;
import otpservice.model.OtpConfig;
import otpservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final OtpConfigDao configDao;
    private final UserDao userDao;
    private final OtpCodeDao codeDao;

    public AdminService(OtpConfigDao configDao, UserDao userDao, OtpCodeDao codeDao) {
        this.configDao = configDao;
        this.userDao = userDao;
        this.codeDao = codeDao;
    }

    public void updateOtpConfig(int length, int ttlSeconds) {
        // Создаем объект OtpConfig (id обычно не важен при обновлении)
        OtpConfig cfg = new OtpConfig(1L, length, ttlSeconds);
        configDao.updateConfig(cfg);
        logger.info("OTP config updated: length={}, ttlSeconds={}", length, ttlSeconds);
    }

    public List<User> getAllUsersWithoutAdmins() {
        return userDao.findAllUsersWithoutAdmins();
    }

    public void deleteUserAndCodes(Long userId) {
        codeDao.deleteAllByUserId(userId);
        userDao.delete(userId);
        logger.info("Deleted user id: {}, and user's OTP codes", userId);
    }
}



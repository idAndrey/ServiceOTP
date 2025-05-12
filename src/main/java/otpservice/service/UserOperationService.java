package otpservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import otpservice.dao.OperationDao;
import otpservice.dao.OtpCodeDao;
import otpservice.dao.UserDao;
import otpservice.dao.impl.OperationDaoImpl;
import otpservice.dao.impl.OtpCodeDaoImpl;
import otpservice.dao.impl.OtpConfigDaoImpl;
import otpservice.dao.impl.UserDaoImpl;
import otpservice.model.Operation;
import otpservice.model.OtpCode;
import otpservice.model.User;
import otpservice.service.notification.NotificationChannel;
import otpservice.service.notification.NotificationServiceFactory;
import otpservice.util.HttpUtils;
import otpservice.util.JsonUtil;
import otpservice.util.PasswordEncoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserOperationService {
    private final OtpService otpService = new OtpService(
            new OtpCodeDaoImpl(),
            new OtpConfigDaoImpl(),
            new UserDaoImpl(),
            new NotificationServiceFactory(),
            new OperationDaoImpl()
    );
    private static final Logger logger = LoggerFactory.getLogger(UserOperationService.class);
    private final OperationDao operationDao;
    private final OtpCodeDao otpCodeDao;
    private final UserDao userDao;

    public UserOperationService(OperationDao operationDao, OtpCodeDao otpCodeDao, UserDao userDao) {
        this.operationDao = operationDao;
        this.otpCodeDao = otpCodeDao;
        this.userDao = userDao;
    }

    public List<Operation> getAllOperations() {
        return operationDao.getAllOperations();
    }

    public String confirmOperation(Long userId, int operationNumber, NotificationChannel channel) {
        String response = null;
        otpService.sendOtpToUser(userId, operationNumber, channel);
        return response;
    }

    public boolean validateCode(String inputCode) {
        boolean valid;
//        otpService.sendOtpToUser(userId, operationNumber, channel);
        valid = otpService.validateOtp(inputCode);
        return valid;
    }

    public String completeOperation(String inputCode, JsonNode requestRootNode, HttpExchange exchange) throws IOException {
        OtpCode codeRaw = otpCodeDao.findByCode(inputCode);
        int operationNumber = codeRaw.getOperationNumber();
        Long userId = codeRaw.getUserId();

        String operationName = operationDao.findByNumber(operationNumber).getName();
        String userName = userDao.findById(userId).getUsername();
        Map<String, Object> response = new HashMap<>();

        response.put("operationNumber", operationNumber);
        response.put("operationName", operationName);
        response.put("username", userName);

        String result = null;
        try {



            switch (operationNumber) {
                case 101:
                    result = UpdatePassword(userId, requestRootNode);
                    break;
                case 102:
                    result = SendReport(userId, requestRootNode);
                    break;
                case 103:
                    result = MakeTransfer(userId, requestRootNode);
                    break;
                default:
                    HttpUtils.sendError(exchange, 401, "Unknown operation number");
            }

            otpCodeDao.markAsUsed(codeRaw.getId());
            logger.info("completeOperation: code {} validated and marked USED", inputCode);

            response.put("result", result);
            logger.info("Operation {} by user {} completed successful!", operationName, userName);

        } catch (IllegalArgumentException | InterruptedException e) {

            logger.error("Operation {} by user {} is failed! Error: {}", operationName, userName, e.getMessage(), e);
        }

        if(result == null) response.put("result", "failed");
        return JsonUtil.toJson(response);
    }

    public String UpdatePassword(Long userId, JsonNode requestRootNode) throws IOException, IllegalArgumentException {
        String response = null;
        JsonNode keyPassword = null;
        String newPassword = null;
        User user = null;

        keyPassword = requestRootNode.findValue("password");
        if(keyPassword != null)
        {
            newPassword = requestRootNode.get("password").asText();
            user = userDao.findById(userId);
            String hashedPassword = PasswordEncoder.hash(newPassword);
            user.setPasswordHash(hashedPassword);
            userDao.save(user);
        } else throw new IllegalArgumentException("Required parameter is missing or invalid.");

        return "success";
    }



    public String SendReport(Long userId, JsonNode requestRootNode) throws InterruptedException {

        String response = null;
        JsonNode keyPassword = null;
        String reportType = null;
        User user = null;

        keyPassword = requestRootNode.findValue("reportType");
        if(keyPassword != null)
        {
            reportType = requestRootNode.get("reportType").asText();
            user = userDao.findById(userId);

            Thread.sleep(2000);

        } else throw new IllegalArgumentException("Required parameter is missing or invalid.");

        return "success";
    }

    public String MakeTransfer(Long userId, JsonNode requestRootNode) throws InterruptedException {

        String response = null;
        JsonNode keyPassword = null;
        int amount = 0;
        User user = null;

        keyPassword = requestRootNode.findValue("amount");
        if (keyPassword != null) {
            amount = requestRootNode.get("amount").asInt();
            user = userDao.findById(userId);

            Thread.sleep(2000);

        } else throw new IllegalArgumentException("Required parameter is missing or invalid.");

        return "failed";
    }
}

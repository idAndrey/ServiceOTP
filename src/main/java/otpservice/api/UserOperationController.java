package otpservice.api;


import com.fasterxml.jackson.databind.JsonNode;

import com.sun.net.httpserver.HttpExchange;
import otpservice.dao.impl.OperationDaoImpl;
import otpservice.dao.impl.OtpCodeDaoImpl;
import otpservice.dao.impl.OtpConfigDaoImpl;
import otpservice.dao.impl.UserDaoImpl;
import otpservice.model.Operation;
import otpservice.model.User;
import otpservice.service.UserOperationService;
import otpservice.service.OtpService;
import otpservice.service.notification.NotificationChannel;
import otpservice.service.notification.NotificationServiceFactory;
import otpservice.util.HttpUtils;
import otpservice.util.JsonUtil;

import java.io.IOException;
import java.util.List;

public class UserOperationController {

//    private final OtpService otpService = new OtpService(
//            new OtpCodeDaoImpl(),
//            new OtpConfigDaoImpl(),
//            new UserDaoImpl(),
//            new NotificationServiceFactory(),
//            new OperationDaoImpl()
//    );

    private final UserOperationService userOperationService = new UserOperationService(
            new OperationDaoImpl(),
            new OtpCodeDaoImpl(),
            new UserDaoImpl()
    );

    public void getOperations(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        try {
            List<Operation> operations = userOperationService.getAllOperations();
            String json = JsonUtil.toJson(operations);
            HttpUtils.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }


    public void performOperation(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            HttpUtils.sendError(exchange, 415, "Content-Type must be application/json");
            return;
        }

        try {

            // Чтение тела запроса, парсинг JSON с помощью Jackson
            JsonNode requestRootNode = JsonUtil.getRequestBody(exchange.getRequestBody());

            // Получение номера операции
            int operationNumber = requestRootNode.get("operationNumber").asInt();

            User user = (User) exchange.getAttribute("user");
            Long exchangeUserIdAttr = (Long) exchange.getAttribute("userId");
            String channel = requestRootNode.get("channel").asText();

            String response;

            response = userOperationService.confirmOperation(exchangeUserIdAttr, operationNumber, NotificationChannel.valueOf(channel));
            HttpUtils.sendEmptyResponse(exchange, 202);


        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            System.out.println("Internal server error: " + e.getMessage());
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }

    public void confirmOperation(HttpExchange exchange) throws IOException {
        if (!"PATCH".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            HttpUtils.sendError(exchange, 415, "Content-Type must be application/json");
            return;
        }
        String response;
        try {

            // Чтение тела запроса, парсинг JSON с помощью Jackson
            JsonNode requestRootNode = JsonUtil.getRequestBody(exchange.getRequestBody());

            // Получение номера операции
            String inputCode = requestRootNode.get("code").asText();

            User user = (User) exchange.getAttribute("user");
            Long exchangeUserIdAttr = (Long) exchange.getAttribute("userId");
//            String channel = requestRootNode.get("channel").asText();

//            boolean valid = otpService.validateOtp(req.code);

            if (userOperationService.validateCode(inputCode)) {
                response = userOperationService.completeOperation(inputCode, requestRootNode, exchange);
//                HttpUtils.sendEmptyResponse(exchange, 200);
                HttpUtils.sendJsonResponse(exchange, 200, response);
            } else {
                HttpUtils.sendError(exchange, 400, "Invalid or expired code");
            }

        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            System.out.println("Internal server error: " + e.getMessage());
            HttpUtils.sendError(exchange, 500, "Internal server error");
        }
    }
}

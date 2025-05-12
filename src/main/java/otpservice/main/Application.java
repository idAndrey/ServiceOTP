package otpservice.main;

import com.sun.net.httpserver.HttpServer;
import otpservice.api.RoutesDispatcher;
import otpservice.dao.impl.OperationDaoImpl;
import otpservice.dao.impl.OtpCodeDaoImpl;
import otpservice.dao.impl.OtpConfigDaoImpl;
import otpservice.dao.impl.UserDaoImpl;
import otpservice.service.CheckExpirationScheduler;
import otpservice.service.OtpService;
import otpservice.service.notification.NotificationServiceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Точка входа приложения. Поднимает HTTP-сервер на порту из application.properties
 * и регистрирует все маршруты через Dispatcher.
 */
public class Application {
    public static void main(String[] args) {
        try {
            // Загружаем конфигурацию
            Properties config = new Properties();
            try (InputStream is = Application.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (is != null) {
                    config.load(is);
                }
            }
            int port = Integer.parseInt(config.getProperty("server.port", "8080"));
            long schedulerInterval = Long.parseLong(config.getProperty("app.scheduler.interval", "1"));

            // Создаём HTTP-сервер
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Регистрируем маршруты
            RoutesDispatcher routesDispatcher = new RoutesDispatcher();
            routesDispatcher.registerRoutes(server);

            // Создаём и запускаем планировщик
            OtpService otpService = new OtpService(
                new OtpCodeDaoImpl(),
                new OtpConfigDaoImpl(),
                new UserDaoImpl(),
                new NotificationServiceFactory(),
                new OperationDaoImpl()
            );

            // Запускаем планировщик
            CheckExpirationScheduler expirationScheduler = new CheckExpirationScheduler(otpService, schedulerInterval);
            expirationScheduler.start();
            System.out.println("ExpirationScheduler started, schedulerInterval = " + schedulerInterval);

            // Регистрируем shutdown hook для остановки планировщика при завершении работы сервера
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                expirationScheduler.stop();
                server.stop(0); // Останавливаем сервер с задержкой 0 секунд
                System.out.println("Server and scheduler stopped gracefully.");
            }));

            // Запускаем сервер
            server.start();
            System.out.println("Server started on http://localhost:" + port);

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

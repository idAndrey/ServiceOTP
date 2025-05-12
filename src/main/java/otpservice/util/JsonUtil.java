package otpservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode getRequestBody(InputStream is) throws IOException {
        String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Парсинг JSON с помощью Jackson
        return MAPPER.readTree(requestBody);
    }

    // Парсит JSON-тело запроса в объект указанного класса
    public static <T> T fromJson(InputStream is, Class<T> clazz) throws IOException {
        return MAPPER.readValue(is, clazz);
    }

    // Сериализует объект в JSON-строку
    public static String toJson(Object obj) throws IOException {
        return MAPPER.writeValueAsString(obj);
    }
}
package otpservice.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность одноразового кода (OTP).
 * Содержит информацию о сгенерированном коде, его статусе и времени создания.
 */
public class OtpCode {
    private Long id;
    private Long userId;
    private int operationNumber;   // идентификатор операции, к которой привязан код (может быть null)
    private String code;          // сам OTP
    private OtpStatus status;     // статус кода: ACTIVE, EXPIRED, USED
    private LocalDateTime createdAt;

    /**
     * Пустой конструктор для фреймворков и JDBC.
     */
    public OtpCode() {
    }

    /**
     * Полный конструктор.
     *
     * @param id                уникальный идентификатор записи
     * @param userId            идентификатор пользователя, для которого сгенерирован код
     * @param operationNumber   идентификатор операции (nullable)
     * @param code              одноразовый код
     * @param status            статус кода
     * @param createdAt         дата и время создания кода
     */
    public OtpCode(Long id,
                   Long userId,
                   int operationNumber,
                   String code,
                   OtpStatus status,
                   LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.operationNumber = operationNumber;
        this.code = code;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getOperationNumber() {
        return operationNumber;
    }

    public void setOperationNumber(int operationNumber) {
        this.operationNumber = operationNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public void setStatus(OtpStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OtpCode otpCode = (OtpCode) o;
        return Objects.equals(id, otpCode.id)
                && Objects.equals(userId, otpCode.userId)
                && Objects.equals(operationNumber, otpCode.operationNumber)
                && Objects.equals(code, otpCode.code)
                && status == otpCode.status
                && Objects.equals(createdAt, otpCode.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, operationNumber, code, status, createdAt);
    }

    @Override
    public String toString() {
        return "OtpCode{" +
                "id=" + id +
                ", userId=" + userId +
                ", operationNumber='" + operationNumber + '\'' +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}


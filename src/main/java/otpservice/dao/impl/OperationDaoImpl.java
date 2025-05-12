package otpservice.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import otpservice.config.DatabaseManager;
import otpservice.dao.OperationDao;
import otpservice.model.Operation;
import otpservice.model.OtpCode;
import otpservice.model.User;
import otpservice.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OperationDaoImpl implements OperationDao {
    private static final Logger logger = LoggerFactory.getLogger(OperationDaoImpl.class);

    private static final String SELECT_ALL_OPERATIONS_SQL =
            "SELECT * FROM operations";
    private static final String SELECT_BY_NUMBER_SQL =
            "SELECT * FROM operations WHERE number = ?";

    @Override
    public Operation findByNumber(int operationNumber) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NUMBER_SQL)) {
            ps.setInt(1, operationNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Operation found = mapRow(rs);
                    logger.info("Found operation by number {}: {}", operationNumber, found);
                    return found;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding operation by number [{}]: {}", operationNumber, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Operation> getAllOperations() {
        List<Operation> operations = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_OPERATIONS_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                operations.add(mapRow(rs));
            }
            logger.info("Found {} operations", operations.size());
        } catch (SQLException e) {
            logger.error("Error fetching operations: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return operations;
    }

    /**
     * Вспомогательный метод для маппинга строки ResultSet в объект User.
     */
    private Operation mapRow(ResultSet rs) throws SQLException {
        Operation operation = new Operation();
        operation.setId(rs.getLong("id"));
        operation.setNumber(rs.getInt("number"));
        operation.setName(rs.getString("name"));
        operation.setDescription(rs.getString("description"));
        return operation;
    }
}

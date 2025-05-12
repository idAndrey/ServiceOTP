package otpservice.dao;

import otpservice.model.Operation;

import java.util.List;

public interface OperationDao {

    List<Operation> getAllOperations();
    Operation findByNumber(int operationNumber);
}

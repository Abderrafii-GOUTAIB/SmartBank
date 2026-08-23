package org.example.app.transaction;

import org.example.app.transaction.dto.TransactionResponse;
import org.example.model.TransactionResult;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(TransactionResult result) {
        return switch (result) {
            case TransactionResult.Success s -> new TransactionResponse(
                    s.accountNumber(),
                    s.amount(),
                    s.balanceAfter(),
                    s.type().name(),
                    true,
                    "Success : You have successfully completed the transaction.");
            case TransactionResult.Failure f -> new TransactionResponse(
                    f.accountNumber(),
                    f.amount(),
                    0.0,
                    null,
                    false,
                    f.message());
        };
    }
}


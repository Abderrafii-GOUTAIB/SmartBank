package org.example.app.transaction;

import jakarta.validation.Valid;
import org.example.app.transaction.dto.AmountRequest;
import org.example.app.transaction.dto.TransactionResponse;
import org.example.model.TransactionResult;
import org.example.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class TransactionController {

    private final AccountService accountService;
    private final TransactionMapper transactionMapper;

    public TransactionController(AccountService accountService,
                                 TransactionMapper transactionMapper) {
        this.accountService = accountService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping("/{accountNumber}/deposits")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {

        TransactionResult result = accountService.depositByAccountNumber(
                accountNumber, request.amount());

        return ResponseEntity.ok(transactionMapper.toResponse(result));
    }

    @PostMapping("/{accountNumber}/withdrawals")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {

        TransactionResult result = accountService.withdrawByAccountNumber(
                accountNumber, request.amount());

        return ResponseEntity.ok(transactionMapper.toResponse(result));
    }
}


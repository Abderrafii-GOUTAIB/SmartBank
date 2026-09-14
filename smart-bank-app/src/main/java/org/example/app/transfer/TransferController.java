package org.example.app.transfer;

import org.example.app.transfer.dto.TransferRequest;
import org.example.model.TransferResult;
import org.example.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST entry point for transfers.
 * Delegates to AccountService.transferByAccountNumber (the REST operation).
 */
@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final AccountService accountService;

    public TransferController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<TransferResult> transfer(@RequestBody TransferRequest request) {
        TransferResult result = accountService.transferByAccountNumber(
                request.fromAccountNumber(),
                request.toAccountNumber(),
                request.amount());
        return ResponseEntity.ok(result);
    }
}
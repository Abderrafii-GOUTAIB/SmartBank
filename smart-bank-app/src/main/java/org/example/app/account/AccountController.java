package org.example.app.account;


import jakarta.validation.Valid;
import org.example.app.account.dto.AccountResponse;
import org.example.app.account.dto.OpenAccountRequest;
import org.example.model.AccountStatus;
import org.example.model.AccountType;
import org.example.model.BankAccount;
import org.example.service.AccountService;
import org.example.service.AccountTypeFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for account resources.
 * SRP: HTTP concerns ONLY — routing, status codes, delegation.
 * NO business logic here (that lives in AccountService).
 * DIP: depends on AccountService (injected), not concrete details.
 */


@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> openAccount(@Valid @RequestBody OpenAccountRequest request) {

        AccountType accountType = AccountTypeFactory.create(request.accountType());
        BankAccount account = accountService.openAccount(
                request.ownerName(),
                request.email(),
                request.initialDeposit(),
                accountType
        );
        AccountResponse response = accountMapper.toResponse(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get an account by its number.
     * GET /accounts/{accountNumber} → 200 OK
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber) {

        BankAccount account = accountService.getRepository().getByAccountNumber(accountNumber);
        AccountResponse response = accountMapper.toResponse(account);

        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<BankAccount> all = accountService.getRepository().getAll();

        int from = page * size;
        int to = Math.min(from + size, all.size());
        List<AccountResponse> pageContent = all.subList(from, to).stream()
                .map(accountMapper::toResponse)
                .toList();

        return ResponseEntity.ok(pageContent);
    }

}


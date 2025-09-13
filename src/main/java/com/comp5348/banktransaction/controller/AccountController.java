package com.comp5348.banktransaction.controller;

import com.comp5348.banktransaction.dto.AccountDTO;
import com.comp5348.banktransaction.model.AccountType;
import com.comp5348.banktransaction.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

/**
 * Interface
 */
@RestController
@RequestMapping("/api/customer/{customerId}/account")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(
            @PathVariable Long customerId, @RequestBody CreateAccountRequest request) {
        //get account type from request
        AccountType accountType = request.accountType;
        AccountDTO account = accountService.createAccount(customerId, request.accountName, accountType);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> getAccount(
            @PathVariable Long customerId,
            @PathVariable Long accountId) {
        AccountDTO account = accountService.getAccount(customerId, accountId);
        return ResponseEntity.ok(account);
    }

    // Add request validation on account type, MUST be [PERSONAL | BUSINESS | REVENUE]
    public static class CreateAccountRequest {
        public String accountName;

        public AccountType accountType;
    }

    public static class ErrorResponse {
        public String status;
        public String errorMessage;

        public ErrorResponse(String status, String errorMessage) {
            this.status = status;
            this.errorMessage = errorMessage;
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormat(Exception ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.name(), ex.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

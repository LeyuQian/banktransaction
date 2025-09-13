package com.comp5348.banktransaction.service;

import com.comp5348.banktransaction.dto.TransactionRecordDTO;
import com.comp5348.banktransaction.errors.InsufficientBalanceException;
import com.comp5348.banktransaction.errors.NegativeTransferAmountException;
import com.comp5348.banktransaction.model.Account;
import com.comp5348.banktransaction.model.TransactionRecord;
import com.comp5348.banktransaction.repository.AccountRepository;
import com.comp5348.banktransaction.repository.CustomerRepository;
import com.comp5348.banktransaction.repository.TransactionRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic for creating and managing transactions (transfer / deposit).
 */
@Service
public class TransactionRecordService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private static final Logger log = LoggerFactory.getLogger(TransactionRecordService.class);

    //TODO: update revenue account id here manually after creation
    public static final Long REVENUE_ACCOUNT_ID = 5L;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TransactionRecordService(AccountRepository accountRepository, CustomerRepository customerRepository, TransactionRecordRepository transactionRecordRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Transactional
    public TransactionRecordDTO performTransaction(
            Long fromCustomerId, Long fromAccountId,
            Long toCustomerId, Long toAccountId,
            Double amount, String memo)
            throws InsufficientBalanceException, HttpClientErrorException {
        if (amount <= 0) {
            throw new NegativeTransferAmountException();
        }

        Account fromAccount = null;
        if (fromAccountId != null) {
            fromAccount = accountRepository
                    .findByIdAndCustomer(fromAccountId, customerRepository.getReferenceById(fromCustomerId))
                    .orElseThrow();
            entityManager.refresh(fromAccount);

            if (fromAccount.getBalance() < amount) {
                throw new InsufficientBalanceException();
            }
            fromAccount.modifyBalance(-amount);
            accountRepository.save(fromAccount);
        }

        Account toAccount = null;
        // if the toAccount is BUSINESS Account, require merchant fee
        // set two variables to track merchant fee and the actual amount transfer to toAccount
        var actualAmount = amount;
        Double merchantFee = 0.0;
        double merchantFeePercentage = 0.02;

        if (toAccountId != null) {
            toAccount = accountRepository
                    .findByIdAndCustomer(toAccountId, customerRepository.getReferenceById(toCustomerId))
                    .orElseThrow();
            // if toAccount type is BUSINESS, update actual transfer amount and merchant fee
            if (toAccount.getAccountType().name().equals("BUSINESS")) {
                merchantFee = amount * merchantFeePercentage;
                actualAmount = amount - merchantFee;
            }
            log.info("Performing {}, toAccount type is {}, actual transfer amount: {}, merchant fee: {}",
                    memo, toAccount.getAccountType().name(), actualAmount, merchantFee);

            //update toAccount balance with actual amount
            toAccount.modifyBalance(actualAmount);
            accountRepository.save(toAccount);
        }

        TransactionRecord transactionRecord = new TransactionRecord(actualAmount, toAccount, fromAccount, memo);
        // default merchant fee is 0, so if is BUSINESS account, will set record with updated merchant fee
        // otherwise merchant fee will still be set to 0
        transactionRecord.setMerchantFee(merchantFee);
        log.info("Create and save new transaction record in database, transaction: {}", transactionRecord.toString());
        transactionRecordRepository.save(transactionRecord);

        // transfer to revenue account if BUSINESS account
        if (toAccount != null && toAccount.getAccountType().name().equals("BUSINESS")) {
            Account revenueAccount = accountRepository.findById(REVENUE_ACCOUNT_ID).orElseThrow();
            revenueAccount.modifyBalance(merchantFee);
            log.info("Transfer merchant fee to revenue account, merchant fee: {}", merchantFee);
            accountRepository.save(revenueAccount);
        }

        return new TransactionRecordDTO(transactionRecord);
    }

}

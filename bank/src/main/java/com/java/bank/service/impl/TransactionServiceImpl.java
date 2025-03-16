package com.java.bank.service.impl;

import com.java.bank.dto.TransactionDTO;
import com.java.bank.entity.Transaction;
import com.java.bank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService{

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public void saveTransaction(TransactionDTO transactiondto) {
        Transaction transaction = new Transaction(
                null,
                transactiondto.getTransactionType(),
                transactiondto.getAmount(),
                transactiondto.getAccountNumber(),
                "SUCCESS",
                null,
                null
        );
        transactionRepository.save(transaction);
        System.out.println("Transaction saved successfully");
    }
}

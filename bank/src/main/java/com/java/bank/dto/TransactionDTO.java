package com.java.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

    private String transactionType;

    private BigDecimal amount;

    private String accountNumber;

    private String status;
}

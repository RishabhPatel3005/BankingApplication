package com.java.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    private String sourceAccountNumber;

    private String destinationAccountNumber;

    private BigDecimal amount;
}

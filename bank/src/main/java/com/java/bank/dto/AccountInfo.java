package com.java.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfo {

    @Schema(name = "User Account Name")
    private String accountName;

    @Schema(name = "User Account Balance")
    private BigDecimal accountBalance;

    @Schema(name = "User Account Number")
    private String accountNumber;
}

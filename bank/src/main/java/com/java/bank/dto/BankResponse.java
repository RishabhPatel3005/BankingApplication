package com.java.bank.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankResponse {

    private String responseCode;

    private String responseMessage;

    private AccountInfo accountInfo;


}

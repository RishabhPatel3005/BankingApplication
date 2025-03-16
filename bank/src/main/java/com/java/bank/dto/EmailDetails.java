package com.java.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails {

    private String recipient;

    private String messageBody;

    private String subject;

    private String attachment;
}

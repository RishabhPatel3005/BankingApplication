package com.java.bank.service.impl;

import com.java.bank.dto.*;

public interface UserService {

    BankResponse createAccount(UserRequest userRequest);

    BankResponse balanceEnquiry(EnquiryRequest request);

    BankResponse login(LoginDto loginDto);

    String nameEnquiry(EnquiryRequest request);

    BankResponse creditAmount(CreditDebitRequest request);

    BankResponse debitAmount(CreditDebitRequest request);

    BankResponse transferAmount(TransferRequest request);

}

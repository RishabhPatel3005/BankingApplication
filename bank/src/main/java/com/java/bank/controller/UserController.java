package com.java.bank.controller;

import com.java.bank.dto.*;
import com.java.bank.service.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name="User Account Management APIs")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
            summary = "Create New User Account",
            description = "Creating an user and assigning an account ID"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Http Status 201 Created"
    )
    @PostMapping
    public BankResponse createAccount(@RequestBody UserRequest userRequest){
        return userService.createAccount(userRequest);
    }

    @Operation(
            summary = "Balance Enquiry",
            description = "Given an account number, checks how much balance user has"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 Success"
    )
    @GetMapping("/balanceEnquiry")
    public BankResponse balanceEnquiry(@RequestBody EnquiryRequest request){
        return userService.balanceEnquiry(request);
    }

    @Operation(
            summary = "Name Enquiry",
            description = "Given an account number,gives the username of the user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 201 Success"
    )
    @GetMapping("/nameEnquiry")
    public String nameEnquiry(@RequestBody EnquiryRequest request){
        return userService.nameEnquiry(request);
    }

    @Operation(
            summary = "Credit Amount",
            description = "Credits the given amount to the given user account"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Http Status 201 Success"
    )
    @PostMapping("/credit")
    public BankResponse creditAccount(@RequestBody CreditDebitRequest request){
        return userService.creditAmount(request);
    }

    @Operation(
            summary = "Debit Amount",
            description = "Debits the given amount to the given user account"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Http Status 201 Success"
    )
    @PostMapping("/debit")
    public BankResponse debitAccount(@RequestBody CreditDebitRequest request){
        return userService.debitAmount(request);
    }

    @Operation(
            summary = "Transfer Amount",
            description = "Transferring the given amount from source account to destination account"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Http Status 201 Success"
    )
    @PostMapping("/transfer")
    public BankResponse transferAmount(@RequestBody TransferRequest request){
        return userService.transferAmount(request);
    }

    @PostMapping("/login")
    public BankResponse login(@RequestBody LoginDto loginDto){
        return userService.login(loginDto);
    }
}

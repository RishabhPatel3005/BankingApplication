package com.java.bank.service.impl;

import com.java.bank.config.JwtTokenProvider;
import com.java.bank.dto.*;
import com.java.bank.entity.Role;
import com.java.bank.entity.User;
import com.java.bank.repository.UserRepository;
import com.java.bank.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public BankResponse createAccount(UserRequest userRequest) {

        //If user already exists
        if(userRepository.existsByEmail(userRequest.getEmail())){
            return new BankResponse(AccountUtils.ACCOUNT_EXISTS_CODE,AccountUtils.ACCOUNT_EXISTS_MESSAGE,null);
        }

        //Creating a new account - saving user details in db
        User newUser = new User(
                null,
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getOtherName(),
                userRequest.getGender(),
                userRequest.getAddress(),
                userRequest.getStateOfOrigin(),
                AccountUtils.generateAccountNumber(),
                BigDecimal.ZERO,
                userRequest.getEmail(),
                passwordEncoder.encode(userRequest.getPassword()),
                userRequest.getPhoneNumber(),
                userRequest.getAlternativePhoneNumber(),
                "ACTIVE",
                Role.valueOf("ROLE_ADMIN"),
                null,
                null);

        User savedUser = userRepository.save(newUser);

        //Send Email Alert
        EmailDetails emailDetails = new EmailDetails(
                savedUser.getEmail(),
                "Congratulations! Your account has been successfully created.\n" +
                        "Your Account Details:\n" +
                        "Account Name: " + savedUser.getFirstName() + " " + savedUser.getLastName() + " " + savedUser.getOtherName() + "\n" +
                        "Account Number: " + savedUser.getAccountNumber(),
                "ACCOUNT CREATION",
                null
        );
        emailService.sendEmailAlert(emailDetails);

        return new BankResponse(AccountUtils.ACCOUNT_CREATION_SUCCESS,
                AccountUtils.ACCOUNT_CREATION_MESSAGE,
                new AccountInfo(
                        savedUser.getFirstName()+" "+savedUser.getLastName()+" "+savedUser.getOtherName(),
                        savedUser.getAccountBalance(),
                        savedUser.getAccountNumber()
                ));
    }

    //Balance enquiry
    @Override
    public BankResponse balanceEnquiry(EnquiryRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if(!isAccountExist){
            return new BankResponse(AccountUtils.ACCOUNT_NOT_EXIST_CODE,
                    AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE,
                    null);
        }
        User foundUser = userRepository.findByAccountNumber(request.getAccountNumber());
        return new BankResponse(AccountUtils.ACCOUNT_FOUND_CODE,
                AccountUtils.ACCOUNT_FOUND_MESSAGE,
                new AccountInfo(
                        foundUser.getFirstName()+" "+foundUser.getLastName()+" "+foundUser.getOtherName(),
                        foundUser.getAccountBalance(),
                        foundUser.getAccountNumber()
                ));
    }

    @Override
    public BankResponse login(LoginDto loginDto) {
        Authentication authentication = null;
        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(),loginDto.getPassword()));
        EmailDetails loginALert = new EmailDetails(
                loginDto.getEmail(),
                "You logged in your account. If you didn't initiate this request, please contact the bank immediately",
                "You're Logged in!!",
                null
        );
        emailService.sendEmailAlert(loginALert);
        return new BankResponse(
                "LOGIN SUCCESS",
                jwtTokenProvider.generateToken(authentication),
                null
        );
    }

    //Name Enquiry
    @Override
    public String nameEnquiry(EnquiryRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if(!isAccountExist){
            return AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE;
        }
        User foundUser = userRepository.findByAccountNumber(request.getAccountNumber());
        return foundUser.getFirstName()+" "+foundUser.getLastName()+" "+foundUser.getOtherName();
    }

    //Credit Amount
    @Override
    public BankResponse creditAmount(CreditDebitRequest request) {
        //checking if the account exists
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if(!isAccountExist){
            return new BankResponse(AccountUtils.ACCOUNT_NOT_EXIST_CODE,
                    AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE,
                    null);
        }
        User userToCredit = userRepository.findByAccountNumber(request.getAccountNumber());
        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(request.getAmount()));
        userRepository.save(userToCredit);

        //Save Transaction
        TransactionDTO transactionDTO = new TransactionDTO(
                "CREDIT",
                request.getAmount(),
                userToCredit.getAccountNumber(),
                null
        );
        transactionService.saveTransaction(transactionDTO);

        //Emailing the details
        EmailDetails emailDetails = new EmailDetails(
                userToCredit.getEmail(),
                "The sum of Rs."+request.getAmount()+" has been credited to your account! Your account balance is Rs."+userToCredit.getAccountBalance(),
                "CREDIT ALERT",
                null
        );
        emailService.sendEmailAlert(emailDetails);


        return new BankResponse(AccountUtils.ACCOUNT_CREDITED_SUCCESS,
                AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE,
                new AccountInfo(
                        userToCredit.getFirstName()+" "+userToCredit.getLastName()+" "+userToCredit.getOtherName(),
                        userToCredit.getAccountBalance(),
                        userToCredit.getAccountNumber()
                ));
    }

    //Debit Amount
    @Override
    public BankResponse debitAmount(CreditDebitRequest request) {
        //Checking if the account exist
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if(!isAccountExist){
            return new BankResponse(AccountUtils.ACCOUNT_NOT_EXIST_CODE,
                    AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE,
                    null);
        }

        //Check if the amount you intend to withdraw is not more than current amount
        User userToDebit = userRepository.findByAccountNumber(request.getAccountNumber());
        BigInteger availableBalance = userToDebit.getAccountBalance().toBigInteger();
        BigInteger debitAmount = request.getAmount().toBigInteger();
        if(availableBalance.intValue() < debitAmount.intValue()){
            return new BankResponse(AccountUtils.INSUFFICIENT_BALANCE_CODE,
                    AccountUtils.INSUFFICIENT_BALANCE_MESSAGE,
                    new AccountInfo(
                            userToDebit.getFirstName()+" "+userToDebit.getLastName()+" "+userToDebit.getOtherName(),
                            userToDebit.getAccountBalance(),
                            userToDebit.getAccountNumber()
                    ));
        }else{
            userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(request.getAmount()));
            userRepository.save(userToDebit);

            //Saving Transaction
            TransactionDTO transactionDTO = new TransactionDTO(
                    "DEBIT",
                    request.getAmount(),
                    userToDebit.getAccountNumber(),
                    null
            );
            transactionService.saveTransaction(transactionDTO);

            //Emailing the details
            EmailDetails emailDetails = new EmailDetails(
                    userToDebit.getEmail(),
                    "The amount of Rs."+request.getAmount()+" has been debited from your account! Your current balance is Rs."+userToDebit.getAccountBalance(),
                    "DEBIT ALERT",
                    null
            );
            emailService.sendEmailAlert(emailDetails);

            return new BankResponse(AccountUtils.ACCOUNT_DEBITED_SUCCESS,
                    AccountUtils.ACCOUNT_DEBITED_MESSAGE,
                    new AccountInfo(
                            userToDebit.getFirstName()+" "+userToDebit.getLastName()+" "+userToDebit.getOtherName(),
                            userToDebit.getAccountBalance(),
                            userToDebit.getAccountNumber()
                    ));
        }

    }

    //Transfer Amount
    @Override
    public BankResponse transferAmount(TransferRequest request) {
        //get the account for debit(Check if the account exist)
        boolean isDestinationAccountExist = userRepository.existsByAccountNumber(request.getDestinationAccountNumber());
        if(!isDestinationAccountExist){
            return new BankResponse(AccountUtils.ACCOUNT_NOT_EXIST_CODE,
                    AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE,
                    null);
        }
        User sourceAccountUser = userRepository.findByAccountNumber(request.getSourceAccountNumber());
        String sourceUsername = sourceAccountUser.getFirstName()+" "+sourceAccountUser.getLastName()+" "+sourceAccountUser.getOtherName();
        //Check if the amount debiting is not more the current balance
        if(request.getAmount().compareTo(sourceAccountUser.getAccountBalance()) > 0){
            return new BankResponse(AccountUtils.INSUFFICIENT_BALANCE_CODE,
                    AccountUtils.INSUFFICIENT_BALANCE_MESSAGE,
                    new AccountInfo(
                            sourceAccountUser.getFirstName()+" "+sourceAccountUser.getLastName()+" "+sourceAccountUser.getOtherName(),
                            sourceAccountUser.getAccountBalance(),
                            sourceAccountUser.getAccountNumber()
                    ));
        }
        //Debit the account
        sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(sourceAccountUser);

        //Saving the transaction
        TransactionDTO debittransactionDTO = new TransactionDTO(
                "DEBIT",
                request.getAmount(),
                request.getSourceAccountNumber(),
                null
        );
        transactionService.saveTransaction(debittransactionDTO);

        //Emailing the details
        EmailDetails debitAlert = new EmailDetails(
                sourceAccountUser.getEmail(),
                "The sum of Rs."+request.getAmount()+" has been debited from your account! Your current balance is Rs."+sourceAccountUser.getAccountBalance(),
                "DEBIT ALERT",
                null
        );
        emailService.sendEmailAlert(debitAlert);

        //Credit to the destination account
        User destinationAccountUser = userRepository.findByAccountNumber(request.getDestinationAccountNumber());
        //Credit the account
        destinationAccountUser.setAccountBalance(destinationAccountUser.getAccountBalance().add(request.getAmount()));
        userRepository.save(destinationAccountUser);

        //Saving the transaction
        TransactionDTO creditTransactionDTO = new TransactionDTO(
                "CREDIT",
                request.getAmount(),
                request.getDestinationAccountNumber(),
                null
        );
        transactionService.saveTransaction(creditTransactionDTO);

        //Emailing the details
        EmailDetails creditAlert = new EmailDetails(
                destinationAccountUser.getEmail(),
                "The sum of Rs."+request.getAmount()+" has been credited to your account from "+sourceUsername+" ! Your current balance is Rs."+destinationAccountUser.getAccountBalance(),
                "CREDIT ALERT",
                null
        );
        emailService.sendEmailAlert(creditAlert);
        return new BankResponse(
                AccountUtils.TRANSFER_SUCCESSFUL_CODE,
                AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE,
                null
        );
    }

}

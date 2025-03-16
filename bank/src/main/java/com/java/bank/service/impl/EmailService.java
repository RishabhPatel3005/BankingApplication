package com.java.bank.service.impl;

import com.java.bank.dto.EmailDetails;

public interface EmailService {

    void sendEmailAlert(EmailDetails emailDetails);

    void sendEmailWithAttachment(EmailDetails emailDetails);
}

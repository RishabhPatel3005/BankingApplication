package com.java.bank.service.impl;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.java.bank.dto.EmailDetails;
import com.java.bank.entity.Transaction;
import com.java.bank.entity.User;
import com.java.bank.repository.TransactionRepository;
import com.java.bank.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class BankStatement {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private static final String FILE = "C:\\Users\\Rishabh\\Documents\\MyStatement.pdf";

    /*
    *retrieve list of transactions within a date range for a given account number
    * generate a pdf file for transactions
    * send the file via email
     */

    public List<Transaction> generateStatement(String accountNumber,String startDate,String endDate) throws IOException {

        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate,DateTimeFormatter.ISO_DATE);

        List<Transaction> transactionList = transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getAccountNumber().equals(accountNumber))
                .filter(transaction ->
                        !transaction.getCreatedAt().isBefore(start) && !transaction.getCreatedAt().isAfter(end)
                ).toList();// Includes range

        User user = userRepository.findByAccountNumber(accountNumber);
        String customerName = user.getFirstName()+" "+user.getLastName()+" "+user.getOtherName();

        PdfWriter writer = new PdfWriter(new FileOutputStream(FILE));
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument,PageSize.A4);

        log.info("Setting Size Of Document");

        //Bank Info Table
        Table bankInfoTable = new Table(1);
        bankInfoTable.setWidth(UnitValue.createPercentValue(100));
        bankInfoTable.addCell(createCell("The Bank",new DeviceRgb(0,0,255),true));
        bankInfoTable.addCell(createCell("Some Address,Bihar,India",null,false));

        //Statement Info Table
        Table statementInfo = new Table(2);
        statementInfo.setWidth(UnitValue.createPercentValue(100));
        statementInfo.addCell(createCell("Start Date: "+startDate,null,false));
        statementInfo.addCell(createCell("STATEMENT OF ACCOUNT",null,true));
        statementInfo.addCell(createCell("End Date: "+endDate,null,false));
        statementInfo.addCell(createCell("Customer Name: "+customerName,null,false));
        statementInfo.addCell(createCell("",null,false));
        statementInfo.addCell(createCell("Customer Address: "+user.getAddress(),null,false));

        //Transactions Table
        Table transactionsTable = new Table(4);
        transactionsTable.setWidth(UnitValue.createPercentValue(100));
        transactionsTable.addCell(createCell("DATE",new DeviceRgb(0,0,255),true));
        transactionsTable.addCell(createCell("TRANSACTION TYPE",new DeviceRgb(0,0,255),true));
        transactionsTable.addCell(createCell("TRANSACTION AMOUNT",new DeviceRgb(0,0,255),true));
        transactionsTable.addCell(createCell("STATUS",new DeviceRgb(0,0,255),true));

        transactionList.forEach(
                transaction -> {
                    try {
                        transactionsTable.addCell(createCell(transaction.getCreatedAt().toString(),null,false));
                        transactionsTable.addCell(createCell(transaction.getTransactionType(),null,false));
                        transactionsTable.addCell(createCell(transaction.getAmount().toString(),null,false));
                        transactionsTable.addCell(createCell(transaction.getStatus(),null,false));
                    } catch (IOException e) {
                        log.error("Error adding transaction data to PDF",e);
                    }

                }
        );

        //Adding tables to document
        document.add(bankInfoTable);
        document.add(statementInfo);
        document.add(transactionsTable);

        document.close();
        log.info("PDF generated successfully at: "+FILE);

        //Sending the mail with attachment
        EmailDetails emailDetails = new EmailDetails(
                user.getEmail(),
                "Kindly find your requested account statement attached!",
                "STATEMENT OF ACCOUNT",
                FILE
        );
        emailService.sendEmailWithAttachment(emailDetails);
        return transactionList;
    }

    private Cell createCell(String content, DeviceRgb backgroundColor, boolean bold) throws IOException {
        PdfFont font = bold
                ? PdfFontFactory.createFont("Helvetica-Bold", PdfEncodings.WINANSI)
                : PdfFontFactory.createFont("Helvetica", PdfEncodings.WINANSI);

        Paragraph paragraph = new Paragraph(content).setFont(font);

        Cell cell = new Cell().add(paragraph);
        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor);
        }
        cell.setTextAlignment(TextAlignment.LEFT);

        return cell;
    }


}

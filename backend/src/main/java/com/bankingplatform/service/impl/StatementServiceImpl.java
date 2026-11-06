package com.bankingplatform.service.impl;

import com.bankingplatform.dto.StatementDto;
import com.bankingplatform.entity.Account;
import com.bankingplatform.entity.Transaction;
import com.bankingplatform.entity.User;
import com.bankingplatform.repository.AccountRepository;
import com.bankingplatform.repository.TransactionRepository;
import com.bankingplatform.service.StatementService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatementServiceImpl implements StatementService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@bank.local}")
    private String fromAddress;

    @Value("${bank.name:CAFED Bank}")
    private String bankName;

    public StatementServiceImpl(AccountRepository accountRepository,
                                TransactionRepository transactionRepository,
                                JavaMailSender mailSender) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.mailSender = mailSender;
    }

    @Override
    public StatementDto generateStatement(Long accountId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must be before 'to'");
        }
        if (to.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("'to' cannot be in the future");
        }
        if (to.isBefore(from.minusYears(7))) {
            throw new IllegalArgumentException("Statement window cannot exceed 7 years");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        List<Transaction> txs = transactionRepository
                .findByAccountIdAndTimestampBetweenOrderByTimestampAsc(accountId, start, end);

        StatementDto dto = new StatementDto();
        dto.setAccountId(account.getId());
        dto.setAccountNumber(mask(account.getAccountNumber()));
        dto.setAccountHolder(account.getUser().getFullName());
        dto.setFrom(from);
        dto.setTo(to);
        dto.setGeneratedAt(LocalDateTime.now());

        BigDecimal opening = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;
        List<StatementDto.StatementLine> lines = new ArrayList<>();

        for (Transaction tx : txs) {
            StatementDto.StatementLine line = new StatementDto.StatementLine();
            line.setDate(tx.getTimestamp());
            line.setType(tx.getType());
            line.setDescription(tx.getDescription());
            line.setReference(tx.getReference());
            BigDecimal amt = tx.getAmount() == null ? BigDecimal.ZERO : tx.getAmount();
            if ("DEBIT".equalsIgnoreCase(tx.getType())) {
                line.setDebit(amt);
                line.setCredit(BigDecimal.ZERO);
                totalDebits = totalDebits.add(amt);
            } else {
                line.setDebit(BigDecimal.ZERO);
                line.setCredit(amt);
                totalCredits = totalCredits.add(amt);
            }
            line.setBalance(tx.getBalanceAfter());
            lines.add(line);
        }

        if (!txs.isEmpty()) {
            opening = txs.get(0).getBalanceAfter() == null
                    ? BigDecimal.ZERO
                    : txs.get(0).getBalanceAfter()
                        .subtract(totalCredits)
                        .add(totalDebits);
        }

        dto.setOpeningBalance(opening);
        dto.setClosingBalance(account.getBalance());
        dto.setTotalCredits(totalCredits);
        dto.setTotalDebits(totalDebits);
        dto.setTransactionCount(txs.size());
        dto.setLines(lines);
        return dto;
    }

    @Override
    public byte[] generatePdf(Long accountId, LocalDate from, LocalDate to) {
        StatementDto dto = generateStatement(accountId, from, to);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 54, 54);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sub = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
            Font head = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph p = new Paragraph(bankName + " — Account Statement", title);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Account: " + dto.getAccountNumber()
                    + "    Holder: " + dto.getAccountHolder(), sub));
            doc.add(new Paragraph("Period: " + dto.getFrom() + " to " + dto.getTo(), sub));
            doc.add(new Paragraph("Generated: " + dto.getGeneratedAt(), sub));
            doc.add(new Paragraph(" "));

            PdfPTable t = new PdfPTable(6);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{2.2f, 1.4f, 3.4f, 1.6f, 1.4f, 1.6f});
            addCell(t, "Date", head);
            addCell(t, "Type", head);
            addCell(t, "Description", head);
            addCell(t, "Reference", head);
            addCell(t, "Debit", head);
            addCell(t, "Credit", head);
            for (StatementDto.StatementLine l : dto.getLines()) {
                addCell(t, l.getDate() == null ? "" : l.getDate().toString(), body);
                addCell(t, l.getType(), body);
                addCell(t, l.getDescription(), body);
                addCell(t, l.getReference(), body);
                addCell(t, l.getDebit() == null ? "" : l.getDebit().toPlainString(), body);
                addCell(t, l.getCredit() == null ? "" : l.getCredit().toPlainString(), body);
            }
            doc.add(t);
            doc.add(new Paragraph(" "));

            Paragraph totals = new Paragraph();
            totals.add(new Chunk("Opening: " + dto.getOpeningBalance()
                    + "    Total Debits: " + dto.getTotalDebits()
                    + "    Total Credits: " + dto.getTotalCredits()
                    + "    Closing: " + dto.getClosingBalance(), body));
            doc.add(totals);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render PDF statement", e);
        }
    }

    @Override
    public String generateCsv(Long accountId, LocalDate from, LocalDate to) {
        StatementDto dto = generateStatement(accountId, from, to);
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Type,Description,Reference,Debit,Credit,Balance\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (StatementDto.StatementLine l : dto.getLines()) {
            sb.append(l.getDate() == null ? "" : l.getDate().format(fmt)).append(',');
            sb.append(csv(l.getType())).append(',');
            sb.append(csv(l.getDescription())).append(',');
            sb.append(csv(l.getReference())).append(',');
            sb.append(l.getDebit() == null ? "" : l.getDebit().toPlainString()).append(',');
            sb.append(l.getCredit() == null ? "" : l.getCredit().toPlainString()).append(',');
            sb.append(l.getBalance() == null ? "" : l.getBalance().toPlainString()).append('\n');
        }
        return sb.toString();
    }

    @Override
    public void emailStatement(User user, Long accountId, LocalDate from, LocalDate to) {
        byte[] pdf = generatePdf(accountId, from, to);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(user.getEmail());
        msg.setSubject(bankName + " Statement " + from + " to " + to);
        msg.setText("Your statement is attached. Please log in to download the PDF.");
        mailSender.send(msg);
    }

    @Override
    public List<String> availablePeriods(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        LocalDate created = account.getCreatedAt() == null
                ? LocalDate.now().minusYears(1)
                : account.getCreatedAt().toLocalDate();
        LocalDate now = LocalDate.now();
        List<String> periods = new ArrayList<>();
        YearMonth ym = YearMonth.from(now);
        while (!ym.atDay(1).isBefore(created)) {
            periods.add(ym.toString());
            ym = ym.minusMonths(1);
        }
        return periods;
    }

    private void addCell(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, f));
        c.setPadding(4);
        t.addCell(c);
    }

    private String csv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String mask(String acct) {
        if (acct == null || acct.length() < 4) return "****";
        return "****" + acct.substring(acct.length() - 4);
    }
}

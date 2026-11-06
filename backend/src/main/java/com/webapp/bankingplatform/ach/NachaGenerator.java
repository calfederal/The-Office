package com.webapp.bankingplatform.ach;

import com.webapp.bankingplatform.entity.AchBatch;
import com.webapp.bankingplatform.entity.AchFile;
import com.webapp.bankingplatform.entity.AchTransfer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds NACHA-compliant fixed-width (94 char) records.
 * Layouts follow the sample file:
 *   1 File Header
 *   5 Batch Header
 *   6 Entry Detail
 *   8 Batch Control
 *   9 File Control
 *   9 padding records to reach 10-record blocks
 */
@Component
public class NachaGenerator {

    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HHmm");

    /** Right-justify numeric, zero-fill, truncate if too long. */
    private static String num(long value, int width) {
        String s = Long.toString(value);
        if (s.length() > width) s = s.substring(s.length() - width);
        return String.format("%" + width + "s", s).replace(' ', '0');
    }

    /** Right-justify amount in cents (10 digits). */
    private static String amount(BigDecimal v) {
        long cents = v.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        return num(cents, 10);
    }

    /** Left-justify alpha, blank-fill, truncate if too long. */
    private static String alpha(String s, int width) {
        if (s == null) s = "";
        if (s.length() > width) s = s.substring(0, width);
        return String.format("%-" + width + "s", s);
    }

    /** Compute check digit for 9-digit ABA routing: 3*(d1+d4+d7) + 7*(d2+d5+d8) + (d3+d6+d9) mod 10. */
    public static char checkDigit(String routing9) {
        int[] d = new int[9];
        for (int i = 0; i < 9; i++) d[i] = routing9.charAt(i) - '0';
        int sum = 3 * (d[0] + d[3] + d[6])
                + 7 * (d[1] + d[4] + d[7])
                + (d[2] + d[5] + d[8]);
        int cd = (10 - (sum % 10)) % 10;
        return (char) ('0' + cd);
    }

    /** 10-char immediate destination: leading space + 9-digit routing + check digit. */
    public static String immediateDestination(String routing9) {
        return " " + routing9 + checkDigit(routing9);
    }

    /** 10-char immediate origin: 1 + 9-digit company tax ID (or ODFI routing). */
    public static String immediateOrigin(String companyId9) {
        return "1" + companyId9;
    }

    public String generate(AchFile file) {
        List<String> lines = new ArrayList<>();
        lines.add(fileHeader(file));
        for (AchBatch batch : file.getBatches()) {
            lines.add(batchHeader(batch));
            for (AchTransfer t : batch.getEntries()) {
                lines.add(entryDetail(t));
            }
            lines.add(batchControl(batch));
        }
        lines.add(fileControl(file));

        // Pad to multiple of 10 with 9-records filled with 9s.
        while (lines.size() % NachaConstants.BLOCKING_FACTOR != 0) {
            lines.add(paddingRecord());
        }

        StringBuilder sb = new StringBuilder();
        for (String l : lines) {
            if (l.length() != NachaConstants.RECORD_LENGTH) {
                throw new IllegalStateException("Record length " + l.length() + " != 94: " + l);
            }
            sb.append(l).append('\n');
        }
        return sb.toString();
    }

    private String fileHeader(AchFile f) {
        StringBuilder s = new StringBuilder(NachaConstants.RECORD_LENGTH);
        s.append(NachaConstants.FILE_HEADER);                              // 1
        s.append(' ');                                                     // 2 priority code
        s.append(' ');                                                     // 3
        s.append(alpha(f.getImmediateDestination(), 10));                  // 4-13
        s.append(alpha(f.getImmediateOrigin(), 10));                       // 14-23
        s.append(f.getFileCreationDate().format(YYMMDD));                  // 24-29
        s.append(f.getFileCreationDate().format(HHMM));                    // 30-33
        s.append(alpha(f.getFileId(), 1));                                 // 34 file id
        s.append("094");                                                   // 35-37 record size
        s.append("10");                                                    // 38-39 blocking factor
        s.append("1");                                                     // 40 format code
        s.append(alpha(f.getDestinationName(), 23));                       // 41-63
        s.append(alpha(f.getOriginName(), 23));                            // 64-86
        s.append(alpha("", 8));                                            // 87-94 reference code
        return pad(s);
    }

    private String batchHeader(AchBatch b) {
        StringBuilder s = new StringBuilder();
        s.append(NachaConstants.BATCH_HEADER);                             // 1
        s.append(alpha(b.getServiceClassCode(), 3));                       // 2-4
        s.append(alpha(b.getCompanyName(), 16));                           // 5-20
        s.append(alpha(b.getCompanyEntryDescription(), 10));               // 21-30
        s.append(alpha("", 10));                                           // 31-40 company discretionary
        s.append(alpha(b.getCompanyId(), 10));                             // 41-50
        s.append(alpha(b.getSecCode(), 3));                                // 51-53
        s.append(alpha(b.getCompanyEntryDescription(), 10));               // 54-63 company entry desc
        s.append(b.getEffectiveDate().format(YYMMDD));                     // 64-69
        s.append(alpha("", 3));                                            // 70-72 settlement date (julian)
        s.append(' ');                                                     // 73 originator status code
        s.append(alpha(b.getOriginatingDfiId(), 8));                       // 74-81
        s.append(num(Long.parseLong(b.getBatchNumber()), 7));              // 82-88 batch number
        return pad(s);
    }

    private String entryDetail(AchTransfer t) {
        StringBuilder s = new StringBuilder();
        s.append(NachaConstants.ENTRY_DETAIL);                             // 1
        s.append(alpha(t.getTransactionCode(), 2));                        // 2-3
        s.append(alpha(t.getReceivingDfiRouting(), 9));                    // 4-12
        s.append(checkDigit(t.getReceivingDfiRouting()));                  // 13
        s.append(alpha(t.getReceivingAccountNumber(), 17));                // 14-30
        s.append(amount(t.getAmount()));                                   // 31-40
        s.append(alpha(t.getIndividualId(), 15));                          // 41-55
        s.append(alpha(t.getIndividualName(), 22));                        // 56-77
        s.append(alpha("", 2));                                            // 78-79 discretionary
        s.append(alpha(t.getTraceNumber().substring(8), 7));                // 80-86 addenda seq indicator
        s.append(alpha(t.getTraceNumber().substring(0, 8), 8));            // 87-94 trace number
        return pad(s);
    }

    private String batchControl(AchBatch b) {
        StringBuilder s = new StringBuilder();
        s.append(NachaConstants.BATCH_CONTROL);                            // 1
        s.append(' ');                                                     // 2 service class (echo)
        s.append(alpha(b.getServiceClassCode(), 3));                       // 3-5
        s.append(num(b.getEntryCount(), 6));                               // 6-11 entry/addenda count
        s.append(alpha("", 10));                                           // 12-21 entry hash (left 0)
        s.append(num(b.getTotalDebitAmount().movePointRight(2).longValueExact(), 12)); // 22-33 debit
        s.append(num(b.getTotalCreditAmount().movePointRight(2).longValueExact(), 12));// 34-45 credit
        s.append(alpha(b.getCompanyId(), 10));                             // 46-55
        s.append(alpha("", 19));                                           // 56-74 message auth / discretionary
        s.append(alpha(b.getOriginatingDfiId(), 8));                       // 75-82
        s.append(num(Long.parseLong(b.getBatchNumber()), 7));              // 83-89
        return pad(s);
    }

    private String fileControl(AchFile f) {
        int batchCount = f.getBatches().size();
        long entryCount = f.getBatches().stream().mapToLong(AchBatch::getEntryCount).sum();
        long blockCount = (long) Math.ceil(
            (double) (1 + batchCount * 2 + entryCount + 1) / NachaConstants.BLOCKING_FACTOR);
        BigDecimal totalDebit = f.getBatches().stream()
            .map(AchBatch::getTotalDebitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = f.getBatches().stream()
            .map(AchBatch::getTotalCreditAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder s = new StringBuilder();
        s.append(NachaConstants.FILE_CONTROL);                             // 1
        s.append(num(batchCount, 6));                                      // 2-7 batch count
        s.append(num(entryCount, 8));                                      // 8-15 block count
        s.append(num(entryCount, 8));                                      // 16-23 entry/addenda count
        s.append(alpha("", 10));                                           // 24-33 entry hash
        s.append(num(totalDebit.movePointRight(2).longValueExact(), 12));  // 34-45 total debit
        s.append(num(totalCredit.movePointRight(2).longValueExact(), 12)); // 46-57 total credit
        s.append(alpha("", 39));                                           // 58-96 reserved
        return pad(s);
    }

    private String paddingRecord() {
        StringBuilder s = new StringBuilder(NachaConstants.RECORD_LENGTH);
        for (int i = 0; i < NachaConstants.RECORD_LENGTH; i++) s.append('9');
        return s.toString();
    }

    private String pad(StringBuilder s) {
        while (s.length() < NachaConstants.RECORD_LENGTH) s.append(' ');
        if (s.length() > NachaConstants.RECORD_LENGTH) {
            return s.substring(0, NachaConstants.RECORD_LENGTH);
        }
        return s.toString();
    }

    /** Convenience: generate a single-batch file from a list of transfers. */
    public String generateSingle(String odfiRouting9, String companyId9, String companyName,
                                 String companyEntryDescription, LocalDate effectiveDate,
                                 List<AchTransfer> transfers) {
        AchFile file = new AchFile();
        file.setFileId("A");
        file.setImmediateDestination(immediateDestination(odfiRouting9));
        file.setImmediateOrigin(immediateOrigin(companyId9));
        file.setDestinationName("BANK");
        file.setOriginName(companyName);
        file.setFileCreationDate(LocalDate.now());

        AchBatch batch = new AchBatch();
        batch.setBatchNumber("0000001");
        batch.setServiceClassCode(NachaConstants.SERVICE_CLASS_CREDITS);
        batch.setCompanyName(companyName);
        batch.setCompanyId(companyId9);
        batch.setSecCode(NachaConstants.SEC_CCD);
        batch.setCompanyEntryDescription(companyEntryDescription);
        batch.setEffectiveDate(effectiveDate);
        batch.setOriginatingDfiId(odfiRouting9);
        for (AchTransfer t : transfers) batch.addEntry(t);
        file.addBatch(batch);

        return generate(file);
    }
}

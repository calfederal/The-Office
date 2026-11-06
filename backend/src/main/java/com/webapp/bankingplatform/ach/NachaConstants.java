package com.webapp.bankingplatform.ach;

/**
 * NACHA file format constants. All records are 94 characters wide.
 * See: https://achdevguide.nacha.org/
 */
public final class NachaConstants {

    private NachaConstants() {}

    public static final int RECORD_LENGTH = 94;
    public static final int BLOCKING_FACTOR = 10;

    // Record type codes
    public static final char FILE_HEADER = '1';
    public static final char BATCH_HEADER = '5';
    public static final char ENTRY_DETAIL = '6';
    public static final char ADDENDA = '7';
    public static final char BATCH_CONTROL = '8';
    public static final char FILE_CONTROL = '9';

    // Service class codes
    public static final String SERVICE_CLASS_MIXED = "200";
    public static final String SERVICE_CLASS_CREDITS = "220";
    public static final String SERVICE_CLASS_DEBITS = "225";

    // Standard Entry Class codes (most common)
    public static final String SEC_CCD = "CCD"; // Corporate Credit or Debit
    public static final String SEC_PPD = "PPD"; // Prearranged Payment and Deposit
    public static final String SEC_WEB = "WEB"; // Internet Initiated
    public static final String SEC_TEL = "TEL"; // Telephone Initiated
    public static final String SEC_COR = "COR"; // Notification of Change
    public static final String SEC_ADV = "ADV"; // Automated Accounting Advice
    public static final String SEC_RCK = "RCK"; // Re-presented Check

    // Transaction codes
    public static final String TC_CHECKING_CREDIT = "22";
    public static final String TC_CHECKING_CREDIT_PRENOTE = "23";
    public static final String TC_CHECKING_DEBIT = "27";
    public static final String TC_CHECKING_DEBIT_PRENOTE = "28";
    public static final String TC_SAVINGS_CREDIT = "32";
    public static final String TC_SAVINGS_CREDIT_PRENOTE = "33";
    public static final String TC_SAVINGS_DEBIT = "37";
    public static final String TC_SAVINGS_DEBIT_PRENOTE = "38";

    // Transfer direction
    public static final String DIRECTION_CREDIT = "CREDIT";
    public static final String DIRECTION_DEBIT = "DEBIT";

    // Transfer status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_BATCHED = "BATCHED";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_SETTLED = "SETTLED";
    public static final String STATUS_RETURNED = "RETURNED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_NOC = "NOC"; // Notification of Change

    // Batch status
    public static final String BATCH_DRAFT = "DRAFT";
    public static final String BATCH_READY = "READY";
    public static final String BATCH_SUBMITTED = "SUBMITTED";
    public static final String BATCH_SETTLED = "SETTLED";

    // File status
    public static final String FILE_DRAFT = "DRAFT";
    public static final String FILE_GENERATED = "GENERATED";
    public static final String FILE_SUBMITTED = "SUBMITTED";
    public static final String FILE_ACKNOWLEDGED = "ACKNOWLEDGED";

    // Common return reason codes (R-codes)
    public static final String[] RETURN_CODES = {
        "R01", "R02", "R03", "R04", "R05", "R06", "R07", "R08", "R09", "R10",
        "R11", "R12", "R13", "R14", "R15", "R16", "R17", "R18", "R19", "R20",
        "R21", "R22", "R23", "R24", "R25", "R26", "R27", "R28", "R29", "R30",
        "R31", "R32", "R33", "R34", "R35", "R36", "R37", "R38", "R39"
    };
}

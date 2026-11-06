package com.webapp.bankingplatform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ach_transfers", indexes = {
    @Index(name = "idx_ach_trace", columnList = "traceNumber", unique = true),
    @Index(name = "idx_ach_status", columnList = "status"),
    @Index(name = "idx_ach_effective", columnList = "effectiveDate")
})
public class AchTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 15-digit ODFI + sequence. Unique per entry. */
    @Column(nullable = false, unique = true, length = 15)
    private String traceNumber;

    /** CREDIT or DEBIT (inbound to RDFI). */
    @Column(nullable = false, length = 10)
    private String direction;

    /** Standard Entry Class: CCD, PPD, WEB, TEL, etc. */
    @Column(nullable = false, length = 3)
    private String secCode;

    /** 22, 27, 32, 37, etc. */
    @Column(nullable = false, length = 2)
    private String transactionCode;

    /** 9-digit ABA routing of the external counterparty's bank. */
    @Column(nullable = false, length = 9)
    private String receivingDfiRouting;

    /** External account number (up to 17 chars). */
    @Column(nullable = false, length = 17)
    private String receivingAccountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 22)
    private String individualName;

    @Column(length = 15)
    private String individualId;

    /** PENDING, BATCHED, SUBMITTED, SETTLED, RETURNED, CANCELLED, NOC. */
    @Column(nullable = false, length = 15)
    private String status;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private LocalDate settlementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private AchBatch batch;

    /** Internal account affected by this transfer. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    /** R01..R39 for returns, COR for notification of change. */
    @Column(length = 4)
    private String returnCode;

    @Column(length = 80)
    private String returnReason;

    @Column(length = 100)
    private String memo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = "PENDING";
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTraceNumber() { return traceNumber; }
    public void setTraceNumber(String traceNumber) { this.traceNumber = traceNumber; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getSecCode() { return secCode; }
    public void setSecCode(String secCode) { this.secCode = secCode; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public String getReceivingDfiRouting() { return receivingDfiRouting; }
    public void setReceivingDfiRouting(String receivingDfiRouting) { this.receivingDfiRouting = receivingDfiRouting; }
    public String getReceivingAccountNumber() { return receivingAccountNumber; }
    public void setReceivingAccountNumber(String receivingAccountNumber) { this.receivingAccountNumber = receivingAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getIndividualName() { return individualName; }
    public void setIndividualName(String individualName) { this.individualName = individualName; }
    public String getIndividualId() { return individualId; }
    public void setIndividualId(String individualId) { this.individualId = individualId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
    public AchBatch getBatch() { return batch; }
    public void setBatch(AchBatch batch) { this.batch = batch; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public String getReturnCode() { return returnCode; }
    public void setReturnCode(String returnCode) { this.returnCode = returnCode; }
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

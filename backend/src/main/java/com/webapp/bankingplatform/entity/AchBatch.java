package com.webapp.bankingplatform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ach_batches", indexes = {
    @Index(name = "idx_batch_number", columnList = "batchNumber", unique = true),
    @Index(name = "idx_batch_status", columnList = "status")
})
public class AchBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 7-digit sequence within a file. */
    @Column(nullable = false, unique = true, length = 7)
    private String batchNumber;

    /** 200 (mixed), 220 (credits), 225 (debits). */
    @Column(nullable = false, length = 3)
    private String serviceClassCode;

    @Column(nullable = false, length = 16)
    private String companyName;

    @Column(nullable = false, length = 10)
    private String companyId;

    @Column(nullable = false, length = 3)
    private String secCode;

    @Column(nullable = false, length = 10)
    private String companyEntryDescription;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    /** DRAFT, READY, SUBMITTED, SETTLED. */
    @Column(nullable = false, length = 15)
    private String status;

    /** First 8 digits of originating DFI routing. */
    @Column(nullable = false, length = 8)
    private String originatingDfiId;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalDebitAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalCreditAmount = BigDecimal.ZERO;

    private int entryCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private AchFile file;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AchTransfer> entries = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = "DRAFT";
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addEntry(AchTransfer t) {
        entries.add(t);
        t.setBatch(this);
        entryCount = entries.size();
        if ("CREDIT".equals(t.getDirection())) {
            totalCreditAmount = totalCreditAmount.add(t.getAmount());
        } else {
            totalDebitAmount = totalDebitAmount.add(t.getAmount());
        }
    }

    // Getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public String getServiceClassCode() { return serviceClassCode; }
    public void setServiceClassCode(String serviceClassCode) { this.serviceClassCode = serviceClassCode; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getSecCode() { return secCode; }
    public void setSecCode(String secCode) { this.secCode = secCode; }
    public String getCompanyEntryDescription() { return companyEntryDescription; }
    public void setCompanyEntryDescription(String companyEntryDescription) { this.companyEntryDescription = companyEntryDescription; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOriginatingDfiId() { return originatingDfiId; }
    public void setOriginatingDfiId(String originatingDfiId) { this.originatingDfiId = originatingDfiId; }
    public BigDecimal getTotalDebitAmount() { return totalDebitAmount; }
    public void setTotalDebitAmount(BigDecimal totalDebitAmount) { this.totalDebitAmount = totalDebitAmount; }
    public BigDecimal getTotalCreditAmount() { return totalCreditAmount; }
    public void setTotalCreditAmount(BigDecimal totalCreditAmount) { this.totalCreditAmount = totalCreditAmount; }
    public int getEntryCount() { return entryCount; }
    public void setEntryCount(int entryCount) { this.entryCount = entryCount; }
    public AchFile getFile() { return file; }
    public void setFile(AchFile file) { this.file = file; }
    public List<AchTransfer> getEntries() { return entries; }
    public void setEntries(List<AchTransfer> entries) { this.entries = entries; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

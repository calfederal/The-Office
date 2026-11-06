package com.webapp.bankingplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ach_files", indexes = {
    @Index(name = "idx_file_id", columnList = "fileId", unique = true),
    @Index(name = "idx_file_status", columnList = "status")
})
public class AchFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Single character A-Z or 0-9, distinguishes files generated same second. */
    @Column(nullable = false, unique = true, length = 1)
    private String fileId;

    /** 10-char immediate destination (RDFI routing + leading space, or 10-digit). */
    @Column(nullable = false, length = 10)
    private String immediateDestination;

    /** 10-char immediate origin (ODFI routing, or 1+9-digit company ID). */
    @Column(nullable = false, length = 10)
    private String immediateOrigin;

    @Column(nullable = false, length = 23)
    private String destinationName;

    @Column(nullable = false, length = 23)
    private String originName;

    @Column(nullable = false)
    private LocalDate fileCreationDate;

    /** DRAFT, GENERATED, SUBMITTED, ACKNOWLEDGED. */
    @Column(nullable = false, length = 15)
    private String status;

    /** Generated NACHA file content (94-char fixed-width lines). */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String fileContent;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AchBatch> batches = new ArrayList<>();

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

    public void addBatch(AchBatch b) {
        batches.add(b);
        b.setFile(this);
    }

    // Getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getImmediateDestination() { return immediateDestination; }
    public void setImmediateDestination(String immediateDestination) { this.immediateDestination = immediateDestination; }
    public String getImmediateOrigin() { return immediateOrigin; }
    public void setImmediateOrigin(String immediateOrigin) { this.immediateOrigin = immediateOrigin; }
    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }
    public String getOriginName() { return originName; }
    public void setOriginName(String originName) { this.originName = originName; }
    public LocalDate getFileCreationDate() { return fileCreationDate; }
    public void setFileCreationDate(LocalDate fileCreationDate) { this.fileCreationDate = fileCreationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFileContent() { return fileContent; }
    public void setFileContent(String fileContent) { this.fileContent = fileContent; }
    public List<AchBatch> getBatches() { return batches; }
    public void setBatches(List<AchBatch> batches) { this.batches = batches; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

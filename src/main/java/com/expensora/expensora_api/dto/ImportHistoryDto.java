package com.expensora.expensora_api.dto;

import com.expensora.expensora_api.entity.ImportFormat;
import com.expensora.expensora_api.entity.ImportStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class ImportHistoryDto {
    private UUID id;
    private String fileName;
    private ImportFormat format;
    private Integer totalRecords;
    private Integer successfulRecords;
    private Integer failedRecords;
    private Integer duplicateRecords;
    private String errorLog;
    private ImportStatus status;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public ImportFormat getFormat() { return format; }
    public void setFormat(ImportFormat format) { this.format = format; }
    public Integer getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }
    public Integer getSuccessfulRecords() { return successfulRecords; }
    public void setSuccessfulRecords(Integer successfulRecords) { this.successfulRecords = successfulRecords; }
    public Integer getFailedRecords() { return failedRecords; }
    public void setFailedRecords(Integer failedRecords) { this.failedRecords = failedRecords; }
    public Integer getDuplicateRecords() { return duplicateRecords; }
    public void setDuplicateRecords(Integer duplicateRecords) { this.duplicateRecords = duplicateRecords; }
    public String getErrorLog() { return errorLog; }
    public void setErrorLog(String errorLog) { this.errorLog = errorLog; }
    public ImportStatus getStatus() { return status; }
    public void setStatus(ImportStatus status) { this.status = status; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

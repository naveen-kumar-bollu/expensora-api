package com.expensora.expensora_api.dto;

import java.util.List;
import java.util.Map;

public class ImportPreviewDto {
    private List<Map<String, String>> previewData;
    private List<String> headers;
    private Integer totalRows;
    private List<String> detectedColumns;
    private Map<String, String> columnMapping;

    // getters and setters
    public List<Map<String, String>> getPreviewData() { return previewData; }
    public void setPreviewData(List<Map<String, String>> previewData) { this.previewData = previewData; }
    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }
    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }
    public List<String> getDetectedColumns() { return detectedColumns; }
    public void setDetectedColumns(List<String> detectedColumns) { this.detectedColumns = detectedColumns; }
    public Map<String, String> getColumnMapping() { return columnMapping; }
    public void setColumnMapping(Map<String, String> columnMapping) { this.columnMapping = columnMapping; }
}

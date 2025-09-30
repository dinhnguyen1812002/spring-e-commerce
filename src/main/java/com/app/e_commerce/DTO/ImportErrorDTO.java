package com.app.e_commerce.DTO;

import com.app.e_commerce.entity.ImportError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportErrorDTO {
    
    private Long id;
    private Integer rowNumber;
    private String columnName;
    private String errorMessage;
    private String cellValue;
    private ImportError.ErrorType errorType;
    
    public ImportErrorDTO() {}
    
    public ImportErrorDTO(ImportError importError) {
        this.id = importError.getId();
        this.rowNumber = importError.getRowNumber();
        this.columnName = importError.getColumnName();
        this.errorMessage = importError.getErrorMessage();
        this.cellValue = importError.getCellValue();
        this.errorType = importError.getErrorType();
    }
}

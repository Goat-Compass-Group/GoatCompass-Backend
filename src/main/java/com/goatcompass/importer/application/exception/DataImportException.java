package com.goatcompass.importer.application.exception;

import com.goatcompass.shared.application.exception.ServiceException;
import lombok.Getter;

import java.util.List;

@Getter
public class DataImportException extends ServiceException {

    private final List<String> errors;

    public DataImportException(String target, List<String> errors) {
        super("Nạp dữ liệu " + target + " thất bại (" + errors.size() + " lỗi):\n  - " + String.join("\n  - ", errors));
        this.errors = List.copyOf(errors);
    }

}

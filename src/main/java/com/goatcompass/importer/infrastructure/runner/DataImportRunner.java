package com.goatcompass.importer.infrastructure.runner;

import com.goatcompass.importer.application.exception.DataImportException;
import com.goatcompass.importer.application.service.DataImportService;
import com.goatcompass.taxonomy.api.TaxonomyImportApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data-import.enabled", havingValue = "true")
public class DataImportRunner implements ApplicationRunner {

    private final DataImportService dataImport;

    @Override
    public void run(ApplicationArguments args) {
        try {
            TaxonomyImportApi.ImportResult result = dataImport.importTaxonomy(false);
            StringBuilder report = new StringBuilder("Đã nạp taxonomy:");
            result.counts().forEach(c -> report.append(String.format(
                    "%n  %-12s mới %3d · cập nhật %3d · giữ nguyên %3d", c.type(), c.created(), c.updated(), c.unchanged())));
            log.info(report.toString());
            if (!result.warnings().isEmpty()) {
                log.warn("Cảnh báo dữ liệu taxonomy ({}):\n  - {}", result.warnings().size(),
                        String.join("\n  - ", result.warnings()));
            }
        } catch (DataImportException e) {
            log.error(e.getMessage());
            throw new IllegalStateException("Dữ liệu taxonomy không hợp lệ, dừng khởi động. Xem log phía trên.", e);
        }
    }
}

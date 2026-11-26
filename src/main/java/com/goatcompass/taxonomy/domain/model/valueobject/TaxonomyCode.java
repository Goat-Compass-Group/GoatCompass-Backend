package com.goatcompass.taxonomy.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object: mã ổn định của một mục taxonomy (kỹ năng, nghề, lĩnh vực, chứng chỉ).
 * Chữ thường tiếng Anh nối bằng gạch ngang, ví dụ {@code meta-ads}. Không bao giờ đổi hay dùng lại.
 */
public record TaxonomyCode(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    public TaxonomyCode {
        Objects.requireNonNull(value, "code");
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Mã taxonomy không hợp lệ: '" + value + "'");
        }
    }

    public static TaxonomyCode of(String value) {
        return new TaxonomyCode(value);
    }

    public static boolean isValid(String value) {
        return value != null && FORMAT.matcher(value).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}

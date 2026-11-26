package com.goatcompass.taxonomy.domain.model.valueobject;

/**
 * Value object: mức độ thành thạo theo thang chung 1–4
 * (1 Cơ bản · 2 Làm được việc · 3 Thành thạo · 4 Chuyên gia). Mức 0 ("chưa có") không được lưu.
 */
public record ProficiencyLevel(int value) {

    public static final ProficiencyLevel WORKING = new ProficiencyLevel(2);

    public ProficiencyLevel {
        if (value < 1 || value > 4) {
            throw new IllegalArgumentException("Mức độ phải trong khoảng 1–4: " + value);
        }
    }

    public static ProficiencyLevel of(int value) {
        return new ProficiencyLevel(value);
    }
}

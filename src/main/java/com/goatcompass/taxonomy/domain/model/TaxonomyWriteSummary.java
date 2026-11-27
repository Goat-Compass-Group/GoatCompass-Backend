package com.goatcompass.taxonomy.domain.model;

import java.util.List;

public record TaxonomyWriteSummary(List<Line> lines) {

    public TaxonomyWriteSummary {
        lines = List.copyOf(lines);
    }

    public boolean hasChanges() {
        return lines.stream().anyMatch(l -> l.created() > 0 || l.updated() > 0);
    }

    public Line line(String type) {
        return lines.stream().filter(l -> l.type().equals(type)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không có loại " + type));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Line l : lines) {
            sb.append(String.format("%n  %-14s tổng %3d · mới %3d · cập nhật %3d · giữ nguyên %3d",
                    l.type(), l.total(), l.created(), l.updated(), l.unchanged()));
        }
        return sb.toString();
    }

    public record Line(String type, int created, int updated, int unchanged) {

        public int total() {
            return created + updated + unchanged;
        }
    }
}

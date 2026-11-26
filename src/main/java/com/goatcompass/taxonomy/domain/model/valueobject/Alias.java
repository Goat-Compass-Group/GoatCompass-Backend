package com.goatcompass.taxonomy.domain.model.valueobject;

/**
 * @param text       cách viết gốc
 * @param normalized dạng đã chuẩn hoá để so khớp (xem {@code AliasNormalizer})
 * @param lang       ISO 639-1: vi, en
 */
public record Alias(String text, String normalized, String lang) {
}
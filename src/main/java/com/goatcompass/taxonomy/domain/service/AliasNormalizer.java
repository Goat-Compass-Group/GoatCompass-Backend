package com.goatcompass.taxonomy.domain.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Chuẩn hoá cụm từ trong JD/CV trước khi so với alias.
 * Chữ thường → bỏ dấu tiếng Việt (NFD, bỏ dấu, đ → d) → giữ {@code + # .} (C++, C#, .NET) → gộp khoảng trắng.
 * Người Việt hay gõ không dấu nên bước bỏ dấu là bắt buộc; nhập nhằng do bỏ dấu được xử lý ở bước LLM.
 */
public final class AliasNormalizer {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NOT_ALLOWED = Pattern.compile("[^\\p{L}\\p{N}\\s+#.]");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private AliasNormalizer() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String s = text.toLowerCase(Locale.ROOT).replace('đ', 'd');
        s = DIACRITICS.matcher(Normalizer.normalize(s, Normalizer.Form.NFD)).replaceAll("");
        s = NOT_ALLOWED.matcher(s).replaceAll(" ");
        return SPACES.matcher(s).replaceAll(" ").strip();
    }
}
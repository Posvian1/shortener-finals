package com.promoit.shortener.core.util;

import com.promoit.shortener.core.port.LinkRepository;

import java.security.SecureRandom;

public class ShortCodeGenerator {
    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_ATTEMPTS = 100;

    private final int length;
    private final SecureRandom random = new SecureRandom();

    public ShortCodeGenerator(int length) {
        if (length < 4) throw new IllegalArgumentException("length must be >= 4");
        this.length = length;
    }

    public String generate(LinkRepository repo) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = randomCode();
            if (!repo.existsByCode(code)) return code;
        }
        throw new IllegalStateException("Не удалось сгенерировать уникальный код");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
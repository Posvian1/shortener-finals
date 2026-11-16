package com.promoit.shortener;

import com.promoit.shortener.core.util.ShortCodeGenerator;
import com.promoit.shortener.infra.InMemoryLinkRepository;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {
    @Test void generatesUniqueCodes() {
        var repo = new InMemoryLinkRepository();
        var gen = new ShortCodeGenerator(7);
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) codes.add(gen.generate(repo));
        assertEquals(1000, codes.size());
    }

    @Test void respectsLength() {
        var gen = new ShortCodeGenerator(10);
        assertEquals(10, gen.generate(new InMemoryLinkRepository()).length());
    }

    @Test void rejectsTooShortLength() {
        assertThrows(IllegalArgumentException.class, () -> new ShortCodeGenerator(2));
    }
}
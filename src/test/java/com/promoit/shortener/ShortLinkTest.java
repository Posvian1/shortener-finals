package com.promoit.shortener;

import com.promoit.shortener.core.model.ShortLink;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShortLinkTest {

    private ShortLink link(int max, Duration ttl) {
        return new ShortLink("abc", "https://x.io", UUID.randomUUID(),
                Instant.now(), ttl, max);
    }

    @Test void notExpiredBeforeTtl() {
        assertFalse(link(10, Duration.ofHours(1)).isExpired(Instant.now()));
    }

    @Test void expiredAfterTtl() {
        Instant now = Instant.now();
        ShortLink l = link(10, Duration.ofHours(1));
        assertTrue(l.isExpired(now.plus(Duration.ofHours(2))));
    }

    @Test void exhaustedWhenClicksEqualMax() {
        ShortLink l = link(2, Duration.ofDays(1));
        l.registerClick(); l.registerClick();
        assertTrue(l.isExhausted());
    }

    @Test void registerClickFailsWhenExhausted() {
        ShortLink l = link(1, Duration.ofDays(1));
        l.registerClick();
        assertThrows(IllegalStateException.class, l::registerClick);
    }

    @Test void setMaxClicksRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> link(5, Duration.ofDays(1)).setMaxClicks(-1));
    }
}
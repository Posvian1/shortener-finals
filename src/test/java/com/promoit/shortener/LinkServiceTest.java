package com.promoit.shortener;

import com.promoit.shortener.core.model.ShortLink;
import com.promoit.shortener.core.service.LinkService;
import com.promoit.shortener.core.util.ShortCodeGenerator;
import com.promoit.shortener.infra.InMemoryLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {

    private InMemoryLinkRepository repo;
    private LinkService service;
    private MutableClock clock;
    private UUID owner;

    static class MutableClock extends Clock {
        Instant now = Instant.now();
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
        void plus(Duration d) { now = now.plus(d); }
    }

    @BeforeEach
    void setup() {
        repo = new InMemoryLinkRepository();
        clock = new MutableClock();
        service = new LinkService(repo, new ShortCodeGenerator(7),
                new com.promoit.shortener.infra.ConsoleNotifier(),
                Duration.ofSeconds(60), clock);
        owner = UUID.randomUUID();
    }

    @Test void twoUsersGetDifferentCodesForSameUrl() {
        ShortLink a = service.create(owner, "https://x.io", 10);
        ShortLink b = service.create(UUID.randomUUID(), "https://x.io", 10);
        assertNotEquals(a.getShortCode(), b.getShortCode());
    }

    @Test void openIncrementsClicks() {
        var l = service.create(owner, "https://x.io", 10);
        service.open(owner, l.getShortCode());
        assertEquals(1, repo.findByCode(l.getShortCode()).orElseThrow().getCurrentClicks());
    }

    @Test void blocksAfterLimit() {
        var l = service.create(owner, "https://x.io", 1);
        service.open(owner, l.getShortCode());
        assertThrows(IllegalStateException.class, () -> service.open(owner, l.getShortCode()));
    }

    @Test void blocksAfterTtl() {
        var l = service.create(owner, "https://x.io", 10);
        clock.plus(Duration.ofSeconds(120));
        assertThrows(IllegalStateException.class, () -> service.open(owner, l.getShortCode()));
    }

    @Test void cleanupRemovesExpired() {
        service.create(owner, "https://x.io", 10);
        clock.plus(Duration.ofSeconds(120));
        service.cleanupExpired();
        assertTrue(repo.findAll().isEmpty());
    }

    @Test void ownershipEnforcedOnEdit() {
        var l = service.create(owner, "https://x.io", 10);
        UUID other = UUID.randomUUID();
        assertThrows(SecurityException.class,
                () -> service.editMaxClicks(other, l.getShortCode(), 5));
    }

    @Test void ownershipEnforcedOnOpen() {
        var l = service.create(owner, "https://x.io", 10);
        assertThrows(SecurityException.class,
                () -> service.open(UUID.randomUUID(), l.getShortCode()));
    }

    @Test void invalidUrlRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(owner, "not-a-url", 10));
    }

    @Test void editChangesLimit() {
        var l = service.create(owner, "https://x.io", 10);
        service.editMaxClicks(owner, l.getShortCode(), 3);
        assertEquals(3, repo.findByCode(l.getShortCode()).orElseThrow().getMaxClicks());
    }
}
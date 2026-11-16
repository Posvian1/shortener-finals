package com.promoit.shortener;

import com.promoit.shortener.core.service.LinkService;
import com.promoit.shortener.core.util.ShortCodeGenerator;
import com.promoit.shortener.infra.ConsoleNotifier;
import com.promoit.shortener.infra.InMemoryLinkRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {
    @Test
    void fullFlow() {
        var repo = new InMemoryLinkRepository();
        var svc = new LinkService(repo, new ShortCodeGenerator(7),
                new ConsoleNotifier(), Duration.ofSeconds(60), Clock.systemUTC());

        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        var a = svc.create(u1, "https://www.baeldung.com/java-9-http-client", 2);
        var b = svc.create(u2, "https://www.baeldung.com/java-9-http-client", 5);

        assertNotEquals(a.getShortCode(), b.getShortCode());

        svc.open(u1, a.getShortCode());
        svc.open(u1, a.getShortCode());

        assertThrows(IllegalStateException.class, () -> svc.open(u1, a.getShortCode()));
        assertThrows(SecurityException.class, () -> svc.delete(u2, a.getShortCode()));
    }
}
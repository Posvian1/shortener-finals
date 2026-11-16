package com.promoit.shortener.core.service;

import com.promoit.shortener.core.model.ShortLink;
import com.promoit.shortener.core.port.LinkRepository;
import com.promoit.shortener.core.port.Notifier;
import com.promoit.shortener.core.util.ShortCodeGenerator;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class LinkService {
    private final LinkRepository repo;
    private final ShortCodeGenerator generator;
    private final Notifier notifier;
    private final Duration ttl;
    private final Clock clock;

    public LinkService(LinkRepository repo, ShortCodeGenerator generator,
                       Notifier notifier, Duration ttl, Clock clock) {
        this.repo = repo;
        this.generator = generator;
        this.notifier = notifier;
        this.ttl = ttl;
        this.clock = clock;
    }

    public ShortLink create(UUID ownerId, String url, int maxClicks) {
        validateUrl(url);
        String code = generator.generate(repo);
        ShortLink link = new ShortLink(code, url, ownerId, Instant.now(clock), ttl, maxClicks);
        repo.save(link);
        return link;
    }

    public String open(UUID userId, String code) {
        ShortLink link = repo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена: " + code));

        if (!link.getOwnerId().equals(userId)) {
            throw new SecurityException("Ссылка принадлежит другому пользователю");
        }
        Instant now = Instant.now(clock);

        if (link.isExpired(now)) {
            repo.delete(code);
            String msg = "Ссылка " + code + " истекла и была удалена";
            notifier.notify(userId, msg);
            throw new IllegalStateException(msg);
        }
        if (link.isExhausted()) {
            String msg = "Ссылка " + code + ": лимит переходов исчерпан";
            notifier.notify(userId, msg);
            throw new IllegalStateException(msg);
        }

        link.registerClick();
        if (link.isExhausted()) {
            notifier.notify(userId,
                    "Ссылка " + code + ": достигнут лимит переходов (" + link.getMaxClicks() + ")");
        }
        return link.getOriginalUrl();
    }

    public List<ShortLink> listOwn(UUID userId) {
        return repo.findAll().stream()
                .filter(l -> l.getOwnerId().equals(userId))
                .toList();
    }

    public void editMaxClicks(UUID userId, String code, int newMax) {
        ShortLink link = getOwned(userId, code);
        link.setMaxClicks(newMax);
        repo.save(link);
    }

    public void delete(UUID userId, String code) {
        getOwned(userId, code);
        repo.delete(code);
    }

    public void cleanupExpired() {
        Instant now = Instant.now(clock);
        for (ShortLink link : repo.findAll()) {
            if (link.isExpired(now)) {
                repo.delete(link.getShortCode());
                notifier.notify(link.getOwnerId(),
                        "Ссылка " + link.getShortCode() + " удалена по истечении TTL");
            }
        }
    }

    private ShortLink getOwned(UUID userId, String code) {
        ShortLink link = repo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена"));
        if (!link.getOwnerId().equals(userId)) {
            throw new SecurityException("Нет прав на изменение этой ссылки");
        }
        return link;
    }

    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!uri.isAbsolute() || scheme == null
                    || !(scheme.equals("http") || scheme.equals("https"))) {
                throw new IllegalArgumentException("Разрешены только абсолютные http(s) URL");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Некорректный URL: " + url);
        }
    }
}
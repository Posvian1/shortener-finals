package com.promoit.shortener.core.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class ShortLink {
    private final String shortCode;
    private final String originalUrl;
    private final UUID ownerId;
    private final Instant createdAt;
    private final Duration ttl;
    private int maxClicks;
    private int currentClicks;

    public ShortLink(String shortCode, String originalUrl, UUID ownerId,
                     Instant createdAt, Duration ttl, int maxClicks) {
        if (maxClicks <= 0) throw new IllegalArgumentException("maxClicks must be positive");
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.ttl = ttl;
        this.maxClicks = maxClicks;
        this.currentClicks = 0;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(createdAt.plus(ttl));
    }

    public boolean isExhausted() {
        return currentClicks >= maxClicks;
    }

    public boolean isActive(Instant now) {
        return !isExpired(now) && !isExhausted();
    }

    public void registerClick() {
        if (isExhausted()) throw new IllegalStateException("Link limit already reached");
        currentClicks++;
    }

    public void setMaxClicks(int maxClicks) {
        if (maxClicks <= 0) throw new IllegalArgumentException("maxClicks must be positive");
        this.maxClicks = maxClicks;
    }

    public String getShortCode()  { return shortCode; }
    public String getOriginalUrl(){ return originalUrl; }
    public UUID   getOwnerId()    { return ownerId; }
    public Instant getCreatedAt() { return createdAt; }
    public Duration getTtl()      { return ttl; }
    public int    getMaxClicks()  { return maxClicks; }
    public int    getCurrentClicks(){ return currentClicks; }
}
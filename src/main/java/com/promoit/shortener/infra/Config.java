package com.promoit.shortener.infra;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public final class Config {
    private final Duration linkTtl;
    private final int defaultMaxClicks;
    private final int shortCodeLength;
    private final String baseUrl;
    private final long cleanupIntervalSeconds;

    private Config(Duration linkTtl, int defaultMaxClicks, int shortCodeLength,
                   String baseUrl, long cleanupIntervalSeconds) {
        this.linkTtl = linkTtl;
        this.defaultMaxClicks = defaultMaxClicks;
        this.shortCodeLength = shortCodeLength;
        this.baseUrl = baseUrl;
        this.cleanupIntervalSeconds = cleanupIntervalSeconds;
    }

    public static Config load() {
        Properties props = new Properties();
        try (InputStream in = Config.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) throw new IllegalStateException("application.properties not found");
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка чтения конфига", e);
        }
        return new Config(
                Duration.ofSeconds(Long.parseLong(
                        props.getProperty("link.ttl.seconds", "86400"))),
                Integer.parseInt(props.getProperty("link.default.max.clicks", "100")),
                Integer.parseInt(props.getProperty("link.shortcode.length", "7")),
                props.getProperty("link.base.url", "clck.ru/"),
                Long.parseLong(props.getProperty("cleanup.interval.seconds", "60"))
        );
    }

    public Duration getLinkTtl() { return linkTtl; }
    public int getDefaultMaxClicks() { return defaultMaxClicks; }
    public int getShortCodeLength() { return shortCodeLength; }
    public String getBaseUrl() { return baseUrl; }
    public long getCleanupIntervalSeconds() { return cleanupIntervalSeconds; }
}
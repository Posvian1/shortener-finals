package com.promoit.shortener;

import com.promoit.shortener.core.port.LinkRepository;
import com.promoit.shortener.core.port.Notifier;
import com.promoit.shortener.core.service.LinkService;
import com.promoit.shortener.core.service.UserService;
import com.promoit.shortener.core.util.ShortCodeGenerator;
import com.promoit.shortener.infra.Config;
import com.promoit.shortener.infra.ConsoleNotifier;
import com.promoit.shortener.infra.InMemoryLinkRepository;
import com.promoit.shortener.ui.Cli;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Принудительно устанавливаем UTF-8 для консоли, чтобы кириллица не превращалась в ????
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        Config config = Config.load();

        LinkRepository repo = new InMemoryLinkRepository();
        Notifier notifier = new ConsoleNotifier();
        ShortCodeGenerator generator = new ShortCodeGenerator(config.getShortCodeLength());

        LinkService linkService = new LinkService(
                repo, generator, notifier, config.getLinkTtl(), Clock.systemDefaultZone());

        UserService userService = new UserService(
                Path.of(System.getProperty("user.home"), ".shortener", "user.id"));

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ttl-cleaner");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(linkService::cleanupExpired,
                config.getCleanupIntervalSeconds(),
                config.getCleanupIntervalSeconds(),
                TimeUnit.SECONDS);

        try {
            new Cli(linkService, userService, notifier, config).run();
        } finally {
            scheduler.shutdownNow();
        }
    }
}
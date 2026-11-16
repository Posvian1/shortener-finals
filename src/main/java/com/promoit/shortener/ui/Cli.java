package com.promoit.shortener.ui;

import com.promoit.shortener.core.model.ShortLink;
import com.promoit.shortener.core.port.Notifier;
import com.promoit.shortener.core.service.LinkService;
import com.promoit.shortener.core.service.UserService;
import com.promoit.shortener.infra.Config;

import java.awt.Desktop;
import java.net.URI;
import java.util.Scanner;
import java.util.UUID;

public class Cli {
    private final LinkService linkService;
    private final UserService userService;
    private final Notifier notifier;
    private final Config config;
    private final Scanner scanner = new Scanner(System.in);

    public Cli(LinkService linkService, UserService userService,
               Notifier notifier, Config config) {
        this.linkService = linkService;
        this.userService = userService;
        this.notifier = notifier;
        this.config = config;
    }

    public void run() {
        UUID userId = userService.getOrCreate();
        System.out.println("=== URL Shortener ===");
        System.out.println("Your UUID: " + userId);
        printHelp();

        while (true) {
            notifier.drain(userId).forEach(m -> System.out.println("[NOTIFY] " + m));
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+", 3);
            String cmd = parts[0].toLowerCase();
            try {
                switch (cmd) {
                    case "help", "?" -> printHelp();
                    case "create" -> handleCreate(userId, parts);
                    case "list"   -> handleList(userId);
                    case "open"   -> handleOpen(userId, parts);
                    case "status" -> handleStatus(userId, parts);
                    case "edit"   -> handleEdit(userId, parts);
                    case "delete" -> handleDelete(userId, parts);
                    case "exit", "quit" -> { return; }
                    default -> System.out.println("Unknown command. Type 'help'.");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
                System.out.println("[ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private void handleCreate(UUID userId, String[] parts) {
        if (parts.length < 2) { System.out.println("Usage: create <url> [maxClicks]"); return; }
        int max = parts.length == 3 ? Integer.parseInt(parts[2]) : config.getDefaultMaxClicks();
        ShortLink link = linkService.create(userId, parts[1], max);
        System.out.println("Created: " + config.getBaseUrl() + link.getShortCode());
        System.out.println("  target: " + link.getOriginalUrl());
        System.out.println("  max:    " + link.getMaxClicks());
        System.out.println("  TTL:    " + link.getTtl().toSeconds() + "s");
    }

    private void handleList(UUID userId) {
        var links = linkService.listOwn(userId);
        if (links.isEmpty()) { System.out.println("No links yet."); return; }
        System.out.println("Your links:");
        for (ShortLink l : links) {
            System.out.printf("  %s%s  %d/%d  %s%n",
                    config.getBaseUrl(), l.getShortCode(),
                    l.getCurrentClicks(), l.getMaxClicks(), l.getOriginalUrl());
        }
    }

    private void handleOpen(UUID userId, String[] parts) {
        if (parts.length < 2) { System.out.println("Usage: open <code|url>"); return; }
        String code = extractCode(parts[1]);
        String url = linkService.open(userId, code);
        System.out.println("Redirect: " + url);
        openInBrowser(url);
    }

    private void handleStatus(UUID userId, String[] parts) {
        if (parts.length < 2) { System.out.println("Usage: status <code>"); return; }
        String code = extractCode(parts[1]);
        linkService.listOwn(userId).stream()
                .filter(l -> l.getShortCode().equals(code))
                .findFirst()
                .ifPresentOrElse(
                        l -> System.out.printf("%s: %d/%d clicks, TTL %ds%n",
                                code, l.getCurrentClicks(), l.getMaxClicks(),
                                l.getTtl().toSeconds()),
                        () -> System.out.println("Not found or not yours")
                );
    }

    private void handleEdit(UUID userId, String[] parts) {
        if (parts.length < 3) { System.out.println("Usage: edit <code> <maxClicks>"); return; }
        int newMax = Integer.parseInt(parts[2]);
        linkService.editMaxClicks(userId, extractCode(parts[1]), newMax);
        System.out.println("Limit updated: " + newMax);
    }

    private void handleDelete(UUID userId, String[] parts) {
        if (parts.length < 2) { System.out.println("Usage: delete <code>"); return; }
        linkService.delete(userId, extractCode(parts[1]));
        System.out.println("Deleted.");
    }

    private String extractCode(String input) {
        String s = input.endsWith("/") ? input.substring(0, input.length() - 1) : input;
        int slash = s.lastIndexOf('/');
        return slash >= 0 ? s.substring(slash + 1) : s;
    }

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("(browser unavailable - URL above)");
            }
        } catch (Exception e) {
            System.out.println("(cannot open browser: " + e.getMessage() + ")");
        }
    }

    private void printHelp() {
        System.out.println("""
                Commands:
                  create <url> [maxClicks]   create short link
                  list                       list my links
                  open <code|url>            open link (browser)
                  status <code>              show status
                  edit <code> <maxClicks>    change click limit
                  delete <code>              delete link
                  help                       this help
                  exit                       quit
                """);
    }
}
package com.promoit.shortener.infra;

import com.promoit.shortener.core.port.Notifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConsoleNotifier implements Notifier {
    private final Map<UUID, List<String>> pending = new ConcurrentHashMap<>();

    @Override
    public void notify(UUID userId, String message) {
        pending.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    }

    @Override
    public List<String> drain(UUID userId) {
        List<String> list = pending.remove(userId);
        return list == null ? List.of() : list;
    }
}
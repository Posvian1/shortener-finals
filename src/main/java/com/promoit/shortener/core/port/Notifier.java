package com.promoit.shortener.core.port;

import java.util.List;
import java.util.UUID;

public interface Notifier {
    void notify(UUID userId, String message);
    List<String> drain(UUID userId);
}
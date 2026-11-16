package com.promoit.shortener.core.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class UserService {
    private final Path userFile;

    public UserService(Path userFile) {
        this.userFile = userFile;
    }

    public UUID getOrCreate() {
        try {
            if (Files.exists(userFile)) {
                String content = Files.readString(userFile).trim();
                if (!content.isEmpty()) return UUID.fromString(content);
            }
            UUID id = UUID.randomUUID();
            if (userFile.getParent() != null) {
                Files.createDirectories(userFile.getParent());
            }
            Files.writeString(userFile, id.toString());
            return id;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
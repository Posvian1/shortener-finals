package com.promoit.shortener;

import com.promoit.shortener.core.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    @Test
    void createsAndReusesUuid(@TempDir Path tmp) {
        var svc = new UserService(tmp.resolve("user.id"));
        UUID a = svc.getOrCreate();
        UUID b = svc.getOrCreate();
        assertEquals(a, b);
    }
}
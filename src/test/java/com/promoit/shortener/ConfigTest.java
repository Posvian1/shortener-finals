package com.promoit.shortener;

import com.promoit.shortener.infra.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @Test
    void loadsDefaults() {
        var c = Config.load();
        assertNotNull(c.getLinkTtl());
        assertTrue(c.getDefaultMaxClicks() > 0);
        assertTrue(c.getShortCodeLength() >= 4);
        assertNotNull(c.getBaseUrl());
    }
}
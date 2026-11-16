package com.promoit.shortener.core.port;

import com.promoit.shortener.core.model.ShortLink;

import java.util.Collection;
import java.util.Optional;

public interface LinkRepository {
    void save(ShortLink link);
    Optional<ShortLink> findByCode(String shortCode);
    void delete(String shortCode);
    boolean existsByCode(String shortCode);
    Collection<ShortLink> findAll();
}
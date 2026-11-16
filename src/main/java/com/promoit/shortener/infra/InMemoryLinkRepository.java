package com.promoit.shortener.infra;

import com.promoit.shortener.core.model.ShortLink;
import com.promoit.shortener.core.port.LinkRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryLinkRepository implements LinkRepository {
    private final ConcurrentMap<String, ShortLink> storage = new ConcurrentHashMap<>();

    @Override public void save(ShortLink link) { storage.put(link.getShortCode(), link); }
    @Override public Optional<ShortLink> findByCode(String code) { return Optional.ofNullable(storage.get(code)); }
    @Override public void delete(String code) { storage.remove(code); }
    @Override public boolean existsByCode(String code) { return storage.containsKey(code); }
    @Override public Collection<ShortLink> findAll() { return storage.values(); }
}
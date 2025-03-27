package com.sas.saveandsound.cashe;

import com.sas.saveandsound.model.Sound;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SoundCache extends LinkedHashMap<String, List<Sound>> {
    private final int maxSize;

    public SoundCache(int maxSize) {
        super(16, 0.75f, true); // true для порядка доступа (LRU)
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, List<Sound>> eldest) {
        return size() > maxSize; // Удаляем самый старый элемент, если превышен размер
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoundCache other)) return false;
        return maxSize == other.maxSize && super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

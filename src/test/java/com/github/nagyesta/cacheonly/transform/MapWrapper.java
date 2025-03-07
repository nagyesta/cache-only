package com.github.nagyesta.cacheonly.transform;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@Setter
@Getter
public final class MapWrapper<K, V> {

    private Map<K, V> map;

    public MapWrapper(final Map<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final MapWrapper<?, ?> that)) {
            return false;
        }
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MapWrapper.class.getSimpleName() + "[", "]")
                .add("map=" + map)
                .toString();
    }
}

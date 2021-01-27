package com.github.nagyesta.cacheonly.transform;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public final class MapWrapper<K, V> {

    private Map<K, V> map;

    public MapWrapper(final Map<K, V> map) {
        this.map = map;
    }

    public Map<K, V> getMap() {
        return map;
    }

    public void setMap(final Map<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapWrapper)) {
            return false;
        }
        final MapWrapper<?, ?> that = (MapWrapper<?, ?>) o;
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

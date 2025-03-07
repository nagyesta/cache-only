package com.github.nagyesta.cacheonly.transform;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;

@Setter
@Getter
public final class CollectionWrapper<E> {

    private Collection<E> collection;

    public CollectionWrapper(final Collection<E> collection) {
        this.collection = collection;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final CollectionWrapper<?> that)) {
            return false;
        }
        return CollectionUtils.containsAll(this.collection, that.collection)
                && CollectionUtils.containsAll(that.collection, this.collection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CollectionWrapper.class.getSimpleName() + "[", "]")
                .add("collection=" + collection)
                .toString();
    }
}

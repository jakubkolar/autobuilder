package com.github.jakubkolar.autobuilder.impl;

import com.github.jakubkolar.autobuilder.spi.ValueResolver;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

class ExactTypeConstantResolver<T> implements ValueResolver {

    private final Class<T> type;
    @Nullable
    private final T value;

    public ExactTypeConstantResolver(Class<T> type, @Nullable T value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public <R> R resolve(Class<R> type, Optional<Type> typeInfo, String name, Collection<Annotation> annotations) {
        // This is not a "greedy" resolver and resolves just the given type
        // It would be confusing for the user if they requested e.g. 'AtomicInteger'
        // to be resolved as 'this.value', and this resolver would resolve e.g. a field
        // of type 'Number' with a given atomic integer
        if (Objects.equals(type, this.type)) {
            return type.cast(value);
        }

        throw new UnsupportedOperationException(this + " cannot resolve type " + type.getSimpleName());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("type", type)
                .append("value", value)
                .toString();
    }
}

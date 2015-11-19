/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Jakub Kolar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.jakubkolar.autobuilder.impl;

import com.github.jakubkolar.autobuilder.spi.ValueResolver;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

class NamedResolver implements ValueResolver {

    @SuppressWarnings("rawtypes")
    private final ImmutableMap<ImmutablePair<String, Class>, RegisteredValue> namedValues;

    public NamedResolver() {
        this.namedValues = ImmutableMap.of();
    }

    @SuppressWarnings("rawtypes")
    private NamedResolver(Map<ImmutablePair<String, Class>, RegisteredValue> oldValues, ImmutablePair<String, Class> newKey, RegisteredValue newValue) {
        this.namedValues = ImmutableMap.<ImmutablePair<String, Class>, RegisteredValue>builder()
                .putAll(oldValues)
                .put(newKey, newValue)
                .build();
    }

    @Nullable
    @Override
    public <T> T resolve(Class<T> type, String name, Collection<Annotation> annotations) {
        @SuppressWarnings("rawtypes")
        RegisteredValue rv = namedValues.get(ImmutablePair.of(name, (Class) type));

        if (rv == null) {
            throw new UnsupportedOperationException(String.format(
                    "There is no registered named value with name %s and type %s", name, type.getSimpleName()));
        }

        for (Annotation requiredAnnotation : rv.annotations) {
            if (!annotations.contains(requiredAnnotation)) {
                throw new UnsupportedOperationException(String.format(
                        "The named value with name %s and type %s requires annotations %s, " +
                                "but only these annotations were present: %s",
                        name, type.getSimpleName(), requiredAnnotation, annotations));
            }
        }

        try {
            return type.cast(rv.value);
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException(String.format(
                    "Named value %s cannot be converted to the required type %s because of: %s",
                    name, type.getSimpleName(), e.getMessage()), e);
        }
    }

    public NamedResolver add(String name, @Nullable Object value, Collection<Annotation> requiredAnnotations) {
        return new NamedResolver(namedValues,
                ImmutablePair.of(name, value != null ? value.getClass() : null),
                new RegisteredValue(value, requiredAnnotations));
    }

    public NamedResolver add(String name, @Nullable Object value, Annotation... requiredAnnotations) {
        return add(name, value, Arrays.asList(requiredAnnotations));
    }

    public NamedResolver addAll(Map<String, Object> addedValues) {
        // TODO: this can be optimized
        NamedResolver result = this;
        for (Entry<String, Object> entry : addedValues.entrySet()) {
            result = add(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static class RegisteredValue {
        @Nullable
        Object value;
        Collection<Annotation> annotations;

        RegisteredValue(@Nullable Object value, Collection<Annotation> annotations) {
            this.value = value;
            this.annotations = annotations;
        }
    }
}

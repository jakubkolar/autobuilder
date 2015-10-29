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
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class NamedResolver implements ValueResolver {

    private final Map<ImmutablePair<String, Class>, RegisteredValue> namedValues;

    public NamedResolver() {
        this.namedValues = new HashMap<>();
    }

    @Nullable
    @Override
    public <T> T resolve(Class<T> type, String name, Collection<Annotation> annotations) {
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

    public NamedResolver add(String name, Object value, Collection<Annotation> requiredAnnotations) {
        this.namedValues.put(ImmutablePair.of(name, value.getClass()), new RegisteredValue(value, requiredAnnotations));
        return this;
    }

    public NamedResolver add(String name, Object value, Annotation... requiredAnnotations) {
        return this.add(name, value, Arrays.asList(requiredAnnotations));
    }

    public NamedResolver addAll(Map<String, Object> addedValues) {
        for (Map.Entry<String, Object> entry : addedValues.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    private static class RegisteredValue {
        Object value;
        Collection<Annotation> annotations;

        RegisteredValue(Object value, Collection<Annotation> annotations) {
            this.value = value;
            this.annotations = annotations;
        }
    }
}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Jakub Kolar
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
import com.google.common.primitives.Primitives;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

class NamedResolver implements ValueResolver {

    /*
     * TODO AB-020:
     * global named resolvers register values under their name and type, that  is you can
     * register more resolvers for different types under the same name... this is probably
     * confusing for a local resolver within a builder instance, because each name (field)
     * has unique type, but if you accidentally register a value of a wrong type, it will
     * not be found and you won't get any error... instead it should refuse the
     * registration with "wrong type for field"
     *
     * Followup:
     * The concept of AutoBuilder.registerValue(name, value) should be revisited...
     * as it is implemented now it requires a class name and then a path in the obj.
     * graph separated by dots. From this I do not see why would someone register the
     * same name with different types, that would only be confusing.
     */

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
    public <T> T resolve(Class<T> type, Optional<Type> typeInfo, String name, Collection<Annotation> annotations) {
        // If type is primitive like int.class, we must use its wrapper type
        // because the wrapper type is used as a part of the key in the namedValues
        // and also because int.class cannot be used in the end to 'unbox' the result
        // (same in BuiltInResolvers.primitiveTypeResolver, you would get ClassCastException)
        Class<T> wrappedType = Primitives.wrap(type);

        @SuppressWarnings("rawtypes")
        RegisteredValue rv = namedValues.get(ImmutablePair.of(name, (Class) wrappedType));

        if (rv == null) {
            // TODO: try to lookup null, which this way applies to _any_ type
            rv = namedValues.get(ImmutablePair.of(name, (Class)null));
            if (rv == null) {
                throw new UnsupportedOperationException(String.format(
                    "There is no registered named value with name %s and type %s", name, type.getSimpleName()));
            }
        }

        for (Annotation requiredAnnotation : rv.getAnnotations()) {
            if (!annotations.contains(requiredAnnotation)) {
                throw new UnsupportedOperationException(String.format(
                    "The named value with name %s and type %s requires annotations %s, " +
                    "but only these annotations were present: %s",
                    name, type.getSimpleName(), requiredAnnotation, annotations));
            }
        }

        try {
            return wrappedType.cast(rv.getValue());
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException(String.format(
                "Named value %s cannot be converted to the required type %s because of: %s",
                name, type.getSimpleName(), e.getMessage()), e);
        }
    }

    public NamedResolver add(String name, @Nullable Object value, Collection<Annotation> requiredAnnotations) {
        if (value == null) {
            // TODO: this may be a problem since for null there is no Class, so how to look it up?
            return new NamedResolver(namedValues,
                    ImmutablePair.of(name, null), new RegisteredValue(null, requiredAnnotations));
        } else {
            return contributeValue(name, value.getClass(), value, requiredAnnotations);
        }
    }

    private NamedResolver contributeValue(String name, @Nullable Class<?> type, Object value, Collection<Annotation> requiredAnnotations) {
        if (type == null || namedValues.containsKey(ImmutablePair.of(name, (Class)type))) {
            return this;
        }

        NamedResolver result = new NamedResolver(namedValues,
                ImmutablePair.of(name, type),
                new RegisteredValue(value, requiredAnnotations));

        result = result.contributeValue(name, type.getSuperclass(), value, requiredAnnotations);

        for (Class<?> iface : type.getInterfaces()) {
            result = result.contributeValue(name, iface, value, requiredAnnotations);
        }

        return result;
    }

    public NamedResolver add(String name, @Nullable Object value, Annotation... requiredAnnotations) {
        return add(name, value, Arrays.asList(requiredAnnotations));
    }

    private static class RegisteredValue {
        @Nullable
        private final Object value;
        private final Collection<Annotation> annotations;

        RegisteredValue(@Nullable Object value, Collection<Annotation> annotations) {
            this.value = value;
            this.annotations = annotations;
        }

        @Nullable
        public Object getValue() {
            return value;
        }

        public Collection<Annotation> getAnnotations() {
            return annotations;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        }
    }
}

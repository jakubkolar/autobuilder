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
import com.google.common.primitives.Primitives;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * TODO: Aggressive / Generic resolvers, should be executed last, given order
 */
class BuiltInResolvers implements ValueResolver {

    @Inject
    public BuiltInResolvers() {
    }

    @Nullable
    @Override
    public <T> T resolve(Class<T> type, String name, Collection<Annotation> annotations) {
        return resolveWith(type, name,
                BuiltInResolvers::stringResolver,
                BuiltInResolvers::primitiveTypeResolver,
                BuiltInResolvers::enumResolver,
                BuiltInResolvers::collectionResolver,
                BuiltInResolvers::arrayResolver
        );
    }

    @SafeVarargs
    private static <T> T resolveWith(Class<T> type, String name, BiFunction<Class<T>, String, T>... functions) {
        for (BiFunction<Class<T>, String, T> resolver : functions) {
            T result = resolver.apply(type, name);

            // For Enums with no constants we accept null as the only valid value
            if (result != null || type.isEnum()) {
                return result;
            }
        }

        throw new UnsupportedOperationException("Built-in resolvers cannot resolve type " + type.getSimpleName());
    }

    private static <T> T stringResolver(Class<T> type, String name) {
        return type.isAssignableFrom(String.class) ? type.cast(name) : null;
    }

    private static <T> T primitiveTypeResolver(Class<T> type, String name) {
        if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
            return Primitives.wrap(type).cast(Integer.MIN_VALUE);
        }
        else if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
            return Primitives.wrap(type).cast(Long.MIN_VALUE);
        }
        else if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
            return Primitives.wrap(type).cast((float) Double.NaN);
        }
        else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
            return Primitives.wrap(type).cast(Double.NaN);
        }
        else if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
            return Primitives.wrap(type).cast(Byte.MIN_VALUE);
        }
        else if (type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
            return Primitives.wrap(type).cast(Short.MIN_VALUE);
        }
        else if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
            return Primitives.wrap(type).cast(false);
        }
        else if (type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class)) {
            return Primitives.wrap(type).cast(Character.MIN_VALUE);
        }

        return null;
    }

    private static <T> T enumResolver(Class<T> type, String name) {
        if (type.isEnum() && type.getEnumConstants().length > 0) {
            return type.getEnumConstants()[0];
        }

        return null;
    }

    private static <T> T collectionResolver(Class<T> type, String name) {
        if (type.isAssignableFrom(List.class)) {
            return type.cast(Collections.emptyList());
        }
        else if (type.isAssignableFrom(Set.class)) {
            return type.cast(Collections.emptySet());
        }
        else if (type.isAssignableFrom(Map.class)) {
            return type.cast(Collections.emptyMap());
        }

        return null;
    }

    private static <T> T arrayResolver(Class<T> type, String name) {
        if (type.isArray()) {
            return type.cast(Array.newInstance(type.getComponentType(), 0));
        }

        return null;
    }
}

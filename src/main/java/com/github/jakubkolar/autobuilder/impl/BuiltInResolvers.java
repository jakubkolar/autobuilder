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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * TODO: Aggressive / Generic resolvers, should be executed last, given order
 */
class BuiltInResolvers implements ValueResolver {

    @FunctionalInterface
    public interface ResolveFunc<T> {
        T apply(Class<T> type, Optional<Type> typeInfo, String name);
    }

    @Inject
    public BuiltInResolvers() {
    }

    @Nullable
    @Override
    public <T> T resolve(Class<T> type, Optional<Type> typeInfo, String name, Collection<Annotation> annotations) {
        return resolveWith(type, typeInfo, name,
                BuiltInResolvers::stringResolver,
                BuiltInResolvers::primitiveTypeResolver,
                BuiltInResolvers::enumResolver,
                BuiltInResolvers::collectionResolver,
                BuiltInResolvers::arrayResolver
        );
    }

    @Nullable
    @SafeVarargs
    private static <T> T resolveWith(Class<T> type, Optional<Type> typeInfo, String name, ResolveFunc<T>... functions) {
        for (ResolveFunc<T> resolver : functions) {
            T result = resolver.apply(type, typeInfo, name);

            // For Enums with no constants we accept null as the only valid value
            if (result != null || type.isEnum()) {
                return result;
            }
        }

        throw new UnsupportedOperationException("Built-in resolvers cannot resolve type " + type.getSimpleName());
    }

    private static boolean isSafeAssignable(Class<?> from, Class<?> to, Optional<Type> toTypeInfo) {
        // We may be really resolving an Object, but we may also be
        // resolving a field of type T of a generic class,
        // for which we got to == Object.class
        if (to.isAssignableFrom(Object.class)) {
            // If we know nothing more, then be better safe and reject to resolve
            if (!toTypeInfo.isPresent()) {
                return false;
            }

            // Raw type? => we are really trying to resolve an Object
            // Anything else => better reject to resolve to avoid ClassCastException
            return toTypeInfo.get() instanceof Class;
        }
        else if (to.isAssignableFrom(Comparable.class)) {
            // Make sure we are really resolving a Comparable<T>, not
            // something crazy like 'T extends Comparable<? super Comparable<Integer>>'
            if (!toTypeInfo.isPresent()
                    || !(toTypeInfo.get() instanceof ParameterizedType)
                    || ((ParameterizedType) toTypeInfo.get()).getActualTypeArguments().length != 1) {
                return false;
            }

            // What is the 'T' in Comparable<T> ?
            Type typeArgument = ((ParameterizedType) toTypeInfo.get()).getActualTypeArguments()[0];

            // We only allow the resolution if typeArgument is raw type and is the same
            // as the raw type of the resolved object; that is we allow e.g. String to be
            // resolved for Comparable<String>, but not for Comparable<Integer>
            return Objects.equals(typeArgument, from);
        } else {
            return to.isAssignableFrom(from);
        }
    }

    @Nullable
    private static <T> T stringResolver(Class<T> type, Optional<Type> typeInfo, String name) {
        return isSafeAssignable(String.class, type, typeInfo) ? type.cast("any_" + name) : null;
    }

    @Nullable
    private static <T> T primitiveTypeResolver(Class<T> type, Optional<Type> typeInfo, String name) {
        if (isSafeAssignable(Integer.class, type, typeInfo) || type.isAssignableFrom(int.class)) {
            return Primitives.wrap(type).cast(Integer.MIN_VALUE);
        }
        else if (isSafeAssignable(Long.class, type, typeInfo) || type.isAssignableFrom(long.class)) {
            return Primitives.wrap(type).cast(Long.MIN_VALUE);
        }
        else if (isSafeAssignable(Float.class, type, typeInfo) || type.isAssignableFrom(float.class)) {
            return Primitives.wrap(type).cast(Float.NaN);
        }
        else if (isSafeAssignable(Double.class, type, typeInfo) || type.isAssignableFrom(double.class)) {
            return Primitives.wrap(type).cast(Double.NaN);
        }
        else if (isSafeAssignable(Byte.class, type, typeInfo) || type.isAssignableFrom(byte.class)) {
            return Primitives.wrap(type).cast(Byte.MIN_VALUE);
        }
        else if (isSafeAssignable(Short.class, type, typeInfo) || type.isAssignableFrom(short.class)) {
            return Primitives.wrap(type).cast(Short.MIN_VALUE);
        }
        else if (isSafeAssignable(Boolean.class, type, typeInfo) || type.isAssignableFrom(boolean.class)) {
            return Primitives.wrap(type).cast(false);
        }
        else if (isSafeAssignable(Character.class, type, typeInfo) || type.isAssignableFrom(char.class)) {
            return Primitives.wrap(type).cast(Character.MIN_VALUE);
        }

        return null;
    }

    @Nullable
    private static <T> T enumResolver(Class<T> type, Optional<Type> typeInfo, String name) {
        if (type.isEnum() && type.getEnumConstants().length > 0) {
            return type.getEnumConstants()[0];
        }

        return null;
    }

    @Nullable
    private static <T> T collectionResolver(Class<T> type, Optional<Type> typeInfo, String name) {
        if (isSafeAssignable(List.class, type, typeInfo)) {
            return type.cast(Collections.emptyList());
        }
        else if (isSafeAssignable(Set.class, type, typeInfo)) {
            return type.cast(Collections.emptySet());
        }
        else if (isSafeAssignable(Map.class, type, typeInfo)) {
            return type.cast(Collections.emptyMap());
        }

        return null;
    }

    @Nullable
    private static <T> T arrayResolver(Class<T> type, Optional<Type> typeInfo, String name) {
        if (type.isArray()) {
            return type.cast(Array.newInstance(type.getComponentType(), 0));
        }

        return null;
    }
}

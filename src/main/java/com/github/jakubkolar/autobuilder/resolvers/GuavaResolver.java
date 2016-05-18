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

package com.github.jakubkolar.autobuilder.resolvers;

import com.github.jakubkolar.autobuilder.spi.ValueResolver;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolver for common types from the <a href="https://github.com/google/guava">Google
 * Guava</a> library.
 *
 * <p> Currently supported types are:
 * <ul>
 *     <li><a href="http://google.github.io/guava/releases/19.0/api/docs/com/google/common/base/Optional.html">Optional</a></li>
 *     <li><a href="http://google.github.io/guava/releases/19.0/api/docs/com/google/common/collect/ImmutableCollection.html">ImmutableCollection</a></li>
 *     <li><a href="http://google.github.io/guava/releases/19.0/api/docs/com/google/common/collect/ImmutableMap.html">ImmutableMap</a></li>
 * </ul>
 *
 * <p> TODO AB-013: Support for more types (e.g. Multiset, Multimap)
 *
 * @author Jakub Kolar
 * @since 0.0.1
 */
@AutoService(ValueResolver.class)
public class GuavaResolver implements ValueResolver {

    @Nullable
    @Override
    public <T> T resolve(Class<T> type, Optional<Type> typeInfo, String name, Collection<Annotation> annotations) {
        if (Objects.equals(type, com.google.common.base.Optional.class)) {
            return type.cast(com.google.common.base.Optional.absent());
        }

        if (ImmutableCollection.class.isAssignableFrom(type)) {
            if (type.isAssignableFrom(ImmutableList.class)) {
                return type.cast(ImmutableList.of());
            } else if (type.isAssignableFrom(ImmutableSet.class)) {
                return type.cast(ImmutableSet.of());
            } else if (type.isAssignableFrom(ImmutableSortedSet.class)) {
                return type.cast(ImmutableSortedSet.of());
            }
        }

        if (ImmutableMap.class.isAssignableFrom(type)) {
            if (type.isAssignableFrom(ImmutableMap.class)) {
                return type.cast(ImmutableMap.of());
            } else if (type.isAssignableFrom(ImmutableSortedMap.class)) {
                return type.cast(ImmutableSortedMap.of());
            }
        }

        throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot resolve type " + type.getSimpleName());
    }

}

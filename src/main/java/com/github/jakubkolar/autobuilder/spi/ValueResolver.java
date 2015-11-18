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

package com.github.jakubkolar.autobuilder.spi;

import com.google.common.annotations.Beta;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * An object that can <em>resolve</em> instances of various types based on metadata.
 * <p>
 * Meaning of the metadata and the resolution strategy depends completely on the
 * implementation.
 */
@Beta
public interface ValueResolver {

    /**
     * Resolves an instance of type {@code T}.
     * <p>
     * The implementation can choose whatever strategy is appropriate. For example,
     * it can create a fresh new object, or it can return an instance from a pool,
     * or lookup a named singleton, etc.
     *
     * @param type        the class object for the requested type
     * @param name        the name of the resolved object
     *                    (meaning depends on the context in which the resolution is
     *                    requested, e.g. it may be a field name)
     * @param annotations additional metadata hints for the resolution
     *                    (content depends on the resolution context, e.g. it can be
     *                    annotations found on a field)
     * @param <T>         the type of the result
     * @return the resolved instance of type {@code T}, including {@code null} as a
     * valid return value
     * @throws UnsupportedOperationException if the instance with the given metadata
     *                                       cannot be resolved by this resolver
     */
    @Nullable
    <T> T resolve(Class<T> type, String name, Collection<Annotation> annotations);

}

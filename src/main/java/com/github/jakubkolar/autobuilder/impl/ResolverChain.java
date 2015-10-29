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
import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class ResolverChain implements ValueResolver {

    private final List<ValueResolver> resolvers;

    public ResolverChain(ValueResolver... resolvers) {
        this.resolvers = new ArrayList<>(resolvers.length);
        Collections.addAll(this.resolvers, resolvers);
    }

    @Nullable
    @Override
    public <T> T resolve(Class<T> type, String name, Collection<Annotation> annotations) {
        StringBuilder failedResolvers = new StringBuilder();
        for (ValueResolver resolver : resolvers) {
            try {
                return resolver.resolve(type, name, annotations);
            } catch (UnsupportedOperationException e) {
                // TODO: it is probably better if the messages are just logged as a debug output
                failedResolvers
                        .append("\t")
                        .append(resolver.getClass().getSimpleName())
                        .append(": ")
                        .append(e.getMessage())
                        .append(SystemUtils.LINE_SEPARATOR);
                // Try next resolver
            }
        }

        throw new UnsupportedOperationException("No suitable resolver found: "
                + SystemUtils.LINE_SEPARATOR
                + failedResolvers.toString());
    }

    public ResolverChain add(ValueResolver resolver) {
        this.resolvers.add(resolver);
        return this;
    }

}

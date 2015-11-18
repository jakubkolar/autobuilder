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
import com.github.jakubkolar.autobuilder.api.BuilderDSL;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

class BuilderImpl<T> implements BuilderDSL<T> {

    private final Class<T> type;

    private final NamedResolver localValues;
    private final ResolverChain localResolvers;

    /**
     * RootResolver (Chain):
     - named: local values
     - chain: local resolvers
     - named: global values
     - chain: global resolvers
     - chain: built-in resolvers
     - bean resolver
     */
    private final ValueResolver rootResolver;

    public BuilderImpl(Class<T> type,
                       ValueResolver globalValues,
                       ValueResolver globalResolvers,
                       ValueResolver builtInResolvers,
                       BeanResolverFactory factory) {
        this.type = type;

        // Our local instances
        this.localValues = new NamedResolver();
        this.localResolvers = new ResolverChain();
        BeanResolver beanResolver = factory.create();

        // This is the root resolver chain
        this.rootResolver = new ResolverChain(
                localValues,
                localResolvers,
                globalValues,
                globalResolvers,
                builtInResolvers,
                beanResolver);

        // This will allow for a recursive object graph resolution
        // TODO: cycles in the object graph will lead to stack overflow
        beanResolver.setFieldsResolver(this.rootResolver);
    }

    private BuilderImpl(Class<T> type, NamedResolver localValues, ResolverChain localResolvers, ValueResolver rootResolver) {
        this.type = type;
        this.localValues = localValues;
        this.localResolvers = localResolvers;
        this.rootResolver = rootResolver;
    }

    @Override
    public BuilderDSL<T> with(String property, Object value) {
        return new BuilderImpl<>(type,
                localValues.add(type.getSimpleName() + '.' + property, value),
                localResolvers,
                rootResolver);
    }

    @Override
    public BuilderDSL<T> with(Map<String, Object> properties) {
        // TODO: this can be optimized
        BuilderDSL<T> result = this;
        for (Entry<String, Object> prop : properties.entrySet()) {
            result = with(prop.getKey(), prop.getValue());
        }
        return result;
    }

    @Override
    public BuilderDSL<T> with(ValueResolver userResolver) {
        return new BuilderImpl<>(type,
                localValues,
                localResolvers.add(userResolver),
                rootResolver);
    }

    @Override
    public T build() {
        return rootResolver.resolve(type, type.getSimpleName(), Arrays.asList(type.getAnnotations()));
    }
}

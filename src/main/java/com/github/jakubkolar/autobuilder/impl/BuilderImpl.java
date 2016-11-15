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

import com.github.jakubkolar.autobuilder.api.BuilderDSL;
import com.github.jakubkolar.autobuilder.spi.ValueResolver;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;

class BuilderImpl<T> implements BuilderDSL<T> {

    private final Class<T> type;
    private final NamedResolver localValues;
    private final ResolverChain localChain;
    private final ResolverChain globalChain;
    private final BeanResolverFactory factory;

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

    public BuilderImpl(Class<T> type, NamedResolver localValues, ResolverChain localChain, ResolverChain globalChain, BeanResolverFactory factory) {
        this.type = type;
        this.localValues = localValues;
        this.localChain = localChain;
        this.globalChain = globalChain;
        this.factory = factory;

        // This is modifiable (dependency cycle), and has to be created for each builder
        // separately
        BeanResolver beanResolver = factory.create();

        // This is the root resolver chain - custom to each builder
        this.rootResolver = new ResolverChain(
                localValues,
                localChain,
                globalChain,
                beanResolver);

        // This will allow for a recursive object graph resolution
        // TODO: cycles in the object graph will lead to stack overflow
        beanResolver.setFieldsResolver(this.rootResolver);
    }

    @Override
    public BuilderDSL<T> with(String property, @Nullable Object value) {
        return new BuilderImpl<>(type,
                localValues.add(type.getSimpleName() + '.' + property, value),
                localChain,
                globalChain,
                factory);
    }

    @Override
    public BuilderDSL<T> with(Map<String, Object> properties) {
        // TODO: this can be optimized
        BuilderDSL<T> result = this;
        for (Entry<String, Object> prop : properties.entrySet()) {
            result = result.with(prop.getKey(), prop.getValue());
        }
        return result;
    }

    @Override
    public BuilderDSL<T> with(ValueResolver userResolver) {
        return new BuilderImpl<>(type,
                localValues,
                localChain.add(userResolver),
                globalChain,
                factory);
    }

    @Override
    public <R> BuilderDSL<T> with(Class<R> type, @Nullable R value) {
        return with(new ExactTypeConstantResolver<>(type, value));
    }

    @Nullable
    @Override
    public T build() {
        VRContext<T> context = VRContext.createRoot(rootResolver, type);
        return context.resolve();
    }

}

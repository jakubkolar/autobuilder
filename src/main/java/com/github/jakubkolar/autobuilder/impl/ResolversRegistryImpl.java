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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

@Singleton
class ResolversRegistryImpl implements ResolversRegistry, Initializable {

    private ResolverChain globalResolvers;
    private NamedResolver globalValues;

    @Inject
    public ResolversRegistryImpl() {
        this.globalResolvers = new ResolverChain();
        this.globalValues = new NamedResolver();
    }

    @Override
    public synchronized ResolversRegistry registerValue(String name, Object value, Annotation... requiredAnnotations) {
        globalValues = globalValues.add(name, value, requiredAnnotations);
        return this;
    }

    @Override
    public synchronized ResolversRegistry registerResolver(ValueResolver resolver) {
        globalResolvers = globalResolvers.add(resolver);
        return this;
    }

    @Override
    public synchronized void init() {
        ServiceLoader.load(ValueResolver.class).forEach(resolver -> {
            if (resolver instanceof Initializable) {
                ((Initializable) resolver).init();
            }
            // TODO: intellij somehow thinks this is not in a synchronized context...
            globalResolvers = globalResolvers.add(resolver);
        });
    }

    public synchronized ResolverChain getGlobalResolvers() {
        return globalResolvers;
    }

    public synchronized NamedResolver getGlobalValues() {
        return globalValues;
    }
}

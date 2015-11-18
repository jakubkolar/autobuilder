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

import com.github.jakubkolar.autobuilder.api.BuilderDSL;
import dagger.Module;
import dagger.Provides;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

@Module
public class AutoBuilderModule {

    @Provides
    public BuilderDSLFactory getFactory(
            BeanResolverFactory beanResolverFactory,
            ResolversRegistryImpl registry,
            BuiltInResolvers builtInResolvers) {
        return new BuilderDSLFactory() {
            @Override
            public <T> BuilderDSL<T> create(Class<T> type) {
                // TODO: This is a consequence of synchronized getters that give a totally
                // false sense of thread safety, to be strictly consistent we want to avoid
                // this (would be possible without synchronized):
                // (first) T1: factory.create
                // (during that) T2: registry.registerValue followed by registry.registerResolver,
                // possible result: only registerValue is observed by the resulting builder
                synchronized (registry) {
                    return new BuilderImpl<>(type,
                            registry.getGlobalValues(),
                            registry.getGlobalResolvers(),
                            builtInResolvers,
                            beanResolverFactory);
                }
            }
        };
    }

    @Provides
    public Objenesis getObjenesis() {
        return new ObjenesisStd();
    }

    @Provides(type = Provides.Type.SET)
    public Initializable getInitialization(ResolversRegistryImpl registry) {
        return registry;
    }

    @Provides
    public ResolversRegistry getRegistry(ResolversRegistryImpl registry) {
        return registry;
    }

}

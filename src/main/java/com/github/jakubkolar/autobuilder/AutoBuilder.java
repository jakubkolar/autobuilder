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

package com.github.jakubkolar.autobuilder;

import com.github.jakubkolar.autobuilder.api.BuilderDSL;
import com.github.jakubkolar.autobuilder.impl.BuilderDSLFactory;
import com.github.jakubkolar.autobuilder.api.ResolversRegistry;
import com.github.jakubkolar.autobuilder.impl.AutoBuilderModule;
import com.github.jakubkolar.autobuilder.spi.Initializable;
import com.github.jakubkolar.autobuilder.spi.ValueResolver;
import dagger.Component;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * <em>AutoBuilder</em> is a magic builder library for unit tests on JVM.
 *
 * TODO:  ways of resolution: 1) Directly with a resolver or 2) Resolve as a bean with BeanResolver
 *
 */
public class AutoBuilder {

    private AutoBuilder() { }

    @Singleton
    @Component(modules = AutoBuilderModule.class)
    interface AutoBuilderComponent {
        BuilderDSLFactory getBuilderFactory();
        ResolversRegistry getRegistry();
        Set<Initializable> getInitBeans();
    }

    private static final BuilderDSLFactory factory;
    private static final ResolversRegistry registry;

    static {
        // We use Dagger under the hood to wire things up, but we keep that as an impl. detail for now
        AutoBuilderComponent component = DaggerAutoBuilder_AutoBuilderComponent.create();
        // Autobuilder synchronous boot-up sequence - done only once when this class is loaded
        component.getInitBeans().forEach(Initializable::init);
        // Our by-now-ready-to-use factory of builder objects
        factory = component.getBuilderFactory();
        registry = component.getRegistry();
    }

    public static void registerValue(String name, Object value, Annotation... requiredAnnotations) {
        registry.registerValue(name, value, requiredAnnotations);
    }

    public static void registerResolver(ValueResolver resolver) {
        registry.registerResolver(resolver);
    }

    public static <T> BuilderDSL<T> instanceOf(Class<T> type) {
        return factory.create(type);
    }

    @Nullable
    static <T> T create(Class<T> type, Map<String, Object> properties) {
        return instanceOf(type).with(properties).build();
    }

}

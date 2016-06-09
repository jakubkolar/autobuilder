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

package com.github.jakubkolar.autobuilder;

import com.github.jakubkolar.autobuilder.api.BuilderDSL;
import com.github.jakubkolar.autobuilder.impl.AutoBuilderModule;
import com.github.jakubkolar.autobuilder.impl.BuilderDSLFactory;
import com.github.jakubkolar.autobuilder.impl.Initializable;
import com.github.jakubkolar.autobuilder.impl.ResolversRegistry;
import com.github.jakubkolar.autobuilder.spi.ValueResolver;
import com.google.common.annotations.Beta;
import dagger.Component;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * <em>AutoBuilder</em> is a magic builder library for unit tests on JVM.
 *
 * TODO: AB-003
 *
 * @since 0.0.1
 */
@Beta
public class AutoBuilder {

    private static final BuilderDSLFactory factory;
    private static final ResolversRegistry registry;

    private AutoBuilder() {
        // Utility class is not instantiable, exclude this constructor from API docs
    }

    @Singleton
    @Component(modules = AutoBuilderModule.class)
    interface AutoBuilderComponent {
        BuilderDSLFactory getBuilderFactory();
        ResolversRegistry getRegistry();
        Set<Initializable> getInitBeans();
    }

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

    /**
     * A shortcut for the {@link #instanceOf(Class)} method.
     *
     * <p> Meant to be imported with {@code import static} and used as a DSL element in
     * tests.
     *
     * @param type class object for the type the builder will build
     * @param <T> the type of objects the builder will build
     * @return a brand new instance of {@link BuilderDSL}
     *
     * @since 0.2
     * @see #instanceOf(Class)
     */
    public static <T> BuilderDSL<T> a(Class<T> type) {
        return factory.create(type);
    }

    /**
     * A shortcut for the {@link #instanceOf(Class)} method.
     *
     * <p> Meant to be imported with {@code import static} and used as a DSL element in
     * tests.
     *
     * @param type class object for the type the builder will build
     * @param <T> the type of objects the builder will build
     * @return a brand new instance of {@link BuilderDSL}
     *
     * @since 0.2
     * @see #instanceOf(Class)
     */
    public static <T> BuilderDSL<T> an(Class<T> type) {
        return factory.create(type);
    }

}

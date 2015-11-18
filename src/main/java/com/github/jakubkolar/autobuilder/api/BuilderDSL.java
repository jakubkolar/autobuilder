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

package com.github.jakubkolar.autobuilder.api;


import com.github.jakubkolar.autobuilder.spi.ValueResolver;
import com.google.common.annotations.Beta;

import java.util.Map;

/**
 * Builder is an abstraction for creating arbitrary objects of type {@code T}.
 *
 * <p> Main purpose of builders is to create objects usable in tests without a lot of
 * coding specific to each type {@code T} that is to be created. Reflection is used to
 * inspect the type {@code T} and to get a builder that is able to create possibly
 * meaningful instances of {@code T} out of the box. You can create and instance of the
 * {@code BuilderDSL} using the {@link com.github.jakubkolar.autobuilder.AutoBuilder} that
 * is an entry point to the {@code AutoBuilder} library.
 *
 * <h3>Handling of {@code null}s</h3>
 *
 * <p> By default the value {@code null} is not considered valid, and the builder will try
 * to create objects not only for {@code T}, but also for all its properties. Depending on
 * configuration there can be exceptions to this behavior - for example, when a property
 * is annotated with {@link javax.annotation.Nullable} then the builder prefers using
 * {@code null}.
 *
 * <h3>Value resolution</h3>
 *
 * <p> How the created objects will actually look like is subject to different configured
 * {@link ValueResolver}s. The default behavior is implemented by built-in resolvers and
 * tries to use unusual but valid instances that may reveal possible bugs in your
 * production code. The idea is that for each test you only need to specify those
 * properties <em>relevant</em> to the test case, and leave the other properties with
 * their unusual defaults. The default behavior can be customized or completely overridden
 * by registering custom resolvers that take precedence. This can be done globally in the
 * {@code AutoBuilder} class or locally per builder instance. See {@link ValueResolver}
 * for more information on how the resolution works, what built-in resolvers are available
 * and what is the order in which they are invoked.
 *
 * <h3>Global and local configuration</h3>
 *
 * <p> Instances of the builder are <em>immutable and thread-safe</em> - except for any
 * modifiable or not thread-safe objects passed to the builder or global configuration. The state of the builder
 * is comprised of a global state configured by the {@code AutoBuilder} class, and a local
 * state specific to every builder instance.
 * A change of the global configuration using
 * the {@code AutoBuilder} class does not affect any existing builders, only new ones created
 * after the change. Any requested change to the local configuration returns a copy
 * of the original {@code BuilderDSL} instance, leaving the original unchanged.
 *
 * <h3>Created objects</h3>
 *
 * <p> The builder and the created products are disconnected and do not affect each other (of course, unless they reference modifiable objects that were
 * passed to the builder). The builder is not created for a <em>single</em> object
 * construction, and can thus be used repeatedly. As the builder is immutable and
 * cannot be changed, it follows that it is not reset after building, so
 * you can continue with the previous state or you can even further customize it.
 *
 * <h3>Property named paths</h3>
 *
 * <p> Properties are specified by their names as strings. Nested properties are supported
 * using a dot "{@code .}" as a separator. For example:
 *
 * <pre>{@code
 * class Customer {
 *     Address address;
 * }
 *
 * class Address {
 *     String street;
 * }
 *
 * // .....
 * --- work in progress ----
 * BuilderDSL<Customer> aCustomer = AutoBuilder.instanceOf(Customer.class);
 * Customer c = aCustomer.with("address.street")
 *
 * }</pre>
 *
 * @author Jakub Kolar
 * @since 0.0.1
 */
@Beta
public interface BuilderDSL<T> {

    /**
     * Uses a given {@code value} for a single property.
     *
     *
     * @param property the property that should be assigned to
     * @param value the value to be used
     * @return self (for method chaining)
     */
    BuilderDSL<T> with(String property, Object value);

    /**
     * @param properties
     * @return
     */
    BuilderDSL<T> with(Map<String, Object> properties);

    BuilderDSL<T> with(ValueResolver userResolver);

    /**
     * s Builds a new instance of {@code T}.
     *
     * @return a new instance of {@code T}
     */
    T build();

}

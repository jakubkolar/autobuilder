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

package com.github.jakubkolar.autobuilder.api;


import com.github.jakubkolar.autobuilder.spi.ValueResolver;
import com.google.common.annotations.Beta;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Map;

/**
 * Builder is an abstraction for creating arbitrary objects of type {@code T}.
 *
 * <p> Main purpose of builders is to create objects usable in tests without a lot of
 * coding specific to each type {@code T} that is to be created. Reflection is used to
 * inspect the type {@code T} and to get a builder that is able to create a possibly
 * meaningful instances of {@code T} out of the box. You can create an instance of the
 * {@code BuilderDSL} using the {@link com.github.jakubkolar.autobuilder.AutoBuilder} that
 * is an entry point to the whole library.
 *
 * <h3>Handling of {@code null}-s</h3>
 *
 * <p> By default the value {@code null} is not considered valid, and the builder will try
 * to create objects not only for {@code T}, but also for all its properties. Depending on
 * configuration there can be exceptions to this behavior - for example, when a property
 * is annotated with {@link Nullable} then the builder prefers using {@code null}.
 *
 * <h3 id="value_resolution">Value resolution</h3>
 *
 * <p> How the created objects will actually look like is subject to different configured
 * {@link ValueResolver}s. The default behavior is implemented by built-in resolvers and
 * tries to use unusual but valid values that may reveal possible bugs in your production
 * code. The idea is that for each test you only need to specify those properties
 * <em>relevant</em> to the test case, and leave the other properties with their unusual
 * defaults. The default behavior can be customized or completely overridden by
 * registering custom resolvers that take precedence. This can be done globally in the
 * {@code AutoBuilder} class or locally per builder instance. See {@link ValueResolver}
 * for more information on how the resolution works, what built-in resolvers are available
 * and what is the order in which they are invoked. The general rule for value resolution
 * is that the values should be uncommon but the same for each test run, or even (pseudo)
 * random but consistently repeatable by a given <i>seed</i> value (this is yet to be
 * implemented: <i>TODO AB-019</i>).
 *
 * <h3 id="configuration">Global and local configuration</h3>
 *
 * <p> Instances of the builder are <em>immutable and thread-safe</em> - except for any
 * modifiable or not thread-safe objects passed to the builder or global configuration.
 * The state of the builder is comprised of a global state configured by the {@code
 * AutoBuilder} class, and a local state specific to every builder instance. A change of
 * the global configuration using the {@code AutoBuilder} class does not affect any
 * existing builders, only new ones created after the change. Any requested change to the
 * local configuration returns a copy of the original {@code BuilderDSL} instance, leaving
 * the original unchanged. For more information on how configuration works, see
 * {@link ValueResolver} class.
 *
 * <h3>Created objects</h3>
 *
 * <p> It follows from the immutability that the builder and the created products are
 * disconnected and do not affect each other (of course, unless they reference modifiable
 * objects that were passed to the builder). Another consequence of the immutability is,
 * as the builder stays in exactly the same state after each method invocation, that its
 * {@link #build()} method can be used repeatedly to produce the product several times if
 * needed.
 *
 * <h3>Property names and paths</h3>
 *
 * <p> Properties are specified by their names as strings. Nested properties are supported
 * using a dot "{@code .}" as a separator. Properties specified this way can be viewed as
 * a special {@code ValueResolver} that takes precedence over built-in and other
 * resolvers.
 *
 * <h3 id="examples">Examples</h3>
 *
 * <pre>{@code
 * class Person {
 *     String name;
 *     Address address;
 * }
 *
 * class Address {
 *     String street;
 *     String city;
 * }
 *
 * // How can any instance be built
 * BuilderDSL<Person> aPerson = AutoBuilder.instanceOf(Person.class);
 * Person anybody = aPerson.build();
 * // anybody: Person [
 * //   name = "whatever_name",
 * //   address = Address [street="whatever_street", city="whatever_city"]
 * // ]
 * // Note: the "whatever_*" are used for illustration purposes here, actual values
 * // may differ based on built-in and other ValueResolvers, and you should never rely
 * // on them in your tests (as described above)
 *
 * // Be more specific
 * BuilderDSL<Person> aPersonFromLondon = aPerson.with("address.city", "London");
 * Person anybodyFromLondon = aPersonFromLondon.build();
 * // anybodyFromLondon: Person [
 * //   name = "whatever_name",
 * //   address = Address [street="whatever_street", city="London"]
 * // ]
 *
 * // Someone in particular, reuse previous builders
 * BuilderDSL<Person> aWizardMerchant = aPersonFromLondon
 *      .with("address.street", "Diagon Alley");
 *      // or .with("address", new Address("Diagon Alley", "London"));
 * Person mrOllivander = aWizardMerchant.with("name", "Garrick Ollivander");
 * // mrOllivander: Person [
 * //   name = "Garrick Ollivander",
 * //   address = Address [street="Diagon Alley", city="London"]
 * // ]
 * }</pre>
 *
 * @param <T> the type of object to be built
 * @author Jakub Kolar
 * @since 0.0.1
 * @see ValueResolver
 */
@Beta
@Immutable
public interface BuilderDSL<T> {

    /**
     * Uses a given {@code value} for a single property.
     *
     * <p> It is expected that the type of the property is {@link Class#isAssignableFrom}
     * the type of the value passed in.
     * <i>TODO AB-020: Otherwise, an {@code IllegalArgumentException} is thrown.</i>
     *
     * <p> Properties registered here take precedence over properties registered globally
     * using the {@code AutoBuilder} class, over built-in resolvers, and also over any
     * registered {@code ValueResolver}s, either local using {@link #with(ValueResolver)}
     * or global using {@link com.github.jakubkolar.autobuilder.AutoBuilder}.
     *
     * <p> Registering the same property name twice is not allowed, and will fail with a
     * {@code RuntimeException}. <i>TODO AB-025: remove this restriction,
     * FIXME AB-024: Null values do not behave correctly here.</i>
     *
     * @param property the property or path that should be assigned to
     * @param value    the actual value to be used, including {@code null}
     * @return a new {@code BuilderDSL<T>} with the requested modification
     * @throws IllegalArgumentException if the type of the value is not assignable to the
     *                                  type of the property or if the property name was
     *                                  already used
     */
    BuilderDSL<T> with(String property, @Nullable Object value);

    /**
     * For each property or path specified by each {@link java.util.Map.Entry#getKey()} a
     * value of the corresponding {@link java.util.Map.Entry#getValue()} is used.
     *
     * <p> The resulting builder is equivalent to the one obtained by calling {@link
     * #with(String, Object)} for each entry of the map. Specifically, it means that the
     * same restriction for the {@code value} types and target property types as in {@link
     * #with(String, Object)} applies here.
     *
     * <p> It also means that the registered properties using this method and {@link
     * #with(String, Object)} are <em>merged</em>, and that the same property name can be
     * registered only once either by this method or by the other one.
     *
     * @param properties name-value pairs that should be used to supply values for given
     *                   properties or paths
     * @return a new {@code BuilderDSL<T>} with the requested modification(s)
     * @throws IllegalArgumentException if the type of any value is not assignable to the
     *                                  type of the corresponding property or if any of
     *                                  the property names was already used
     */
    BuilderDSL<T> with(Map<String, Object> properties);

    /**
     * Use an additional {@code ValueResolver}.
     *
     * <p> The resulting builder keeps all the {@code ValueResolver}s registered with the
     * previous builder (if any), plus the new {@code ValueResolver}, which takes
     * precedence over the other resolvers and also over any global or built-in
     * resolvers.
     *
     * @param userResolver the new resolver to be added
     * @return a new {@code BuilderDSL<T>} with the requested modification(s)
     *
     * @see <a href="#configuration">Configuration section</a>
     */
    BuilderDSL<T> with(ValueResolver userResolver);

    /**
     * Builds an instance of {@code T} based on configuration of this builder.
     *
     * <p> The configuration is comprised of all the registered properties, {@code
     * ValueResolver}s, as well as global properties and global resolvers in {@code
     * AutoBuilder}. How and in which order they are applied to resolve the instance and
     * (possibly) resolve its properties is described in <a href="#value_resolution">the
     * value resolution</a> section and in the {@link ValueResolver} class.
     *
     * <p> Also note that, if configured so, the builder may return an already existing
     * instance, e.g. from a pool or a singleton, or it may even return {@code null}. So,
     * unlike with 'traditional' builders, a new instance may not always be created.
     *
     * @return an instance of {@code T}, or {@code null} if configured so
     * @throws UnsupportedOperationException if the builder is unable to resolve an
     *                                       instance of {@code T}
     * @see <a href="#examples">Examples section</a>
     */
    @Nullable
    T build();

}

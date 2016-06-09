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

package com.github.jakubkolar.autobuilder.groovy;

import com.github.jakubkolar.autobuilder.AutoBuilder;
import com.github.jakubkolar.autobuilder.api.BuilderDSL;
import com.google.common.annotations.Beta;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;

import javax.annotation.Nullable;
import java.util.List;

import static groovy.lang.Closure.DELEGATE_FIRST;


/**
 * Groovy extension module for the {@code BuilderDSL}.
 *
 * This class defines new groovy methods that appear on the classes {@link BuilderDSL}
 * and {@link Class} inside the Groovy environment.
 *
 * @author Jakub Kolar
 * @see <a href="http://groovy-lang.org/metaprogramming.html#_extension_modules">Groovy
 * Extenstion Modules</a>
 * @since 0.2
 */
@Beta
public class BuilderDSLGroovyMethods {

    private BuilderDSLGroovyMethods() {
        // Utility class is not instantiable, exclude this constructor from API docs
    }

    /**
     * A shortcut for directly creating an object using {@code AutoBuilder}.
     *
     * <p> Interaction with the {@link BuilderDSL} is done inside the passed-in {@code
     * Closure} by setting properties as if the closure directly acted upon the
     * to-be-created object. Properties are specified by their names as strings, and
     * nested properties are supported using a dot '{@code .}' as a separator. Setting a
     * property inside the closure is equivalent to calling the {@link
     * BuilderDSL#with(String, Object)} method.
     *
     * <p> For example:
     * <pre>{@code
     * class Person {
     *     String name;
     *     Address address;
     * }
     *
     * // Groovy code:
     * def person = Person.of {
     *     name = "Granger, Hermione"
     *     address.city = LONDON
     * }
     * }</pre>
     *
     * @param self         an object on which this extension method is invoked
     * @param instanceData the closure that sets the property values on the built object
     * @param <T>          the type of the object to be built
     *
     * @return an instance of {@code T}, or {@code null} if configured so, equivalent to
     * calling the {@link BuilderDSL#build()} method
     */
    @Nullable
    public static <T> T of(
            @DelegatesTo.Target Class<T> self,
            @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) Closure<?> instanceData) {
        return TableDSL.parseSingle(AutoBuilder.instanceOf(self), instanceData);
    }

    /**
     * A shortcut for the {@link #fromTable(BuilderDSL, Closure)} method.
     *
     * <p> Can be called on the class object directly without the need to create a {@code
     * BuilderDSL}. This method is helpful in <i>IDE</i>s that offer proper
     * auto-completion features for the properties in the passed-in closure (and that do
     * not have such auto-completion when using the other {@code fromTable} method).
     *
     * @param self      an object on which this extension method is invoked
     * @param tableData closure with the definition of the tabular data
     * @param <T>       the type of objects to be built
     *
     * @return a list with one instance of {@code T} created using the {@link
     * BuilderDSL#build()} method for each row in the table; never {@code null}, but may
     * be {@code empty} and may contain {@code null} elements if the builder was
     * configured to resolve some elements as {@code null}
     *
     * @see #fromTable(BuilderDSL, Closure)
     */
    public static <T> List<T> fromTable(
            @DelegatesTo.Target Class<T> self,
            @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) Closure<?> tableData) {
        return fromTable(AutoBuilder.instanceOf(self), tableData);
    }

    /**
     * Builds a list of instances of {@code T} based on tabular data.
     *
     * <p> The passed-in {@code Closure} contains a <i>table definition</i> of the data
     * that:
     * <ul>
     * <li>has at least one row</li>
     *
     * <li>in each row has two or more columns separated by the bitwise or '|'
     * operator</li>
     *
     * <li>in the first row (the header row) contains the property names that would
     * otherwise be used in the {@link BuilderDSL#with(String, Object)} method (nested
     * properties are supported using a dot '{@code .}') separator</li>
     *
     * <li>in each row after the first row contains the actual values that would be used
     * in the {@link BuilderDSL#with(String, Object)} method for the individual
     * properties</li>
     * </ul>
     *
     * <p> An object of the requested type is built from each row in the table after the
     * header. The order of the rows in the table corresponds to the order of the built
     * instances in the resulting {@code List}. A table with only the header row will
     * result in an empty {@code List} being returned.
     *
     * <p> The table definition in the closure can also be freely combined with setting
     * the property values as in the closures passed to the method {@link #of(Class,
     * Closure)} - see examples below more details. The passed-in builder is used as a
     * basis to create each instance. This also means that any configuration done in the
     * closure overrides the configuration done in the builder so far.
     *
     * <p> Examples:
     * <pre>{@code
     * class Person {
     *     String firstName;
     *     String lastName;
     *     int age;
     *     Address address;
     * }
     *
     * // Groovy code:
     * def people = a Person fromTable {
     *      // Notation from the '#of' method can be used here as well
     *      address.street = '<unknown>'
     *
     *      // Properties set in this way are added to every row unless already defined
     *      // by that row (FIXME: does not work, will be fixed by AB-025)
     *      age = 17
     *
     *      // The table is defined as follows:
     *      firstName  | lastName     | age | address.city
     *      'Harry'    | 'Potter'     | 17  | LITTLE_WHINGING
     *      'Ronald'   | 'Weasley'    | 17  | OTTERY_ST_CATCHPOLE
     *      'Hermione' | 'Granger'    | 18  | LONDON
     *      'Albus'    | 'Dumbledore' | 115 | HOGWARTS
     * }
     * }</pre>
     *
     * @param self      an object on which this extension method is invoked
     * @param tableData closure with the definition of the tabular data
     * @param <T>       the type of objects to be built
     *
     * @return a list with one instance of {@code T} created using the {@link
     * BuilderDSL#build()} method for each row in the table; never {@code null}, but may
     * be {@code empty} and may contain {@code null} elements if the builder was
     * configured to resolve some elements as {@code null}
     */
    public static <T> List<T> fromTable(
            @DelegatesTo.Target BuilderDSL<T> self,
            @DelegatesTo(strategy = DELEGATE_FIRST, genericTypeIndex = 0) Closure<?> tableData) {
        return TableDSL.parseTable(self, tableData);
    }

    /**
     * A shortcut for converting the result of {@link BuilderDSL#build()} method.
     *
     * <p> Tries to convert a given {@link BuilderDSL} object to the target type {@code U}
     * by first building an instance of {@code T} using the {@link BuilderDSL#build()}
     * method and then coercing that to an instance of {@code U}. If {@code T} is not
     * assignment-compatible with {@code U} then the default Groovy <a
     * href="http://groovy-lang.org/operators.html#_coercion_operator">coercion handling
     * </a>  is used.
     *
     * <p> This method defines the behavior of th groovy 'as' operator when used on a
     * {@code BuilderDSL} type.
     *
     * @param self   the builder to be converted
     * @param target the class object for the target type
     * @param <T>    the type of object that will be built by the builder
     * @param <U>    the target type to which the built object will be converted
     *
     * @return an instance of {@code T}, or {@code null} if configured so
     *
     * @throws GroovyCastException - if the built object cannot be coerced to the type
     *                             {@code U}
     */
    @Nullable
    public static <T, U> U asType(BuilderDSL<T> self, Class<U> target) {
        T built = self.build();

        if (built == null || target.isInstance(built)) {
            return target.cast(built);
        } else {
            // Try the default handling of the 'as' operator:
            // TODO: maybe check if built is not an instance of e.g. a Collection or a
            // Number, DefaultGroovyMethods defines some special handling for them
            // We should also check is the built object implements 'asType' directly
            // (discovering extension 'asType' methods that could apply here would
            // probably be too much)
            return DefaultGroovyMethods.asType(built, target);
        }
    }

}

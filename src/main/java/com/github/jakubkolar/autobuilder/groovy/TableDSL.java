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

import com.github.jakubkolar.autobuilder.api.BuilderDSL;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.GroovyCategorySupport;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
class TableDSL {

    /**
     * This class is used as a <a href="http://groovy-lang.org/metaprogramming.html#categories">Groovy
     * category</a> during an execution of a closure with a data-table or a closure that
     * sets properties with a {@code BuilderDSL} on a resulting object. Because only
     * static methods may be called on a category and there is no other way to pass any
     * "context" to the closure, we unfortunately had to use a global variable like this.
     *
     * <p> A {@code ThreadLocal} is used so that the parsing can be invoked simultaneously
     * from multiple threads, for example when running tests in parallel.
     *
     * <p> The trick and the whole idea is taken from <a href="http://tux2323.blogspot.cz/2013/04/simple-table-dsl-in-groovy.html">
     * Simple table DSL in Groovy</a> blog post.
     */
    private static final ThreadLocal<Context<?>> context = new ThreadLocal<>();

    private TableDSL() {
        // Utility class is not instantiable
    }

    @Nullable
    public static <T> T parseSingle(BuilderDSL<T> builder, Closure<?> instanceData) {
        return parse(builder, instanceData, Context::buildSingle);
    }

    public static <T> List<T> parseTable(BuilderDSL<T> builder, Closure<?> tableData) {
        return parse(builder, tableData, Context::buildMany);
    }

    /**
     * Default implementation of the '|' operator.
     *
     * <p> Used when a new table row is started, first two columns are used to create
     * a new {@link TableRow}. This is the origin of the restriction that a table must
     * have at least two columns - without it, we do not have means to "step in" and
     * set up a new row in the context.
     */
    public static TableRow or(Object self, Object argument) {
        TableRow newRow = TableRow.of(self, argument);
        context.get().addRow(newRow);
        return newRow;
    }

    /**
     * This 'overrides' the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#or(Number,
     * Number)} in the case {@code Number}s were used with the '|' operator.
     */
    public static TableRow or(Number self, Number argument) {
        return or((Object) self, argument);
    }

    /**
     * This 'overrides' the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#or(BitSet,
     * BitSet)} in the case {@code BitSet}s were used with the '|' operator.
     */
    public static TableRow or(BitSet self, BitSet argument) {
        return or((Object) self, argument);
    }

    /**
     * This 'overrides' the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#or(Boolean,
     * Boolean)} in the case {@code Boolean}s were used with the '|' operator.
     */
    public static TableRow or(Boolean self, Boolean argument) {
        return or((Object) self, argument);
    }

    /**
     * Handles the assignment of a resulting object's property inside the DSL closure.
     */
    public static void setProperty(String property, @Nullable Object newValue) {
        context.get().addProperty(property, newValue);
    }

    private static <T, R> R parse(BuilderDSL<T> builder, Closure<?> tableData, Function<Context<T>, R> result) {
        Context<T> c = new Context<>(builder);
        context.set(c);

        tableData.setResolveStrategy(Closure.DELEGATE_FIRST);
        tableData.setDelegate(new VariableResolvingDelegate());

        // If tableData.call() happens to call this method again, our global variable 'context'
        // gets all messed up - TODO: maybe a stack push/pop mechanism should be introduced
        GroovyCategorySupport.use(TableDSL.class, tableData);

        return result.apply(c);
    }

    /**
     * Instance of this class is used as a delegate of the DSL closure.
     *
     * <p> This makes assigning-to and using the resulting object's properties (nonexistent
     * in the scope of the closure) possible by wrapping their name with and instance
     * of the class {@link Variable}.
     */
    private static class VariableResolvingDelegate extends GroovyObjectSupport {

        @Override
        public Object getProperty(String property) {
            return new Variable(property);
        }

        @Override
        public void setProperty(String property, Object newValue) {
            // Called when a (non-nested) property is set, e.g. in:
            // TableDSL.parseSingle builder, { a = 1 }
            // this method will get called as: setProperty('a', 1)
            TableDSL.setProperty(property, newValue);
        }

    }

    private static class Context<T> {

        private BuilderDSL<T> builder;
        private final List<TableRow> collectedRows;

        public Context(BuilderDSL<T> builder) {
            this.builder = builder;
            this.collectedRows = new ArrayList<>();
        }

        public void addRow(TableRow newRow) {
            collectedRows.add(newRow);
        }

        public void addProperty(String property, @Nullable Object newValue) {
            builder = builder.with(property, newValue);
        }

        @Nullable
        public T buildSingle() {
            return builder.build();
        }

        public List<T> buildMany() {
            return Table.of(collectedRows).stream()
                    .map(props -> builder.with(props).build())
                    .collect(Collectors.toList());
        }
    }
}

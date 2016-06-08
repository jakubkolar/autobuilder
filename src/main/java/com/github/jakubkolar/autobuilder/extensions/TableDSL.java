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

package com.github.jakubkolar.autobuilder.extensions;

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

    private static final ThreadLocal<Context<?>> context = new ThreadLocal<>();

    @Nullable
    public static <T> T parseSingle(BuilderDSL<T> builder, Closure<?> tableData) {
        return parse(builder, tableData, Context::buildSingle);
    }

    public static <T> List<T> parseTable(BuilderDSL<T> builder, Closure<?> tableData) {
        return parse(builder, tableData, Context::buildMany);
    }

    /**
     * Default implementation of the '|' operator.
     */
    public static TableRow or(Object self, Object argument) {
        TableRow newRow = TableRow.of(self, argument);
        context.get().addRow(newRow);
        return newRow;
    }

    /**
     * Default implementation of the '|' operator.
     */
    public static TableRow or(List<Object> self, Object argument) {
        return or((Object) self, argument);
    }

    /**
     * This 'overrides' the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#or(Number,
     * Number)}
     * in the case {@code Number}s were used with the '|' operator.
     */
    public static TableRow or(Number self, Number argument) {
        return or((Object) self, argument);
    }

    /**
     * This 'overrides' the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#or(BitSet,
     * BitSet)}
     * in the case {@code BitSet}s were used with the '|' operator.
     */
    public static TableRow or(BitSet self, BitSet argument) {
        return or((Object) self, argument);
    }

    /**
     * This 'overrides' the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#or(Boolean,
     * Boolean)}
     * in the case {@code Boolean}s were used with the '|' operator.
     */
    public static TableRow or(Boolean self, Boolean argument) {
        return or((Object) self, argument);
    }

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

    private static class VariableResolvingDelegate extends GroovyObjectSupport {

        @Override
        public Object getProperty(String property) {
            return new Variable(property);
        }

        @Override
        public void setProperty(String property, Object newValue) {
            // TODO: when is it called?
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

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

import javax.annotation.Nullable;
import java.util.List;

/**
 * TODO
 *
 * @since 0.2
 */
@Beta
public class BuilderDSLGroovyMethods {

    private BuilderDSLGroovyMethods() {
        // Groovy extension module - static methods only
    }

    @Nullable
    public static <T> T of(Class<T> self, Closure<?> instanceData) {
        return TableDSL.parseSingle(AutoBuilder.instanceOf(self), instanceData);
    }

    public static <T> List<T> fromTable(Class<T> self, Closure<?> tableData) {
        return fromTable(AutoBuilder.instanceOf(self), tableData);
    }

    public static <T> List<T> fromTable(BuilderDSL<T> self, Closure<?> tableData) {
        return TableDSL.parseTable(self, tableData);
    }

    public static <T, U> U asType(BuilderDSL<T> self, Class<U> target) {
        return target.cast(self.build());
    }

}

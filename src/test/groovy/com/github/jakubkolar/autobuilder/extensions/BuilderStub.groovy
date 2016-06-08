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

package com.github.jakubkolar.autobuilder.extensions

import com.github.jakubkolar.autobuilder.api.BuilderDSL
import com.github.jakubkolar.autobuilder.spi.ValueResolver

import javax.annotation.Nullable

class BuilderStub implements BuilderDSL<Map<String, Object>> {

    private final Map<String, Object> properties

    BuilderStub() {
        this([:])
    }

    private BuilderStub(Map<String, Object> properties) {
        this.properties = properties
    }

    @Override
    BuilderDSL<Map<String, Object>> with(String property, @Nullable Object value) {
        new BuilderStub(properties + [(property): value])
    }

    @Override
    BuilderDSL<Map<String, Object>> with(Map<String, Object> properties) {
        new BuilderStub(this.properties + properties)
    }

    @Override
    BuilderDSL<Map<String, Object>> with(ValueResolver userResolver) {
        this
    }

    @Override
    <R> BuilderDSL<Map<String, Object>> with(Class<R> type, @Nullable R value) {
        this
    }

    @Override
    Map<String, Object> build() {
        Collections.unmodifiableMap(properties)
    }
}

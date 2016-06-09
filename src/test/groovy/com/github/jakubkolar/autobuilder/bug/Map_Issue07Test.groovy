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

package com.github.jakubkolar.autobuilder.bug

import com.github.jakubkolar.autobuilder.AutoBuilder
import spock.lang.Specification

class Map_Issue07Test extends Specification {

    def "BuilderDSL.with(Map properties) only considers the last property in the map"() {
        when:
        def instance = AutoBuilder.instanceOf(SeveralFields)
                .with(field1: 'ABC', field2: 123, field3: 321)
                .build()

        then:
        assert instance.field1 == 'ABC'
        assert instance.field2 == 123
        assert instance.field3 == 321
    }

    def "AutoBuilder.create(Class, Map) only considers the last property in the map"() {
        when:
        def instance = AutoBuilder.instanceOf(SeveralFields).with(field1: 'ABC', field2: 123, field3: 321).build()

        then:
        assert instance.field1 == 'ABC'
        assert instance.field2 == 123
        assert instance.field3 == 321
    }

}
